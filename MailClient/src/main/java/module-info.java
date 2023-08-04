module com.prog.client.mailclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens com.prog.client.mailclient to javafx.fxml;
    opens com.prog.client.mailclient.controllers to javafx.fxml;
    opens com.prog.client.mailclient.controllers.client to javafx.fxml;
    exports com.prog.client.mailclient;
    exports com.prog.client.mailclient.controllers;
    exports com.prog.client.mailclient.controllers.client;
    exports com.prog.client.mailclient.models;
    exports com.prog.client.mailclient.utils;
}