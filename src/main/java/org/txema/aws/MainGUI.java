package org.txema.aws;

import javafx.application.Application;
import javafx.geometry.Insets;
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
    private TextField createQueueTxt = new TextField();
    private TextField sendMessageTxt = new TextField();
    private TextField receiveMessageTxt = new TextField();
    private String queueUrl = "";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SQS Client");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 480, Color.WHITE);

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
        textArea.setPrefColumnCount(5);

        //Input
        sendMessageTxt.setDisable(true);
        Label queueLbl = new Label("Queue/Url");
        TextField delayTxt = new TextField("0");
        Label delayLbl = new Label("Delay");
        TextField messageTxt = new TextField("");
        Label messageLbl = new Label("Message/s");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(queueLbl, 0, 0);
        grid.add(sendMessageTxt, 1, 0);
        grid.add(delayLbl, 0, 1);
        grid.add(delayTxt, 1, 1);
        grid.add(messageLbl, 0, 2);
        grid.add(messageTxt, 1, 2);

        //Button
        Button buttonPush = new Button("Push");
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

        vbox.getChildren().add(grid);
        vbox.getChildren().add(buttonPush);
        vbox.getChildren().add(textArea);

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

        //Input
        receiveMessageTxt.setDisable(true);
        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(queueLbl, 0, 0);
        grid.add(receiveMessageTxt, 1, 0);

        //Button
        Button buttonPull = new Button("Pull");
        buttonPull.setOnAction(e -> {
            Thread t = new Thread("consume") {
                public void run() {
                    queueService.receiveMessage(queueUrl);
                    textArea.appendText(Log.getInfo());
                }
            };
            t.start();
        });

        vbox.getChildren().add(grid);
        vbox.getChildren().add(buttonPull);
        vbox.getChildren().add(textArea);

        vbox.setAlignment(Pos.CENTER);
        tabReceive.setContent(vbox);
        return tabReceive;
    }

    private Tab queuesSection() {
        tabQueues.setText("Queue");
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        //Input
        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(queueLbl, 0, 0);
        grid.add(createQueueTxt, 1, 0);

        //Button
        Button buttonCreate = new Button("Get/Create");
        buttonCreate.setOnAction(e -> {
            System.out.println("Create queue");
            Thread t = new Thread("creator") {
                public void run() {
                    String url = queueService.createQueue(createQueueTxt.getText());
                    if(url != null) {
                        setQueue(url);
                    }
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
        });

        vbox.getChildren().add(grid);
        vbox.getChildren().add(buttonCreate);
        vbox.getChildren().add(textArea);

        vbox.setAlignment(Pos.CENTER);
        tabQueues.setContent(vbox);
        return tabQueues;
    }

    private void setQueue(String url) {
        queueUrl = url;
        sendMessageTxt.setText(queueUrl);
        receiveMessageTxt.setText(queueUrl);
        tabReceive.setDisable(false);
        tabSend.setDisable(false);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}