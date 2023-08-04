package com.prog.server.mailserver;

import com.prog.server.mailserver.controller.MailServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MailServer.fxml"));
        Scene scene = new Scene(loader.load());
        MailServerController controller = loader.getController();
        primaryStage.setTitle("NerdMail Server");
        primaryStage.setOnCloseRequest(e -> controller.stopServer());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
