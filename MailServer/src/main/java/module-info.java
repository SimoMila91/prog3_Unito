module com.prog.server.mailserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires com.prog.client.mailclient;


    opens com.prog.server.mailserver to javafx.fxml;
    opens com.prog.server.mailserver.controller to javafx.fxml;
    exports com.prog.server.mailserver;
    exports com.prog.server.mailserver.controller;
    exports com.prog.server.mailserver.model;
}