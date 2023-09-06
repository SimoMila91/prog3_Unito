package com.prog.server.mailserver.controller;

import com.prog.client.mailclient.models.Email;
import com.prog.client.mailclient.utils.SerializableEmail;
import com.prog.server.mailserver.model.Server;

import com.prog.client.mailclient.utils.Request;
import com.prog.client.mailclient.utils.Response;
import com.prog.server.mailserver.model.User;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MailServerController {

    private static final int THREAD_NUMBER = 6;


    private ServerSocket serverSocket;
    private Server server;
    private ExecutorService fixedThreadPool;


    @FXML
    private TextArea logsTxt;
    @FXML
    private Button startBtn;
    @FXML
    private Button stopBtn;

    @FXML
    public void initialize() {
        // server error
        if (this.server != null) throw new IllegalStateException("Server can only be initialized once");
        this.server = new Server();
        startBtn.setDisable(true);
        logsTxt.textProperty().bind(server.logProperty()); // binding server messages
        logsTxt.setWrapText(true);
        startServer();
    }

    @FXML
    public void stopServer() {
        System.out.println("Stop server");
        server.handleServerStatus(false);
        fixedThreadPool.shutdown();
        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Stop server exception");
        }
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
    }

    @FXML
    public void startServer() {
        try {
            serverSocket = new ServerSocket(9000);
            server.readFromDatabase();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        fixedThreadPool = Executors.newFixedThreadPool(THREAD_NUMBER);

        new Thread(() -> {
            while(server.getRunning()) {
                try {
                    Socket requestSocket = serverSocket.accept();
                    ServerRunnable sr = new ServerRunnable(requestSocket);
                    fixedThreadPool.execute(sr); 
                } catch (SocketException s) {
                    System.out.println("Socket Closing");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        startBtn.setDisable(true);
        stopBtn.setDisable(false);
    }

    // runnable class to serve call from client

    class ServerRunnable implements Runnable {
        Socket sockets;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        boolean isInitialized = false;

        public ServerRunnable(Socket sockets) {
            this.sockets = sockets;
            try {
                objectOutputStream = new ObjectOutputStream(sockets.getOutputStream());
                objectInputStream = new ObjectInputStream(sockets.getInputStream());
                isInitialized = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (!isInitialized) return;
            try {
                Request request = (Request) objectInputStream.readObject();
                Response response;
                switch (request.getRequest()) {
                    case "getAll" -> {
                        synchronized (logsTxt) {
                            server.addRequest("Incoming request from " + request.getEmailAddress()  + " : First alignment\n\n");
                        }
                        response = (Response) sendAllEmails(request.getEmailAddress());
                        System.out.println(response);
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    }
                    case "update" -> {
                        synchronized (logsTxt) {
                            server.addRequest("Incoming request from " + request.getEmailAddress()  + ": Update mail client...\n\n");
                        }
                        response = (Response) updateClient(request);
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    }
                    case "submit" -> {
                        synchronized (logsTxt) {
                            server.addRequest("Incoming request from " + request.getEmailAddress()  + ": sending email to " + request.getEmail().getReceiver() + "\n\n");
                        }
                        response = sendEmail(request);
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    }
                    case "delete" -> {
                        response = deleteEmail(request);
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    }
                    case "setRead" -> {
                        response = setEmailRead(request);
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    }
                }
                synchronized (logsTxt) {
                    server.addRequest("Request from " + request.getEmailAddress() + " successfully terminated\n" +
                            "\n");
                }

            } catch (Exception ex) {
                synchronized (logsTxt) {
                    server.addRequest("Error trying to serve the request " + sockets.getInetAddress().getHostName() + "\n\n");
                }
                ex.printStackTrace();
            } finally {
                try {
                    objectOutputStream.close();
                    objectInputStream.close();
                    sockets.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * in case of update I'm going to check if there are new Emails to send.
         * lastInbox >= all inbox emails ? false : true
         */

        private Response updateClient(Request request) {

            long lastInbox = request.getLastInbox();
            User user = server.getUser(request.getEmailAddress());

            boolean isLast = server.checkLastInbox(user, lastInbox);

            if (!isLast) {
                ArrayList<SerializableEmail> newInbox = server.getNewInbox(user, lastInbox);
                return new Response(true, "update", newInbox);
            } else {
                return new Response(true, "update");
            }
        }

        private Response setEmailRead(Request request) {
            try {
                SerializableEmail email = (SerializableEmail) request.getEmail();

                User user = server.getUser(request.getEmailAddress());

                boolean found = false;
                for (int i = 0; i < user.getInbox().size() && !found; i++) {
                    if (user.getInbox().get(i).getIdEmail() ==  email.getIdEmail()) {
                        user.getInbox().get(i).setRead(true);
                        found = true;
                    }
                }
                server.saveToDatabase();
                server.readFromDatabase();
                return new Response(true, "Email set as read");
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Internal server error");
            }
        }

        public Response sendAllEmails(String emailAddress) {
            Response response = null;
            User user = server.getUser(emailAddress);
            if (user == null) {
                System.out.println("è null");
            }
            if (user != null) {
                response = new Response(
                        true,
                        "getAll",
                        user.getInbox(),
                        user.getSent(),
                        user.getTrashed()
                );
            } else {
                response = new Response(
                        false,
                        "User not found, try again"
                );
            }
            return response;
        }

        private Response sendEmail(Request request) {
            SerializableEmail email = (SerializableEmail) request.getEmail();
            long newIdSent = 0;

            String[] users = email.getReceiver().split(",");
            ArrayList<User> receivers = new ArrayList<>();

            for (String user : users) {
                User usr = server.getUser(user.trim());
                if (usr == null) {
                    return new Response(false, "Account " + user + " not found");
                }
                receivers.add(usr);
            }

            for (User usr : receivers) {
                email.setIdEmail(createNewId(usr.getInbox()));
                usr.getInbox().add(email);
            }
            User sender = server.getUser(email.getSender());
            newIdSent = createNewId(sender.getSent());
            sender.getSent().add(email);
            try {
                server.saveToDatabase();
                server.readFromDatabase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new Response(true, "Email successfully sent", newIdSent);
        }

        private Response deleteEmail(Request request) {
            try {
                String message;
                long idTrashed = 0; // in case of sent or inbox we have to add email in trashed, so I'll return idTrashed to client
                SerializableEmail emailToDelete = (SerializableEmail) request.getEmail();
                synchronized (logsTxt) {
                    server.addRequest("Incoming request from " + request.getEmailAddress()  + ": Deleting email with id " + emailToDelete.getIdEmail() + "\n\n");
                }
                User user = server.getUser(request.getEmailAddress());
                String section = request.getSection();
                message = "Email successfully deleted";
                if (section.equals("inbox")) {
                    for (int i = 0; i < user.getInbox().size(); i++) {
                        if (user.getInbox().get(i).getIdEmail() == emailToDelete.getIdEmail()) {
                            user.getInbox().remove(i);
                        }
                    }
                }
                if (section.equals("sent")) {
                    for (int i = 0; i < user.getSent().size(); i++) {
                        if (user.getSent().get(i).getIdEmail() == emailToDelete.getIdEmail()) {
                            user.getSent().remove(i);
                        }
                    }
                }
                if (section.equals("trashed")) {
                    for (int i = 0; i < user.getTrashed().size(); i++) {
                        if (user.getTrashed().get(i).getIdEmail() == emailToDelete.getIdEmail()) {
                            user.getTrashed().remove(i);
                        }
                    }
                    message = "Email successfully deleted from trashed";

                }
                if (section.equals("inbox") || section.equals("sent")) {
                    idTrashed = createNewId(user.getTrashed());
                    emailToDelete.setIdEmail(idTrashed); // email could be both in send and inbox so change id for trashed
                    user.getTrashed().add(emailToDelete);
                    message = "Email added to trashed";
                }
                try {
                    server.saveToDatabase();
                    server.readFromDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (section.equals("inbox") || section.equals("sent")) {
                    return new Response(true, message, idTrashed);
                } else {
                    return new Response(true, message);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return new Response(false, "Internal server error. Please try later");
            }
        }

        private long createNewId(ArrayList<SerializableEmail> list) {
            long id = 0;
            for (SerializableEmail email : list) {
                id = Math.max(email.getIdEmail(), id);
            }
            id++;
            return id;
        }

    }
}
