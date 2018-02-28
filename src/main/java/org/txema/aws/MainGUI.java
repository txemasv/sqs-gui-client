package org.txema.aws;

import javafx.application.Application;
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
    private TextField sendMessageUrl = new TextField();
    private TextField receiveMessageUrl = new TextField();
    private TextField deleteMessageUrl = new TextField();
    private String queueUrl = "";
    private static final double height = 600;
    private static final double width = 800;
    private static final double prefWidth = 5 * width / 6;
    private Button buttonDelete;
    private Button buttonPurge;

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
        textArea.setScrollLeft(10);

        sendMessageUrl.setDisable(true);
        sendMessageUrl.setPrefWidth(prefWidth);
        Label queueLbl = new Label("Queue/Url");
        TextField delayTxt = new TextField("0");
        Label delayLbl = new Label("Delay");
        TextField messageTxt = new TextField("");
        Label messageLbl = new Label("Message/s");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(queueLbl, 0, row);
        grid.add(sendMessageUrl, 0, ++row);
        grid.add(delayLbl, 0, ++row);
        grid.add(delayTxt, 0, ++row);
        grid.add(messageLbl, 0, ++row);
        grid.add(messageTxt, 0, ++row);

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

        grid.add(buttonSend, 0, ++row);
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
        TextField receiptHandleTxt = new TextField();

        receiveMessageUrl.setDisable(true);
        receiveMessageUrl.setPrefWidth(prefWidth);
        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(queueLbl, 0, ++row);
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
        textArea.setEditable(false);

        Label queueLbl = new Label("Queue/Url");
        queuesTxt.setPrefWidth(prefWidth);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(queueLbl, 0, ++row);
        grid.add(queuesTxt, 0, ++row);

        Button buttonCreate = new Button("Get/Create");
        buttonCreate.setOnAction(e -> {
            textArea.setText("\nLoading ...");
            Runnable run = () -> {
                String url = sqsClient.createQueue(queuesTxt.getText());
                setQueue(url);
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        grid.add(buttonCreate, 0, ++row);
        grid.add(textArea, 0, ++row);

        buttonPurge = new Button("Purge");
        buttonPurge.setDisable(true);
        buttonPurge.setOnAction(e -> {
            textArea.setText("\nLoading ...");
            Runnable run = () -> {
                sqsClient.purgeQueue(queueUrl);
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        buttonDelete = new Button("Delete");
        buttonDelete.setDisable(true);
        buttonDelete.setOnAction(e -> {
            textArea.setText("\nLoading ...");
            Runnable run = () -> {
                sqsClient.deleteQueue(queueUrl);
                setQueue(null);
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        Button buttonListQueues = new Button("ListQueues");
        buttonListQueues.setOnAction(e -> {
            textArea.setText("\nLoading ...");
            Runnable run = () -> {
                sqsClient.listQueues();
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        GridPane gridSub = new GridPane();
        gridSub.add(buttonPurge, 0, 0);
        gridSub.add(buttonDelete, 1, 0);
        gridSub.add(buttonListQueues, 2, 0);
        gridSub.setHgap(10);
        gridSub.setVgap(10);

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
        } else {
            queueUrl = url;
            sendMessageUrl.setText(queueUrl);
            receiveMessageUrl.setText(queueUrl);
            deleteMessageUrl.setText(queueUrl);
            tabReceive.setDisable(false);
            tabSend.setDisable(false);
            tabDelete.setDisable(false);
            buttonPurge.setDisable(false);
            buttonDelete.setDisable(false);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}