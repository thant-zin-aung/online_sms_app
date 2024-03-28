module com.example.online_sms_sender {
    requires javafx.controls;
    requires javafx.fxml;
    requires twilio;


    opens com.blacksky.app to javafx.fxml;
    exports com.blacksky.app;
    exports com.blacksky.app.controller;
    opens com.blacksky.app.controller to javafx.fxml;
}