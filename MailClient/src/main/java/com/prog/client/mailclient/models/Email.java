package com.prog.client.mailclient.models;

import com.prog.client.mailclient.utils.SerializableEmail;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Email implements Serializable {

    private long idEmail;
    private final StringProperty sender;
    private final StringProperty receiver;
    private final StringProperty subject;
    private final StringProperty body;
    private LocalDateTime dateTime;
    private boolean isRead;


    public Email(long idEmail, String sender, String receiver, String subject, String body, LocalDateTime dateTime, boolean isRead) {
        this.idEmail = idEmail;
        this.sender = new SimpleStringProperty(sender);
        this.receiver = new SimpleStringProperty(receiver);
        this.subject = new SimpleStringProperty(subject);
        this.body = new SimpleStringProperty(body);
        this.dateTime = dateTime;
        this.isRead = isRead;
    }

    public Email(long idEmail, String sender) {
        this.idEmail = idEmail;
        this.sender = new SimpleStringProperty(sender);
        this.receiver = new SimpleStringProperty("");
        this.subject = new SimpleStringProperty("");
        this.body = new SimpleStringProperty("");
        this.dateTime = LocalDateTime.now();
        this.isRead = false;
    }

    public Email(SerializableEmail email) {
        this.idEmail = email.getIdEmail();
        this.sender = new SimpleStringProperty(email.getSender());
        this.receiver = new SimpleStringProperty(email.getReceiver());
        this.subject = new SimpleStringProperty(email.getSubject());
        this.body = new SimpleStringProperty(email.getBody());
        this.dateTime = email.getDateTime();
        this.isRead = email.isRead();
    }

    public long getIdEmail() {
        return idEmail;
    }

    public void setIdEmail(long idEmail) {
        this.idEmail = idEmail;
    }

    public StringProperty getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender.set(sender);
    }

    public StringProperty getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver.set(receiver);
    }

    public StringProperty getSubject() { return subject; }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public StringProperty getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body.set(body);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return subject.get();

    }

}
