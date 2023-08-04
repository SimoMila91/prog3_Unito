package com.prog.server.mailserver.model;

import com.prog.client.mailclient.utils.SerializableEmail;

import java.util.ArrayList;

public class User {
    private ArrayList<SerializableEmail> inbox;
    private ArrayList<SerializableEmail> sent;
    private ArrayList<SerializableEmail> trashed;
    private String emailAddress;

    public User(String emailAddress) {
        this.sent = new ArrayList<>();
        this.inbox = new ArrayList<>();
        this.trashed = new ArrayList<>();
        this.emailAddress = emailAddress;
    }


    public ArrayList<SerializableEmail> getInbox() {
        return inbox;
    }

    public void setInbox(ArrayList<SerializableEmail> inbox) {
        this.inbox = inbox;
    }

    public ArrayList<SerializableEmail> getSent() {
        return sent;
    }

    public void setSent(ArrayList<SerializableEmail> sent) {
        this.sent = sent;
    }

    public ArrayList<SerializableEmail> getTrashed() {
        return trashed;
    }

    public void setTrashed(ArrayList<SerializableEmail> trashed) {
        this.trashed = trashed;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

}
