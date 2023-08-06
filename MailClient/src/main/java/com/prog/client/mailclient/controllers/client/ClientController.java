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
    private Button btnForward;
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



    public void initializeClient(String usr) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        listEmails.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setText(null);
                } else {
                    if (sectionName.equals("inbox")) {
                        String text;
                        if (!email.isRead()) {
                            text = email.toString() + " - NEW";
                        } else {
                            text = email.toString();
                        }
                        setText(text);
                    } else {
                        setText(email.toString());
                    }
                }
            }
        });

        model = new User(usr);
        lblUsername.textProperty().bind(model.emailAddressProperty());
        sentCount.textProperty().bind(model.sentCounterProperty());
        inboxCount.textProperty().bind(model.inboxCounterProperty());
        trashedCount.textProperty().bind(model.trashedCounterProperty());
        model.emptyEmail = null;

        boolean res = allFromServer();
        if (res) {
            bindList(model.inboxProperty());
            sectionName = "inbox";
            model.selectedEmail = model.inboxProperty().get(0);
            updateDetailView(model.selectedEmail);
            inboxBtn.setStyle(" -fx-background-color: #CAC9D2;\n" + " -fx-background-radius: 5px;");
        } else {
            updateDetailView(model.emptyEmail);
            Alert a = new Alert(Alert.AlertType.INFORMATION, "No received emails to display at the moment.");
            a.show();
        }
        handleTimerLoadEmails(true);

    }



    private void bindList(ListProperty<Email> list) {
        listEmails.itemsProperty().bind(list);
        listEmails.getSelectionModel().selectFirst();
        listEmails.setOnMouseClicked(this::showSelectedEmail);
    }

    @FXML
    private void showInbox(ActionEvent event) {
        if (model.inboxProperty().size() > 0) {
            sectionName = "inbox";
            bindList(model.inboxProperty());
            model.selectedEmail = model.inboxProperty().get(0);
            updateDetailView(model.selectedEmail);
            activeBtnHandle(sectionName);
        }
    }

    @FXML
    private void showSent(ActionEvent event) {
        if (model.sentProperty().size() > 0) {
            sectionName = "sent";
            bindList(model.sentProperty());
            model.selectedEmail = model.sentProperty().get(0);
            updateDetailView(model.selectedEmail);
            activeBtnHandle("sent");
        }
    }

    @FXML
    private void showTrashed(ActionEvent event) {
        if (model.trashedProperty().size() > 0) {
            sectionName = "trashed";
            model.selectedEmail = model.trashedProperty().get(0);
            bindList(model.trashedProperty());
            updateDetailView(model.selectedEmail);
            activeBtnHandle(sectionName);
        }
    }

    private void activeBtnHandle(String section) {
        switch (section) {
            case "inbox" -> {
                inboxBtn.setStyle(" -fx-background-color: #CAC9D2;\n" +
                        " -fx-background-radius: 5px;");
                sentBtn.setStyle(null);
                trashedBtn.setStyle(null);
            }
            case "sent" -> {
                sentBtn.setStyle(" -fx-background-color: #CAC9D2;\n" +
                                 " -fx-background-radius: 5px;");
                inboxBtn.setStyle(null);
                trashedBtn.setStyle(null);
            }
            case "trashed" -> {
                trashedBtn.setStyle(" -fx-background-color: #CAC9D2;\n" +
                        " -fx-background-radius: 5px;");
                inboxBtn.setStyle(null);
                sentBtn.setStyle(null);
            }
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/prog/client/mailclient/newEmail.fxml"));
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

    private boolean checkUnreadMails() {
        boolean unread = false;
        for (Email e: model.inboxProperty()) {
            if (!e.isRead()) {
                unread = true;
                break;
            }
        }
        return unread;
    }

    @FXML
    void showSelectedEmail(MouseEvent mouseEvent) {
        Email email = listEmails.getSelectionModel().getSelectedItem();
        model.selectedEmail = email;
        updateDetailView(email);

        /*  case inbox => check if read or not   */
        if (sectionName.equals("inbox")) {
            if (!model.selectedEmail.isRead()) {
                model.selectedEmail.setRead(true);
                Request request = new Request(
                        "setRead",
                        model.emailAddressProperty().get(),
                        new SerializableEmail(model.selectedEmail)
                );

                openConnection();
                sendEmail(request);
                try {
                    Response response = getServerResponse();
                    System.out.println(response);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    closeConnection();
                }
            }
            if (!checkUnreadMails()) {
                Platform.runLater(() -> txtNewInbox.setVisible(false));
            }

        }
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
    protected boolean allFromServer() {
        boolean response = false;
        try {
            Request request;
            int newInbox = 0;
            request = new Request(
                    "getAll",
                    model.emailAddressProperty().get()
            );

            openConnection();
            sendEmail(request);
            Response res = getServerResponse();
            if (res.isSuccess()) {
                ArrayList<Email> inbox = new ArrayList<>();
                ArrayList<Email> sent = new ArrayList<>();
                ArrayList<Email> trashed = new ArrayList<>();
                if (!res.getInbox().isEmpty()) {
                    response = true;
                    System.out.println(res.getInbox().size());
                    System.out.println(res.getInbox());
                    for (SerializableEmail sEmail : res.getInbox()) {
                        Email e = new Email(sEmail);
                        inbox.add(e);
                    }
                    for (Email e : inbox) {
                        System.out.println(e.isRead());
                        if (!e.isRead()) {
                            newInbox++;
                        }
                        model.inboxProperty().add(e);
                    }
                }
                if (!res.getSent().isEmpty()) {
                    for (SerializableEmail sEmail : res.getSent()) {
                        Email e = new Email(sEmail);
                        sent.add(e);
                    }
                    for (Email e : sent) {
                        model.sentProperty().add(e);
                    }
                }
                if (!res.getTrashed().isEmpty()) {
                    for (SerializableEmail sEmail : res.getTrashed()) {
                        Email e = new Email(sEmail);
                        trashed.add(e);
                    }
                    for (Email e : trashed) {
                        model.trashedProperty().add(e);
                    }
                }
                final int newMail = newInbox;
                Platform.runLater(() -> {
                    model.setTrashedCounter(model.trashedProperty().size());
                    model.setSentCounter(model.sentProperty().size());
                    model.setInboxCounter(model.inboxProperty().size());
                    newMailHandler(newMail);
                });
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR, res.getMessage());
                a.show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            closeConnection();
        }
        return response;
    }
    protected void updateFromServer() {
        try {

            Request request;
            int newInbox = 0;
            long lastInbox = model.inboxProperty().isEmpty() ? 0 : model.inboxProperty().get(model.inboxProperty().size() - 1).getIdEmail();

            request = new Request(
                    "update",
                    model.emailAddressProperty().get(),
                    lastInbox
            );
            openConnection();
            sendEmail(request);
            Response res = getServerResponse();
            if (res.isSuccess()) {
                ArrayList<Email> inbox = new ArrayList<>();
                if (res.getInbox() != null) { /* there are new emails */
                    for (SerializableEmail sEmail : res.getInbox()) {
                        newInbox = newInbox +1 ;
                        Email e = new Email(sEmail);
                        inbox.add(e);
                    }
                    for (Email e : inbox) {
                        Platform.runLater(() -> model.inboxProperty().add(e));
                    }
                }
                final int newMail = newInbox;
                Platform.runLater(() -> {
                    model.setTrashedCounter(model.trashedProperty().size());
                    model.setSentCounter(model.sentProperty().size());
                    model.setInboxCounter(model.inboxProperty().size());
                    newMailHandler(newMail);
                });
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR, res.getMessage());
                a.show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            closeConnection();
        }
    }

    private void newMailHandler(int newInbox) {
        if (newInbox >= 1 && !txtNewInbox.isVisible()) {
            txtNewInbox.setVisible(true);
        }
    }

    public void handleTimerLoadEmails(boolean open) {
        if (open && scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(new emailDownload(), 5, 5, TimeUnit.SECONDS);
            System.out.println("ok schedule timer");
        } else if (!open && scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    class emailDownload implements Runnable {
        public emailDownload() {}
        @Override
        public void run() {
            updateFromServer();
        }
    }

}
