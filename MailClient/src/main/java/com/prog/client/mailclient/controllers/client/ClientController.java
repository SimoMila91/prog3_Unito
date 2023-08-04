package com.prog.client.mailclient.controllers.client;

import com.prog.client.mailclient.models.Email;
import com.prog.client.mailclient.models.User;
import com.prog.client.mailclient.utils.Request;
import com.prog.client.mailclient.utils.Response;
import com.prog.client.mailclient.utils.SerializableEmail;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientController {

    private Socket socket;
    private ScheduledExecutorService scheduledExecutorService;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    @FXML
    private Label lblFrom;
    @FXML
    private Label lblTo;
    @FXML
    private Label lblUsername;
    @FXML
    private Label lblSubject;
    @FXML
    private TextArea txtEmail;
    @FXML
    private Button btnReply;
    @FXML
    private Button btnReplyAll;
    @FXML
    private Button btnDelete;
    @FXML
    private ListView<Email> listEmails;
    @FXML
    private Button inboxBtn;
    @FXML
    private Button sentBtn;
    @FXML
    private Button trashedBtn;
    @FXML
    private Text inboxCount;
    @FXML
    private Text sentCount;
    @FXML
    private Text trashedCount;
    @FXML
    private Text txtNewInbox;

    public User model;
    protected Stage newStage;

    private String sectionName = "inbox";

    private int newInbox = 0;


    public void initializeClient(String usr) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        // new user
        model = new User(usr);


        lblUsername.textProperty().bind(model.emailAddressProperty());
        bindList(model.inboxProperty());

        model.emptyEmail = null;
        if (model.inboxProperty().size() > 0) {
            sectionName = "inbox";
            model.selectedEmail = model.inboxProperty().get(0);
            updateDetailView(model.selectedEmail);
            inboxBtn.setDisable(true);
        } else {
            updateDetailView(model.emptyEmail);
        }
        handleTimerLoadEmails(true);
    }

    private void bindList(ListProperty<Email> list) {
        listEmails.itemsProperty().bind(list);
        listEmails.setOnMouseClicked(this::showSelectedEmail);
    }

    @FXML
    private void showInbox(ActionEvent event) {
        if (model.inboxProperty().size() > 0) {
            sectionName = "inbox";
            bindList(model.inboxProperty());
            model.selectedEmail = model.inboxProperty().get(0);
            updateDetailView(model.selectedEmail);
            inboxBtn.setDisable(true);
            sentBtn.setDisable(false);
            trashedBtn.setDisable(false);
        }
    }

    @FXML
    private void showSent(ActionEvent event) {
        if (model.sentProperty().size() > 0) {
            sectionName = "sent";
            bindList(model.sentProperty());
            model.selectedEmail = model.sentProperty().get(0);
            updateDetailView(model.selectedEmail);
            inboxBtn.setDisable(false);
            sentBtn.setDisable(true);
            trashedBtn.setDisable(false);
        }
    }

    @FXML
    private void showTrashed(ActionEvent event) {
        if (model.trashedProperty().size() > 0) {
            sectionName = "trashed";
            model.selectedEmail = model.trashedProperty().get(0);
            bindList(model.trashedProperty());
            updateDetailView(model.selectedEmail);
            inboxBtn.setDisable(false);
            sentBtn.setDisable(false);
            trashedBtn.setDisable(true);
        }
    }

    @FXML
    public void handleNewEmail(ActionEvent event) {
        try {
            handleNewStage("new");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    void onReplyBtnClick(ActionEvent event) {
        try {
            handleNewStage("reply");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onReplyAllBtnClick(ActionEvent event) {
        try {
            handleNewStage("replyAll");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onForwardBtnClick(ActionEvent event) {
        try {
            handleNewStage("forward");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNewStage(String btnClicked) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newEmail.fxml"));
        Parent root = loader.load();
        NewEmailController newController = loader.getController();
        newController.initializeNewController(model, this, btnClicked); //passing controller and model
        newStage = new Stage();
        Scene scene = new Scene(root);
        newStage.setScene(scene);
        newStage.setTitle("New Email");
        newStage.show();
    }

    @FXML
    public void onDeleteBtnClick(ActionEvent event) {
        try {
            openConnection();
            SerializableEmail email = new SerializableEmail(
                    model.selectedEmail
            );
            Request request = new Request("delete", model.emailAddressProperty().get(), email, sectionName); // the server should know in which section delete the email because could be in inbox and sent
            sendEmail(request);
            Response response = getServerResponse();
            if (response.isSuccess()) {
                switch (sectionName) {
                    case "inbox" -> {
                        model.deleteInbox(model.selectedEmail);
                        model.selectedEmail.setIdEmail(response.getIdEmail());
                        Platform.runLater(() -> model.trashedProperty().add(model.selectedEmail)); // add inbox email to trashed
                        if (model.inboxProperty().size() > 0) {
                            model.selectedEmail = model.inboxProperty().get(0);
                            updateDetailView(model.selectedEmail);
                        }
                    }
                    case "sent" -> {
                        model.deleteSent(model.selectedEmail);
                        model.selectedEmail.setIdEmail(response.getIdEmail());
                        Platform.runLater(() -> model.trashedProperty().add(model.selectedEmail));  // add sent email to trashed
                        if (model.sentProperty().size() > 0) {
                            model.selectedEmail = model.sentProperty().get(0);
                            updateDetailView(model.selectedEmail);
                        }
                    }
                    case "trashed" -> {
                        model.deleteTrashed(model.selectedEmail);
                        if (model.trashedProperty().size() > 0) {
                            model.selectedEmail = model.trashedProperty().get(0);
                            updateDetailView(model.selectedEmail);
                        }
                    }
                }

                if (model.sentProperty().size() == 0 && model.trashedProperty().size() == 0 && model.inboxProperty().size() == 0)
                    updateDetailView(model.emptyEmail);
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR, response.getMessage());
                a.show();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        handleTimerLoadEmails(false);
        stage.close();
    }

    @FXML
    void showSelectedEmail(MouseEvent mouseEvent) {
        Email email = listEmails.getSelectionModel().getSelectedItem();
        model.selectedEmail = email;
        if (!model.selectedEmail.isRead()) {
            model.selectedEmail.setRead(true);
            newInbox--;
            Request request = new Request(
                    "setRead",
                    model.emailAddressProperty().get(),
                    new SerializableEmail(model.selectedEmail)
            );
            openConnection();
            sendEmail(request);
            try {
                Response response = getServerResponse();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                closeConnection();
            }
            if (newInbox > 0) {
                Platform.runLater(() -> txtNewInbox.setText("New -> " +  String.valueOf(newInbox)));
            } else {
                Platform.runLater(() -> txtNewInbox.setText(""));
            }

        }
        updateDetailView(email);
    }

    public void updateDetailView(Email email) {
        if(email != null) {
            lblFrom.setText(email.getSender().get());
            lblTo.setText(email.getReceiver().get());
            lblSubject.setText(email.getSubject().get());
            txtEmail.setText(email.getBody().get());
        }
    }

    protected void openConnection() {
        try {
            socket = new Socket(InetAddress.getLocalHost(), 9000);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    protected void closeConnection() {
        if (socket != null && objectOutputStream != null && objectInputStream != null) {
            try {
                socket.close();
                objectInputStream.close();
                objectOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendEmail(Request req) {
        if (objectOutputStream == null) return;
        try {
            objectOutputStream.writeObject(req);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Response getServerResponse() throws IOException, ClassNotFoundException {
        return (Response) objectInputStream.readObject();
    }

    /*
    *   This method is automatic ty to the Runnable
    *
    *   I made 2 case:
    *       - getAll, will make a request to upload everything from the server
    *       - update, will make a request to upload new emails
    *
    *   Sent email or trashed will be added from the client when an email is sent or deleted.
    *   The server will be notified.
    *
     */
    protected void allFromServer() {
        try {
            Request request;
            long lastInbox;

            if (model.inboxProperty().isEmpty() && model.sentProperty().isEmpty() && model.trashedProperty().isEmpty()) {
                request = new Request(
                        "getAll",
                        model.emailAddressProperty().get()
                );
            } else {
                lastInbox = model.inboxProperty().get(model.inboxProperty().size() - 1).getIdEmail();

                request = new Request(
                        "update",
                        model.emailAddressProperty().get(),
                        lastInbox
                );
            }

            openConnection();
            sendEmail(request);
            Response res = getServerResponse();
            if (res.isSuccess()) {

                if (res.getMessage().equals("getAll")) {
                    ArrayList<Email> inbox = new ArrayList<>();
                    ArrayList<Email> sent = new ArrayList<>();
                    ArrayList<Email> trashed = new ArrayList<>();
                    if (!res.getInbox().isEmpty()) {
                        for (SerializableEmail sEmail : res.getInbox()) {
                            Email e = new Email(sEmail);
                            inbox.add(e);
                        }
                        for (Email e : inbox) {
                            if (model.checkDuplicates(model.inboxProperty(), e) && !e.isRead()) {
                                newInbox++;
                                Platform.runLater(() -> model.inboxProperty().add(e));
                            }
                        }
                    }
                    if (!res.getSent().isEmpty()) {
                        for (SerializableEmail sEmail : res.getSent()) {
                            Email e = new Email(sEmail);
                            sent.add(e);
                        }
                        for (Email e : sent) {
                            if (model.checkDuplicates(model.sentProperty(), e)) {
                                Platform.runLater(() -> model.sentProperty().add(e));
                            }
                        }
                    }
                    if (!res.getTrashed().isEmpty()) {
                        for (SerializableEmail sEmail : res.getTrashed()) {
                            Email e = new Email(sEmail);
                            trashed.add(e);
                        }
                        for (Email e : trashed) {
                            if (model.checkDuplicates(model.trashedProperty(), e)) {
                                Platform.runLater(() -> model.trashedProperty().add(e));
                            }
                        }
                    }

                    if (newInbox > 0) {
                        Platform.runLater(() ->   txtNewInbox.setText("New -> " + String.valueOf(newInbox)));
                    } else {
                        Platform.runLater(() ->   txtNewInbox.setText(""));
                    }

                }

                if (res.getMessage().equals("update")) {
                    ArrayList<Email> inbox = new ArrayList<>();
                    if (res.getInbox() != null) { /* there are new emails */
                        for (SerializableEmail sEmail : res.getInbox()) {
                            newInbox++;
                            Email e = new Email(sEmail);
                            inbox.add(e);
                        }
                        for (Email e : inbox) {
                            Platform.runLater(() -> model.inboxProperty().add(e));
                        }

                        if (newInbox > 0) {
                            Platform.runLater(() ->   txtNewInbox.setText("New -> " + String.valueOf(newInbox)));
                        } else {
                            Platform.runLater(() ->   txtNewInbox.setText(""));
                        }
                    }
                }
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR, res.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            closeConnection();
        }
    }

    public void handleTimerLoadEmails(boolean open) {
        if (open && scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(new emailDownload(), 0, 5, TimeUnit.SECONDS);
            System.out.println("ok schedule timer");
        } else if (!open && scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    class emailDownload implements Runnable {
        public emailDownload() {}
        @Override
        public void run() {
            allFromServer();
        }
    }

}
