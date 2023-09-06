package com.prog.server.mailserver.model;

import com.prog.client.mailclient.utils.SerializableEmail;
import javafx.application.Platform;

import javafx.beans.property.SimpleStringProperty;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Server {
    private final SimpleStringProperty logs;
    static final String JSONClient = "./MailServer/src/main/resources/com/prog/server/mailserver/database/clientEmail.json";
    private boolean running = true;
    ArrayList<User> users;

    private final String[] sectionNames = { "inbox", "sent", "trashed" };

    public Server() {
        users = new ArrayList<>();
        logs = new SimpleStringProperty("");
    }

    public SimpleStringProperty logProperty() {
        return logs;
    }
    public boolean getRunning() {
        return running;
    }

    // open - close server
    public void handleServerStatus(boolean x) {
        this.running = x;
    }

    // request from client
    public synchronized void addRequest(String request) {
        String currentText = logs.getValue();
        String newText = request + currentText;
        Platform.runLater(() -> logs.setValue(newText));
    }

    public ArrayList<SerializableEmail> getNewInbox(User user, long lastFromClient) {

        ArrayList<SerializableEmail> res = user.getInbox();

        return res.stream()
                .filter(email -> email.getIdEmail() > lastFromClient)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean checkLastInbox(User user, long lastFromClient) {
        boolean res = true;
        ArrayList<SerializableEmail> inbox = user.getInbox();
        for (SerializableEmail serializableEmail : inbox) {
            if (serializableEmail.getIdEmail() > lastFromClient) {
                res = false;
                break;
            }
        }
        return res;
    }

    public User getUser(String email) {
        User user = null;
        boolean found = false;
        for (int i = 0; i < users.size() && !found; i++) {
            if (users.get(i).getEmailAddress().equals(email)) {
                user = users.get(i);
                found = true;
            }
        }
        return user;
    }

    @SuppressWarnings("All")
    public void saveToDatabase() throws IOException {
        JSONParser parser = new JSONParser();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        JSONArray fileToSave = new JSONArray();

        for (User usr : users) {
            JSONObject accountObject = new JSONObject();
            accountObject.put("email", usr.getEmailAddress());

            JSONArray inboxArray = new JSONArray();
            for (SerializableEmail email : usr.getInbox()) {
                JSONObject emailObject = new JSONObject();
                emailObject.put("idEmail", email.getIdEmail());
                emailObject.put("sender", email.getSender());
                emailObject.put("receiver", email.getReceiver());
                emailObject.put("subject", email.getSubject());
                emailObject.put("body", email.getBody());
                emailObject.put("dateTime", email.getDateTime().format(formatter));
                emailObject.put("isRead", email.isRead());
                inboxArray.add(emailObject);
            }
            accountObject.put("inbox", inboxArray);

            JSONArray sentArray = new JSONArray();
            for (SerializableEmail email : usr.getSent()) {
                JSONObject emailObject = new JSONObject();
                emailObject.put("idEmail", email.getIdEmail());
                emailObject.put("sender", email.getSender());
                emailObject.put("receiver", email.getReceiver());
                emailObject.put("subject", email.getSubject());
                emailObject.put("body", email.getBody());
                emailObject.put("dateTime", email.getDateTime().format(formatter));
                emailObject.put("isRead", email.isRead());
                sentArray.add(emailObject);
            }
            accountObject.put("sent", sentArray);

            JSONArray trashedArray = new JSONArray();
            for (SerializableEmail email : usr.getTrashed()) {
                JSONObject emailObject = new JSONObject();
                emailObject.put("idEmail", email.getIdEmail());
                emailObject.put("sender", email.getSender());
                emailObject.put("receiver", email.getReceiver());
                emailObject.put("subject", email.getSubject());
                emailObject.put("body", email.getBody());
                emailObject.put("dateTime", email.getDateTime().format(formatter));
                emailObject.put("isRead", email.isRead());
                trashedArray.add(emailObject);
            }
            accountObject.put("trashed", trashedArray);

            fileToSave.add(accountObject);

            try {
                File file = new File(JSONClient);
                PrintWriter out = new PrintWriter(file);
                synchronized (out) {
                    try {
                        out.write(fileToSave.toJSONString());
                        out.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        out.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("All")
    public void readFromDatabase() throws IOException {
        users = new ArrayList<>();

        String fileToRead = new String(Files.readAllBytes(Paths.get(JSONClient)));

        JSONParser parser = new JSONParser();
        try {
            Object object = parser.parse(fileToRead);
            JSONArray accountsJson = (JSONArray) object;
            for (int i = 0; i < accountsJson.size(); i++) {

                JSONObject account = (JSONObject) accountsJson.get(i);
                String emailAddress = (String) account.get("email");
                User user = new User(emailAddress);
                users.add(user);

                JSONArray inbox = (JSONArray) account.get("inbox");
                JSONArray sent = (JSONArray) account.get("sent");
                JSONArray trashed = (JSONArray) account.get("trashed");
                ArrayList<SerializableEmail> inboxEmails = new ArrayList<>();
                ArrayList<SerializableEmail> sentEmails = new ArrayList<>();
                ArrayList<SerializableEmail> trashedEmails = new ArrayList<>();

                if (inbox.size() > 0) {
                    for (int j = 0; j < inbox.size(); j++) {
                        JSONObject mailJson = (JSONObject) inbox.get(j);
                        long idEmail = (long) mailJson.get("idEmail");
                        String sender = (String) mailJson.get("sender");
                        String receiver = (String) mailJson.get("receiver");
                        String subject = (String) mailJson.get("subject");
                        String txtBody = (String) mailJson.get("body");
                        String date = (String) mailJson.get("dateTime");
                        boolean read = (boolean) mailJson.get("isRead");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        SerializableEmail email = new SerializableEmail(idEmail, sender, receiver, subject, txtBody, LocalDateTime.parse(date, formatter), read);
                        inboxEmails.add(email);
                    }
                }

                if (sent.size() > 0) {
                    for (int j = 0; j < sent.size(); j++) {
                        JSONObject mailJson = (JSONObject) sent.get(j);
                        long idEmail = (long) mailJson.get("idEmail");
                        String sender = (String) mailJson.get("sender");
                        String receiver = (String) mailJson.get("receiver");
                        String subject = (String) mailJson.get("subject");
                        String txtBody = (String) mailJson.get("body");
                        String date = (String) mailJson.get("dateTime");
                        boolean read = (boolean) mailJson.get("isRead");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        SerializableEmail email = new SerializableEmail(idEmail, sender, receiver, subject, txtBody, LocalDateTime.parse(date, formatter), read);
                        sentEmails.add(email);
                    }
                }
               if (trashed.size() > 0) {
                   for (int j = 0; j < trashed.size(); j++) {
                       JSONObject mailJson = (JSONObject) trashed.get(j);
                       long idEmail = (long) mailJson.get("idEmail");
                       String sender = (String) mailJson.get("sender");
                       String receiver = (String) mailJson.get("receiver");
                       String subject = (String) mailJson.get("subject");
                       String txtBody = (String) mailJson.get("body");
                       String date = (String) mailJson.get("dateTime");
                       boolean read = (boolean) mailJson.get("isRead");
                       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                       SerializableEmail email = new SerializableEmail(idEmail, sender, receiver, subject, txtBody, LocalDateTime.parse(date, formatter), read);
                       trashedEmails.add(email);
                   }
               }
                user.setInbox(inboxEmails);
                user.setSent(sentEmails);
                user.setTrashed(trashedEmails);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
