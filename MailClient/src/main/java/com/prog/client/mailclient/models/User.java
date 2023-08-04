package com.prog.client.mailclient.models;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class User {
    private final SimpleListProperty<Email> inbox;
    private final SimpleListProperty<Email> sent;
    private final SimpleListProperty<Email> trashed;
    private final StringProperty emailAddress;

    public Email selectedEmail;
    public Email writeEmail;
    public Email emptyEmail;

    public User(String emailAddress) {
        this.inbox = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.sent = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.trashed = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.emailAddress = new SimpleStringProperty(emailAddress);
    }

    public SimpleListProperty<Email> inboxProperty()
    {
        return inbox;
    }

    public StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public void deleteInbox(Email email) {
        inbox.remove((email));
    }

    public void deleteSent(Email email) {
        sent.remove((email));
    }

    public void deleteTrashed(Email email) {
        trashed.remove((email));
    }

    public ObservableList<Email> getSent() {
        return sent.get();
    }

    public SimpleListProperty<Email> sentProperty() {
        return sent;
    }

    public ObservableList<Email> getTrashed() {
        return trashed.get();
    }

    public SimpleListProperty<Email> trashedProperty() {
        return trashed;
    }

    public boolean checkDuplicates(ListProperty<Email> list, Email e) {
        return list.stream().noneMatch(email -> email.getIdEmail() == e.getIdEmail());
    }

}
