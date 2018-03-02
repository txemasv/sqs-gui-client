package org.txema.aws;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    private SqsClient sqsClient;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private TabPane tabPane;
    private Tab tabSend;
    private Tab tabReceive;
    private Tab tabQueues;
    private TextField inputQueue;
    private TextField inputTimeout;
    private TextField inputReceiptHandle;
    private TextField inputBodyMessage;
    private TextField inputDelay;
    private RadioButton urlQueuesSection;
    private RadioButton urlSendSection;
    private RadioButton urlReceiveSection;
    private String queueUrl = "";
    private static final double height = 600;
    private static final double width = 820;
    private static final double prefWidth = 7 * width / 8;
    private Button buttonDelete;
    private Button buttonPurge;
    private Button buttonTimeout;
    private TextArea consoleQueues;
    private TextArea consoleReceive;
    private TextArea consoleSend;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SQS Client");
        primaryStage.setScene(contentScene());
        primaryStage.show();
        primaryStage.setOnCloseRequest(we -> executorService.shutdownNow());
    }

    private Scene contentScene() {
        Group root = new Group();
        Scene scene = new Scene(root, width, height, Color.WHITE);

        tabPane = new TabPane();
        inputQueue = new TextField();
        urlQueuesSection = new RadioButton("No queue selected");
        urlSendSection = new RadioButton();
        urlReceiveSection = new RadioButton();
        inputTimeout = new TextField();

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

        return scene;
    }

    private Tab credentialsSection() {
        Tab tabCredentials = new Tab("Credentials");
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.prefWidthProperty().bind(vbox.prefWidthProperty());

        Credentials credentials = ApplicationContext.getInstance().getCredentials();
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

        Button buttonConnect = new Button("Connect");
        buttonConnect.setOnAction(event -> {
            textArea.setText("\nVerifying Credentials...");
            String accessKey = accessTxt.getText();
            String secretKey = secretTxt.getText();
            if (ApplicationContext.getInstance().testCredentials(accessKey, secretKey)) {
                sqsClient = ApplicationContext.getInstance().renewSqsClient(accessKey, secretKey);
                tabQueues.setDisable(false);
                tabPane.getSelectionModel().select(tabQueues);
                clearAll();
                setQueue(null);
            }
            textArea.setText(Log.getInfo());
        });

        int row = 0;
        grid.add(accessLbl, 0, ++row);
        grid.add(accessTxt, 0, ++row);
        grid.add(accessTxtPsw, 0, row);
        grid.add(secretLbl, 0, ++row);
        grid.add(secretTxt, 0, ++row);
        grid.add(secretTxtPsw, 0, row);
        grid.add(buttonConnect, 0, ++row);
        grid.add(textArea, 0, ++row);
        vbox.getChildren().add(grid);
        vbox.setAlignment(Pos.CENTER);
        tabCredentials.setContent(vbox);

        return tabCredentials;
    }

    private Tab sendMessageSection() {
        tabSend = new Tab("Send");
        tabSend.setDisable(true);
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        consoleSend = new TextArea();
        consoleSend.setEditable(false);
        consoleSend.prefWidthProperty().bind(vbox.prefWidthProperty());

        inputDelay = new TextField("0");
        Label delayLbl = new Label("Delay");
        inputBodyMessage = new TextField("");
        Label messageLbl = new Label("MessageBody");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        urlSendSection.setToggleGroup(new ToggleGroup());
        urlSendSection.setSelected(true);

        int row = 0;
        grid.add(urlSendSection, 0, ++row);

        Button buttonSend = new Button("Send");
        buttonSend.setOnAction(e -> {
            String newValue = inputDelay.getText();
            if (newValue == null || !newValue.matches("^[0-9]\\d*$")) {
                inputDelay.setText("0");
            }
            Runnable run = () -> {
                sqsClient.sendMessage(queueUrl, Integer.valueOf(inputDelay.getText()), inputBodyMessage.getText());
                consoleSend.appendText(Log.getInfo());
            };
            executorService.submit(run);
        });

        GridPane gridSub = new GridPane();
        gridSub.setHgap(10);
        gridSub.setVgap(10);
        gridSub.add(delayLbl, 0, 0);
        gridSub.add(inputDelay, 1, 0);
        gridSub.add(messageLbl, 0, 1);
        gridSub.add(inputBodyMessage, 1, 1);
        gridSub.add(buttonSend, 0, 2);

        grid.add(gridSub, 0, ++row);
        grid.add(consoleSend, 0, ++row);

        vbox.getChildren().add(grid);
        vbox.setAlignment(Pos.CENTER);
        tabSend.setContent(vbox);

        return tabSend;
    }

    private Tab receiveMessageSection() {
        tabReceive = new Tab("Receive");
        tabReceive.setDisable(true);
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        consoleReceive = new TextArea();
        consoleReceive.setEditable(false);
        consoleReceive.prefWidthProperty().bind(vbox.prefWidthProperty());
        consoleReceive.setScrollLeft(10);

        inputReceiptHandle = new TextField();
        inputReceiptHandle.setPromptText("Receipt handle");
        urlReceiveSection.setToggleGroup(new ToggleGroup());
        urlReceiveSection.setSelected(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(urlReceiveSection, 0, ++row);

        Button buttonReceive = new Button("Receive");
        buttonReceive.setOnAction(e -> {
            Runnable run = () -> {
                sqsClient.receiveMessage(queueUrl);
                consoleReceive.appendText(Log.getInfo());
            };
            executorService.submit(run);
        });

        grid.add(buttonReceive, 0, ++row);
        grid.add(consoleReceive, 0, ++row);

        Button buttonDelete = new Button("Delete");
        buttonDelete.setOnAction(e -> {
            Runnable run = () -> {
                sqsClient.deleteMessage(queueUrl, inputReceiptHandle.getText());
                consoleReceive.appendText(Log.getInfo());
            };
            executorService.submit(run);
        });

        GridPane gridSub = new GridPane();
        gridSub.add(buttonDelete, 0, 0);
        gridSub.add(inputReceiptHandle, 1, 0);
        gridSub.setHgap(10);
        gridSub.setVgap(10);

        grid.add(gridSub, 0, ++row);

        vbox.getChildren().add(grid);

        vbox.setAlignment(Pos.CENTER);
        tabReceive.setContent(vbox);
        return tabReceive;
    }

    private Tab queuesSection() {
        tabQueues = new Tab("Queue");
        tabQueues.setDisable(true);
        VBox vbox = new VBox();
        vbox.setPrefWidth(prefWidth);
        consoleQueues = new TextArea();
        consoleQueues.prefWidthProperty().bind(vbox.prefWidthProperty());
        consoleQueues.setEditable(false);

        Label queueLbl = new Label("Queue/Url");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(queueLbl, 0, ++row);
        grid.add(inputQueue, 0, ++row);

        Button buttonCreate = new Button("Get/Create");
        buttonCreate.setOnAction(e -> {
            consoleQueues.setText("\nLoading...");
            Runnable run = () -> {
                consoleQueues.setText(Log.getInfo());
                String url = sqsClient.createQueue(inputQueue.getText());
                setQueue(url);
                String timeout = sqsClient.getVisibilityTimeout(queueUrl);
                inputTimeout.setText(timeout);
                consoleQueues.setText(Log.getInfo());
            };
            Platform.runLater(run);
        });

        Button buttonListQueues = new Button("ListQueues");
        buttonListQueues.setOnAction(e -> {
            consoleQueues.setText("\nLoading...");
            Runnable run = () -> {
                sqsClient.listQueues();
                consoleQueues.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        GridPane gridTop = new GridPane();
        gridTop.add(buttonCreate, 0, 0);
        gridTop.add(buttonListQueues, 1, 0);
        gridTop.setHgap(10);
        gridTop.setVgap(10);

        grid.add(gridTop, 0, ++row);
        grid.add(consoleQueues, 0, ++row);
        buttonPurge = new Button("Purge");
        buttonPurge.setDisable(true);
        buttonPurge.setOnAction(e -> {
            consoleQueues.setText("\nLoading...");
            Runnable run = () -> {
                sqsClient.purgeQueue(queueUrl);
                consoleQueues.setText(Log.getInfo());
            };
            executorService.submit(run);
        });

        buttonDelete = new Button("Delete");
        buttonDelete.setDisable(true);
        buttonDelete.setOnAction(e -> {
            consoleQueues.setText("\nLoading...");
            Runnable run = () -> {
                sqsClient.deleteQueue(queueUrl);
                setQueue(null);
                consoleQueues.setText(Log.getInfo());
            };
            Platform.runLater(run);
        });

        buttonTimeout = new Button("Timeout");
        buttonTimeout.setDisable(true);
        buttonTimeout.setOnAction(e -> {
            consoleQueues.setText("\nLoading...");
            Runnable run = () -> {
                Integer timeout = Integer.valueOf(inputTimeout.getText());
                sqsClient.setVisibilityTimeout(queueUrl, timeout);
                consoleQueues.setText(Log.getInfo());

            };
            if (!inputTimeout.getText().isEmpty() && inputTimeout.getText().matches("\\d*")) {
                executorService.submit(run);
            } else {
                consoleQueues.setText(Log.incorrect("VisibilityTimeout"));
            }
        });

        urlQueuesSection.setToggleGroup(new ToggleGroup());
        urlQueuesSection.setSelected(true);

        GridPane gridSub = new GridPane();
        gridSub.add(buttonPurge, 0, 0);
        gridSub.add(buttonDelete, 1, 0);
        gridSub.add(buttonTimeout, 2, 0);
        gridSub.add(inputTimeout, 3, 0);
        gridSub.setHgap(10);
        gridSub.setVgap(10);

        grid.add(urlQueuesSection, 0, ++row);
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
            buttonTimeout.setDisable(true);
            inputTimeout.setDisable(true);
            urlQueuesSection.setText("No queue selected");
        } else {
            queueUrl = url;
            urlQueuesSection.setText(queueUrl);
            urlSendSection.setText(queueUrl);
            urlReceiveSection.setText(queueUrl);
            tabReceive.setDisable(false);
            tabSend.setDisable(false);
            buttonPurge.setDisable(false);
            buttonDelete.setDisable(false);
            buttonTimeout.setDisable(false);
            inputTimeout.setDisable(false);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void clearAll() {
        consoleQueues.clear();
        consoleReceive.clear();
        consoleSend.clear();
        inputTimeout.clear();
        inputQueue.clear();
        inputReceiptHandle.clear();
        inputDelay.setText("0");
        inputBodyMessage.clear();
    }
}