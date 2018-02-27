package org.txema.aws;

import com.amazonaws.AmazonServiceException;
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
    private TextField queueCreate = new TextField();
    private TextField queuePush = new TextField();
    private TextField queuePull = new TextField();
    private String queueUrl = "";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tabs");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 480, Color.WHITE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // set tabs
        BorderPane borderPane = new BorderPane();
        tabPane.getTabs().add(queueTab());
        tabPane.getTabs().add(pushTab());
        tabPane.getTabs().add(pullTab());
        borderPane.setCenter(tabPane);

        // bind to take available space
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        // set scene
        root.getChildren().add(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab pushTab() {
        //Push
        Tab tab = new Tab();
        tab.setText("Send");
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefColumnCount(5);

        //Input
        queuePush.setDisable(true);
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
        grid.add(queuePush, 1, 0);
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
                    textArea.setText(Log.getInfo());
                }
            };
            t.run();

        });

        vbox.getChildren().add(grid);
        vbox.getChildren().add(buttonPush);
        vbox.getChildren().add(textArea);

        vbox.setAlignment(Pos.CENTER);
        tab.setContent(vbox);

        return tab;
    }

    private Tab pullTab() {
        //Push
        Tab tab = new Tab();
        tab.setText("Receive");
        VBox vbox = new VBox();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        //Input
        queuePull.setDisable(true);
        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(queueLbl, 0, 0);
        grid.add(queuePull, 1, 0);

        //Button
        Button buttonPull = new Button("Pull");
        buttonPull.setOnAction(e -> {
            System.out.println("Pull message");
            Thread t = new Thread("consume") {
                public void run() {
                    queueService.receiveMessage(queueUrl);
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
        });

        vbox.getChildren().add(grid);
        vbox.getChildren().add(buttonPull);
        vbox.getChildren().add(textArea);

        vbox.setAlignment(Pos.CENTER);
        tab.setContent(vbox);
        return tab;
    }

    private Tab queueTab() {
        Tab tab = new Tab();
        tab.setText("Queue");
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
        grid.add(queueCreate, 1, 0);

        //Button
        Button buttonCreate = new Button("Get/Create");
        buttonCreate.setOnAction(e -> {
            System.out.println("Create queue");
            Thread t = new Thread("creator") {
                public void run() {
                    queueUrl = queueService.createQueue(queueCreate.getText());
                    setQueue(queueUrl);
                    textArea.setText(Log.getInfo());
                }
            };
            t.start();
        });

        vbox.getChildren().add(grid);
        vbox.getChildren().add(buttonCreate);
        vbox.getChildren().add(textArea);

        vbox.setAlignment(Pos.CENTER);
        tab.setContent(vbox);
        return tab;
    }

    private void setQueue(String queueUrl) {
        queuePush.setText(queueUrl);
        queuePull.setText(queueUrl);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}