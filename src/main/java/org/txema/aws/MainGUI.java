package org.txema.aws;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
    private TabPane tabPane = new TabPane();
    private Tab tabCredentials = new Tab();
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
    private static final double prefWidth = 7 * width / 8;
    private Button buttonDelete;
    private Button buttonPurge;
    private Button buttonTimeout;
    private TextField txtTimeout = new TextField();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SQS Client");
        Group root = new Group();
        Scene scene = new Scene(root, width, height, Color.WHITE);

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // set tabs
        BorderPane borderPane = new BorderPane();
        tabPane.getTabs().add(credentialsSection());
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

    private Tab credentialsSection() {
        tabCredentials.setText("Credentials");
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.prefWidthProperty().bind(vbox.prefWidthProperty());

        Credentials credentials = ApplicationContext.getCredentials();
        CheckBox checkBox = new CheckBox("Show Credentials");
        vbox.getChildren().add(checkBox);

        //AccessKey
        Label accessLbl = new Label("AccessKey");
        TextField accessTxt = new TextField();
        accessTxt.setManaged(false);
        accessTxt.setVisible(false);
        final PasswordField accessTxtPsw = new PasswordField();
        accessTxt.managedProperty().bind(checkBox.selectedProperty());
        accessTxt.visibleProperty().bind(checkBox.selectedProperty());
        accessTxt.setText(credentials.getAccessKey());
        accessTxtPsw.setText(credentials.getAccessKey());
        accessTxtPsw.managedProperty().bind(checkBox.selectedProperty().not());
        accessTxtPsw.visibleProperty().bind(checkBox.selectedProperty().not());
        accessTxt.textProperty().bindBidirectional(accessTxtPsw.textProperty());

        //SecretKey
        Label secretLbl = new Label("SecretKey");
        TextField secretTxt = new TextField();
        secretTxt.setManaged(false);
        secretTxt.setVisible(false);
        final PasswordField secretTxtPsw = new PasswordField();
        secretTxt.managedProperty().bind(checkBox.selectedProperty());
        secretTxt.visibleProperty().bind(checkBox.selectedProperty());
        secretTxt.setText(credentials.getSecretKey());
        secretTxtPsw.setText(credentials.getSecretKey());
        secretTxtPsw.managedProperty().bind(checkBox.selectedProperty().not());
        secretTxtPsw.visibleProperty().bind(checkBox.selectedProperty().not());
        secretTxt.textProperty().bindBidirectional(secretTxtPsw.textProperty());

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        Button buttonSave = new Button("Connect");
        buttonSave.setOnAction(e -> {
            textArea.setText("\nVerifying Credentials...");
            Runnable run = () -> {
                String accessKey = accessTxt.getText();
                String secretKey = secretTxt.getText();
                if (sqsClient.testCredentials(accessKey, secretKey)) {
                    sqsClient = ApplicationContext.getInstance().renewCredentials(new Credentials(accessKey, secretKey));
                    tabQueues.setDisable(false);
                    tabPane.getSelectionModel().select(tabQueues);
                }
                textArea.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        int row = 0;
        grid.add(accessLbl, 0, ++row);
        grid.add(accessTxt, 0, ++row);
        grid.add(accessTxtPsw, 0, row);
        grid.add(secretLbl, 0, ++row);
        grid.add(secretTxt, 0, ++row);
        grid.add(secretTxtPsw, 0, row);
        grid.add(buttonSave, 0, ++row);
        grid.add(textArea, 0, ++row);
        vbox.getChildren().add(grid);
        vbox.setAlignment(Pos.CENTER);
        tabCredentials.setContent(vbox);

        return tabCredentials;
    }

    private Tab sendMessageSection() {
        tabSend.setText("Send");
        tabSend.setDisable(true);
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.prefWidthProperty().bind(vbox.prefWidthProperty());

        TextField delayTxt = new TextField("0");
        Label delayLbl = new Label("Delay");
        TextField messageTxt = new TextField("");
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
        vbox.setPrefWidth(prefWidth);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.prefWidthProperty().bind(vbox.prefWidthProperty());
        textArea.setScrollLeft(10);

        TextField receiptHandleTxt = new TextField();
        receiptHandleTxt.setPromptText("Receipt handle");
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
        tabQueues.setDisable(true);
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        TextArea textArea = new TextArea();
        textArea.prefWidthProperty().bind(vbox.prefWidthProperty());
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
                textArea.setText(Log.incorrect("VisibilityTimeout"));
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