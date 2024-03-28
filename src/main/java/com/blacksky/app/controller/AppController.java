package com.blacksky.app.controller;

// Twilio packages
//---------------------------------------
import com.blacksky.app.model.AnimationStyles;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.OutgoingCallerId;
//---------------------------------------

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppController {

    @FXML
    AnchorPane animationRoot;
    @FXML
    private Label serverStatus;
    @FXML
    private Circle serverStatusBulb;
    @FXML
    private Button setServerInfoButton;
    @FXML
    private VBox availableReceiverWrapper;
    @FXML
    private Label selectedReceiverNumber,sendingStatus;
    @FXML
    private TextArea messageBox;
    @FXML
    private HBox sendButton;

    private static final String SERVER_INFO_FILENAME = "BlackskyOnlineSmsServerInfo.environment";

    private File serverInfoFile = new File("C:\\Users\\"+System.getProperty("user.name")+"\\"+SERVER_INFO_FILENAME);
    private static final String ACCOUNT_SID_INFO_NAME = "SID";
    private static final String ACCOUNT_TOKEN_INFO_NAME = "TOKEN";
    private static final String SERVER_OUTBOUND_PHONE_NUMBER_INFO_NAME = "OUTBOUND_PHONE_NUMBER";
    private static final String SERVER_INFO_DELIMITER = "-";

    public String ACCOUNT_SID;
    public String AUTH_TOKEN;
    public String SERVER_OUTBOUND_NUMBER;
    public List<String> verifiedPhoneNumbers = new ArrayList<>();
    public Map<String,String> verifiedPhoneNumbersInName = new HashMap<>();
    public String selectedReceiver;

    @FXML
    public void initialize() {
        messageBox.setOnKeyReleased(e-> {
            if ( !messageBox.getText().isEmpty() && !selectedReceiverNumber.getText().equals("[ N/A ]") ) {
                sendButton.setVisible(true);
            } else sendButton.setVisible(false);
            AnimationStyles.sprinkleFlyingEffect(animationRoot,1500,1,0,new int[]{1,4},Color.GREEN);
        });

        initAppProcess();
    }

    // Most important part of the program...
    @FXML
    protected void onSendButtonClick() {
        try {
            Message message = Message.creator(
                            new com.twilio.type.PhoneNumber(selectedReceiver),
                            new com.twilio.type.PhoneNumber(SERVER_OUTBOUND_NUMBER),
                            messageBox.getText())
                    .create();
            showSendingStatus(true);
        } catch ( Exception e ) {
            e.printStackTrace();
            showSendingStatus(false);
        }
        restartAllUIStage();

    }

    private void initAppProcess() {
        // If SID, AUTH_TOKEN and OUTBOUND_PHONE_NUMBER failed to set...
        if ( !setSidAndToken()  ) {
            // show failed message to ui...
            showFailedServerStatusLabel("[Error] Server configs have not been set");
            clearAvailableReceivers();
        }
        // If the app successfully connected to Twilio server...
        if ( isAbleToConnectSmsServer() ) {
            // Show server status to online...
            showServerOnlineStatus();
            syncAvailableReceivers();
        }
    }
    @FXML
    protected void onSetServerInfoClick() {
        VBox vBox = new VBox();
        vBox.getStylesheets().add(this.getClass().getResource("/com/blacksky/app/stylesheets/server-info-view.css").toExternalForm());
        vBox.setPrefWidth(550);
        vBox.setPrefHeight(250);
        vBox.setFillWidth(true);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        Scene scene = new Scene(vBox);
        Stage stage = new Stage();
        stage.setScene(scene);

        HBox sidHBox = new HBox();
        HBox authTokenBox = new HBox();
        HBox outboundNumberBox = new HBox();
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(20);
        sidHBox.setPrefHeight(30); authTokenBox.setPrefHeight(30); outboundNumberBox.setPrefHeight(30); buttonBox.setPrefHeight(35);

        sidHBox.setAlignment(Pos.CENTER);
        authTokenBox.setAlignment(Pos.CENTER);
        outboundNumberBox.setAlignment(Pos.CENTER);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(0,25,0,0));

        Label sidLabel = new Label("Account sid");
        Label authTokenLabel = new Label("Auth_token");
        Label outboundNumberLabel = new Label("Outbound phone number");
        Label errorLabel = new Label("[Warning] Please fill up all the requirements");
        errorLabel.getStyleClass().add("error-label");
        sidLabel.setPrefWidth(200); authTokenLabel.setPrefWidth(200); outboundNumberLabel.setPrefWidth(200);

        TextField sidField = new TextField();
        TextField authTokenField = new TextField();
        TextField outboundNumberField = new TextField();
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("submit-button");
        sidField.setPrefWidth(300); authTokenField.setPrefWidth(300); outboundNumberField.setPrefWidth(300);

        sidHBox.getChildren().addAll(sidLabel,sidField);
        authTokenBox.getChildren().addAll(authTokenLabel,authTokenField);
        outboundNumberBox.getChildren().addAll(outboundNumberLabel,outboundNumberField);
        buttonBox.getChildren().addAll(errorLabel,submitButton);

        vBox.getChildren().addAll(sidHBox,authTokenBox,outboundNumberBox,buttonBox);

        errorLabel.setVisible(false);
        submitButton.setOnMouseClicked(e -> {
            if ( sidField.getText().isEmpty() || authTokenField.getText().isEmpty() || outboundNumberField.getText().isEmpty() ) {
                errorLabel.setVisible(true);
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        errorLabel.setVisible(false);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                saveSidAndTokenToEnvFile(sidField.getText(),authTokenField.getText(),outboundNumberField.getText());
                initAppProcess();
                stage.close();
            }
        });


        stage.setTitle("Set server info");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void saveSidAndTokenToEnvFile(String sid,String authToken,String outboundNumber) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(serverInfoFile))) {
            bufferedWriter.write(ACCOUNT_SID_INFO_NAME+SERVER_INFO_DELIMITER+sid+"\n");
            bufferedWriter.write(ACCOUNT_TOKEN_INFO_NAME+SERVER_INFO_DELIMITER+authToken+"\n");
            bufferedWriter.write(SERVER_OUTBOUND_PHONE_NUMBER_INFO_NAME+SERVER_INFO_DELIMITER+outboundNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean setSidAndToken() {
//        File serverInfoFile = new File("C:\\Users\\"+System.getProperty("user.name")+"\\"+SERVER_INFO_FILENAME);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(serverInfoFile))) {
            String readLine = "";
            while ( (readLine = bufferedReader.readLine() ) != null ) {
                String[] readLineArray = readLine.split(SERVER_INFO_DELIMITER);
                if (ACCOUNT_SID_INFO_NAME.equals(readLineArray[0])) ACCOUNT_SID = readLineArray[1];
                if (ACCOUNT_TOKEN_INFO_NAME.equals(readLineArray[0])) AUTH_TOKEN = readLineArray[1];
                if (SERVER_OUTBOUND_PHONE_NUMBER_INFO_NAME.equals(readLineArray[0])) SERVER_OUTBOUND_NUMBER = readLineArray[1];
            }
        } catch ( Exception e ) {
            System.out.println(e.getMessage());
//            e.printStackTrace();
        }
        return ACCOUNT_SID != null && AUTH_TOKEN != null && SERVER_OUTBOUND_NUMBER != null;
    }

    private boolean isAbleToConnectSmsServer() {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            ResourceSet<OutgoingCallerId> outgoingCallerIds =
                    OutgoingCallerId.reader()
                            .limit(20)
                            .read();

            for (OutgoingCallerId record : outgoingCallerIds) {
                verifiedPhoneNumbers.add(record.getPhoneNumber().toString());
                verifiedPhoneNumbersInName.put(record.getFriendlyName(),record.getPhoneNumber().toString());
            }

            return true;
        } catch (ApiException ape) {
            showFailedServerStatusLabel("[Error] "+ape.getMessage());
            clearAvailableReceivers();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void syncAvailableReceivers() {
//        verifiedPhoneNumbers.forEach( verifyPhone -> {
//            HBox phoneWrapper = new HBox();
//            phoneWrapper.setAlignment(Pos.CENTER);
//            phoneWrapper.getStyleClass().add("receiver-numbers-wrapper");
//            Label phone = new Label(convertCountryCode(verifyPhone,"+95","0"));
//            phone.getStyleClass().add("receiver-numbers");
//            phoneWrapper.getChildren().add(phone);
//            phoneWrapper.setOnMouseClicked(e-> {
//                selectedReceiver = ((Label)((HBox)e.getSource()).getChildren().get(0)).getText();
//                selectedReceiverNumber.setText("[ "+selectedReceiver+" ]");
//                if ( messageBox.getText().isEmpty() ) {
//                    sendButton.setVisible(false);
//                } else sendButton.setVisible(true);
//            });
//            availableReceiverWrapper.getChildren().add(phoneWrapper);
//        });

        for (Map.Entry<String,String> num: verifiedPhoneNumbersInName.entrySet() ) {
            HBox phoneWrapper = new HBox();
            phoneWrapper.setAlignment(Pos.CENTER);
            phoneWrapper.getStyleClass().add("receiver-numbers-wrapper");
            Label phone = new Label(convertCountryCode(num.getKey(),"+95","0"));
            phone.getStyleClass().add("receiver-numbers");
            phoneWrapper.getChildren().add(phone);
            phoneWrapper.setOnMouseClicked(e-> {
                HBox receiverNumberWrapper = (HBox)e.getSource();
                String selectedReceiverInName = ((Label)((HBox)e.getSource()).getChildren().get(0)).getText();
                selectedReceiver = verifiedPhoneNumbersInName.get(selectedReceiverInName);
                selectedReceiverNumber.setText("[ "+convertCountryCode(selectedReceiver,"+95","0")+" ]");
                if ( messageBox.getText().isEmpty() ) {
                    sendButton.setVisible(false);
                } else sendButton.setVisible(true);
            });
            availableReceiverWrapper.getChildren().add(phoneWrapper);
        }
    }

    private void showSendingStatus(boolean isSuccess) {
        if ( isSuccess ) {
            sendingStatus.setStyle("-fx-text-fill: #0CC800");
            sendingStatus.setText("[ Success ]");
        } else {
            sendingStatus.setStyle("-fx-text-fill: red");
            sendingStatus.setText("[ Failed ]");
        }
    }
    private void makeServerStatusBulbFailed() {
        serverStatusBulb.setStyle("-fx-fill: red");
    }
    private void makeServerStatusBulbSuccess() {
        serverStatusBulb.setStyle("-fx-fill: #0CC800");
    }
    private void showFailedServerStatusLabel(String msg) {
        makeServerStatusBulbFailed();
        serverStatus.setText(msg);
        serverStatus.setStyle("-fx-text-fill: red");
    }
    private void showServerOnlineStatus() {
        makeServerStatusBulbSuccess();
        serverStatus.setText("Online");
        serverStatus.setStyle("-fx-text-fill: black");
    }
    private void clearAvailableReceivers() {
        verifiedPhoneNumbers.clear();
        availableReceiverWrapper.getChildren().removeIf( p -> p instanceof HBox);
    }
    private String convertCountryCode(String phoneNumber,String from,String to) {
        return phoneNumber.replace(from,to);
    }

    private void restartAllUIStage() {
        messageBox.clear();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
//                    initAppProcess();
                    selectedReceiverNumber.setText("[ N/A ]");
                    sendingStatus.setText("[ Waiting ]");
                    sendingStatus.setStyle("-fx-text-fill: black");
                    animationRoot.getChildren().clear();
                    selectedReceiver = selectedReceiverNumber.getText();
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}