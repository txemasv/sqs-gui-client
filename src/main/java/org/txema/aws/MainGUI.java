package org.txema.aws;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainGUI extends Application {

    private SqsClient sqsClient = ApplicationContext.getInstance().getSqsClient();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Tab tabSend = new Tab();
    private Tab tabReceive = new Tab();
    private Tab tabDelete = new Tab();
    private Tab tabQueues = new Tab();
    private TextField queuesTxt = new TextField();
    private RadioButton queuesUrl = new RadioButton("No queue selected");
    private RadioButton sendMessageUrl = new RadioButton();
    private RadioButton receiveMessageUrl = new RadioButton();
    private String queueUrl = "";
    private static final double height = 600;
    private static final double width = 820;
    private static final double prefWidth = 3 * width / 4;
    private Button buttonDelete;
    private Button buttonPurge;
    private Button buttonTimeout;
    private TextField txtTimeout = new TextField();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SQS Client");
        Group root = new Group();
        Scene scene = new Scene(root, width, height, Color.WHITE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // set tabs
        BorderPane borderPane = new BorderPane();
        tabPane.getTabs().add(queuesSection());
        tabPane.getTabs().add(sendMessageSection());
        tabPane.getTabs().add(receiveMessageSection());
        borderPane.setCenter(tabPane);

        // bind to take available space
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        // set scene
        root.getChildren().add(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(we -> executorService.shutdownNow());
    }

    private Tab sendMessageSection() {
        tabSend.setText("Send");
        tabSend.setDisable(true);
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefWidth(prefWidth);

        TextField delayTxt = new TextField("0");
        delayTxt.setPrefWidth(prefWidth);
        Label delayLbl = new Label("Delay");
        TextField messageTxt = new TextField("");
        messageTxt.setPrefWidth(prefWidth);
        Label messageLbl = new Label("MessageBody");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        sendMessageUrl.setToggleGroup(new ToggleGroup());
        sendMessageUrl.setSelected(true);

        int row = 0;
        grid.add(sendMessageUrl, 0, ++row);

        Button buttonSend = new Button("Send");
        buttonSend.setOnAction(e -> {
            String newValue = delayTxt.getText();
            if (newValue == null || !newValue.matches("^[0-9]\\d*$")) {
                delayTxt.setText("0");
            }
            Runnable run = () -> {
                sqsClient.sendMessage(queueUrl, Integer.valueOf(delayTxt.getText()), messageTxt.getText());
                textArea.appendText(Log.getInfo());
            };
            executorService.submit(run);

        });

        GridPane gridSub = new GridPane();
        gridSub.setHgap(10);
        gridSub.setVgap(10);
        gridSub.add(delayLbl, 0, 0);
        gridSub.add(delayTxt, 1, 0);
        gridSub.add(messageLbl, 0, 1);
        gridSub.add(messageTxt, 1, 1);
        gridSub.add(buttonSend, 0, 2);

        grid.add(gridSub, 0, ++row);
        grid.add(textArea, 0, ++row);

        vbox.getChildren().add(grid);
        vbox.setAlignment(Pos.CENTER);
        tabSend.setContent(vbox);

        return tabSend;
    }

    private Tab receiveMessageSection() {
        tabReceive.setText("Receive");
        tabReceive.setDisable(true);
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefWidth(prefWidth);
        textArea.setScrollLeft(10);

        TextField receiptHandleTxt = new TextField();
        receiptHandleTxt.setPrefWidth(prefWidth);

        receiveMessageUrl.setToggleGroup(new ToggleGroup());
        receiveMessageUrl.setSelected(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(receiveMessageUrl, 0, ++row);

        Button buttonReceive = new Button("Receive");
        buttonReceive.setOnAction(e -> {
            Runnable run = () -> {
                sqsClient.receiveMessage(queueUrl);
                textArea.appendText(Log.getInfo());
            };
            executorService.submit(run);
        });

        grid.add(buttonReceive, 0, ++row);
        grid.add(textArea, 0, ++row);

        Button buttonDelete = new Button("Delete");
        buttonDelete.setOnAction(e -> {
            Runnable run = () -> {
                sqsClient.deleteMessage(queueUrl, receiptHandleTxt.getText());
                textArea.appendText(Log.getInfo());
            };
            executorService.submit(run);
        });

        GridPane gridSub = new GridPane();
        gridSub.add(buttonDelete, 0, 0);
        gridSub.add(receiptHandleTxt, 1, 0);
        gridSub.setHgap(10);
        gridSub.setVgap(10);

        grid.add(gridSub, 0, ++row);

        vbox.getChildren().add(grid);

        vbox.setAlignment(Pos.CENTER);
        tabReceive.setContent(vbox);
        return tabReceive;
    }

    private Tab queuesSection() {
        tabQueues.setText("Queue");
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setPrefWidth(prefWidth);
        textArea.setEditable(false);

        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(queueLbl, 0, ++row);
        grid.add(queuesTxt, 0, ++row);

        Button buttonCreate = new Button("Get/Create");
        buttonCreate.setOnAction(e -> {
            textArea.setText("Loading...");
            Runnable run = () -> {
                String url = sqsClient.createQueue(queuesTxt.getText());
                setQueue(url);
                txtTimeout.setText(sqsClient.getVisibilityTimeout(queueUrl));
                textArea.setText(Log.getInfo());
            };
            Platform.runLater(run);
        });

        Button buttonListQueues = new Button("ListQueues");
        buttonListQueues.setOnAction(e -> {
            textArea.setText("\nLoading...");
            Runnable run = () -> {
                sqsClient.listQueues();
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        GridPane gridTop = new GridPane();
        gridTop.add(buttonCreate, 0, 0);
        gridTop.add(buttonListQueues, 1, 0);
        gridTop.setHgap(10);
        gridTop.setVgap(10);

        grid.add(gridTop, 0, ++row);
        grid.add(textArea, 0, ++row);

        buttonPurge = new Button("Purge");
        buttonPurge.setDisable(true);
        buttonPurge.setOnAction(e -> {
            textArea.setText("\nLoading...");
            Runnable run = () -> {
                sqsClient.purgeQueue(queueUrl);
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        buttonDelete = new Button("Delete");
        buttonDelete.setDisable(true);
        buttonDelete.setOnAction(e -> {
            textArea.setText("\nLoading...");
            Runnable run = () -> {
                sqsClient.deleteQueue(queueUrl);
                setQueue(null);
                textArea.setText(Log.getInfo());
            };
            Platform.runLater(run);
        });

        buttonTimeout = new Button("Timeout");
        buttonTimeout.setDisable(true);
        buttonTimeout.setOnAction(e -> {
            textArea.setText("\nLoading...");
            Runnable run = () -> {
                Integer timeout = Integer.valueOf(txtTimeout.getText());
                sqsClient.setVisibilityTimeout(queueUrl, timeout);
                textArea.setText(Log.getInfo());

            };
            if (!txtTimeout.getText().isEmpty() && txtTimeout.getText().matches("\\d*")) {
                executorService.submit(run);
            } else {
                textArea.setText(Log.incorrect("Timeout"));
            }
        });

        queuesUrl.setToggleGroup(new ToggleGroup());
        queuesUrl.setSelected(true);

        GridPane gridSub = new GridPane();
        gridSub.add(buttonPurge, 0, 0);
        gridSub.add(buttonDelete, 1, 0);
        gridSub.add(buttonTimeout, 2, 0);
        gridSub.add(txtTimeout, 3, 0);
        gridSub.setHgap(10);
        gridSub.setVgap(10);

        grid.add(queuesUrl, 0, ++row);
        grid.add(gridSub, 0, ++row);

        vbox.getChildren().add(grid);
        vbox.setAlignment(Pos.CENTER);
        tabQueues.setContent(vbox);
        return tabQueues;
    }

    private void setQueue(String url) {
        if (url == null) {
            tabReceive.setDisable(true);
            tabSend.setDisable(true);
            tabDelete.setDisable(true);
            buttonDelete.setDisable(true);
            buttonPurge.setDisable(true);
            buttonTimeout.setDisable(true);
            txtTimeout.setDisable(true);
            queuesUrl.setText("No queue selected");
        } else {
            queueUrl = url;
            queuesUrl.setText(queueUrl);
            sendMessageUrl.setText(queueUrl);
            receiveMessageUrl.setText(queueUrl);
            tabReceive.setDisable(false);
            tabSend.setDisable(false);
            tabDelete.setDisable(false);
            buttonPurge.setDisable(false);
            buttonDelete.setDisable(false);
            buttonTimeout.setDisable(false);
            txtTimeout.setDisable(false);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}