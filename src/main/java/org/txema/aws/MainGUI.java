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

public class MainGUI extends Application {

    private SqsClient queueService = ApplicationContext.getInstance().getSqsClient();
    private Tab tabSend = new Tab();
    private Tab tabReceive = new Tab();
    private Tab tabQueues = new Tab();
    private TextField queuesTxt = new TextField();
    private TextField sendMessageTxt = new TextField();
    private TextField receiveMessageTxt = new TextField();
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
    }

    private Tab sendMessageSection() {
        tabSend.setText("Send");
        tabSend.setDisable(true);
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setScrollLeft(10);

        sendMessageTxt.setDisable(true);
        sendMessageTxt.setPrefWidth(prefWidth);
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
        grid.add(sendMessageTxt, 0, ++row);
        grid.add(delayLbl, 0, ++row);
        grid.add(delayTxt, 0, ++row);
        grid.add(messageLbl, 0, ++row);
        grid.add(messageTxt, 0, ++row);

        Button buttonPush = new Button("Send");
        buttonPush.setOnAction(e -> {
            String newValue = delayTxt.getText();
            if (newValue == null || !newValue.matches("^[0-9]\\d*$")) {
                delayTxt.setText("0");
            }
            Thread t = new Thread("producer") {
                public void run() {
                    queueService.sendMessage(queueUrl, Integer.valueOf(delayTxt.getText()), messageTxt.getText());
                    textArea.appendText(Log.getInfo());
                }
            };
            t.run();

        });

        grid.add(buttonPush, 0, ++row);
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

        receiveMessageTxt.setDisable(true);
        receiveMessageTxt.setPrefWidth(prefWidth);
        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(queueLbl, 0, ++row);
        grid.add(receiveMessageTxt, 0, ++row);

        Button buttonPull = new Button("Receive");
        buttonPull.setOnAction(e -> {
            Thread t = new Thread("consume") {
                public void run() {
                    queueService.receiveMessage(queueUrl);
                    textArea.appendText(Log.getInfo());
                }
            };
            t.start();
        });

        grid.add(buttonPull, 0, ++row);
        grid.add(textArea, 0, ++row);
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
            textArea.setText("\n...");
            Thread t = new Thread("creator") {
                public void run() {
                    String url = queueService.createQueue(queuesTxt.getText());
                    setQueue(url);
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
        });

        grid.add(buttonCreate, 0, ++row);
        grid.add(textArea, 0, ++row);

        buttonPurge = new Button("Purge");
        buttonPurge.setDisable(true);
        buttonPurge.setOnAction(e -> {
            textArea.setText("\n...");
            Thread t = new Thread("creator") {
                public void run() {
                    queueService.purgeQueue(queueUrl);
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
        });

        buttonDelete = new Button("Delete");
        buttonDelete.setDisable(true);
        buttonDelete.setOnAction(e -> {
            textArea.setText("\n...");
            Thread t = new Thread("creator") {
                public void run() {
                    queueService.deleteQueue(queueUrl);
                    setQueue(null);
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
        });

        Button buttonListQueues = new Button("ListQueues");
        buttonListQueues.setOnAction(e -> {
            textArea.setText("\n...");
            Thread t = new Thread("creator") {
                public void run() {
                    queueService.listQueues();
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
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
            buttonDelete.setDisable(true);
            buttonPurge.setDisable(true);
        } else {
            queueUrl = url;
            sendMessageTxt.setText(queueUrl);
            receiveMessageTxt.setText(queueUrl);
            tabReceive.setDisable(false);
            tabSend.setDisable(false);
            buttonPurge.setDisable(false);
            buttonDelete.setDisable(false);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}