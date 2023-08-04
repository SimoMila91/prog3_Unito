package com.prog.client.mailclient.controllers;

import com.prog.client.mailclient.controllers.client.ClientController;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

public class LoginController {

    private String userSelected;
    private final List<String> users = new ArrayList<>();
    protected ListProperty<String> listProperty = new SimpleListProperty<>();
    @FXML
    public ListView<String> accSelector;
    @FXML
    public Button loginBtn;

    @FXML
    public void initialize() {
        loadUser();
    }

    private void loadUser() {
        try {
            String data = new String(Files.readAllBytes(Paths.get("./MailClient/login.json")));

            JSONArray jsonArray = new JSONArray(data);

            for (int i = 0; i < jsonArray.length(); i++) {
                String str = jsonArray.get(i).toString();

                JSONObject user = new JSONObject(str);
                String email = user.getString("email");

                users.add(email);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage() + " ERRORE LOAD USER");
            throw new RuntimeException(e);
        }


        accSelector.itemsProperty().bind(listProperty);
        listProperty.set(FXCollections.observableArrayList(users));
        accSelector.setOnMouseClicked(this::handleSelectedItem);
    }

    protected void handleSelectedItem(MouseEvent event) {
        userSelected = accSelector.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void login(ActionEvent e) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("/com/prog/client/mailclient/clientView.fxml"));
        Stage stage = (Stage) loginBtn.getScene().getWindow();
        Scene scene = new Scene(root.load());
        ClientController user = root.getController();
        stage.setTitle("NerdMail");
        stage.setScene(scene);
        user.initializeClient(userSelected);
        stage.setOnCloseRequest((windowEvent -> user.handleTimerLoadEmails(false)));
        stage.show();

    }


}
