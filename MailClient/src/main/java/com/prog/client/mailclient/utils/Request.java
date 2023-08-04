package com.prog.client.mailclient.utils;


import com.prog.client.mailclient.models.Email;

import java.io.Serializable;

public class Request implements Serializable {

    private String request;
    private long lastInbox;
    private long idEmail;
    private String emailAddress;
    private SerializableEmail email;
    private String section;

    // this just for delete
    public Request(String request, String emailAddress, SerializableEmail email, String section) {
        this.request = request;
        this.emailAddress = emailAddress;
        this.email = email;
        this.section = section;
    }

    public Request(String request, String emailAddress, SerializableEmail email) {
        this.request = request;
        this.emailAddress = emailAddress;
        this.email = email;
    }

    // this just for allEmails
    public Request(String request, String emailAddress) {
        this.request = request;
        this.emailAddress = emailAddress;
    }

    public Request(String request, String emailAddress, long lastInbox) {
        this.request = request;
        this.emailAddress = emailAddress;
        this.lastInbox = lastInbox;
    }

    // this for send email
    public Request(String request, SerializableEmail email) {
        this.request = request;
        this.email = email;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public SerializableEmail getEmail() {
        return email;
    }

    public void setEmail(SerializableEmail email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Request{" +
                "request='" + request + '\'' +
                ", idEmail='" + idEmail + '\'' +
                ", email=" + email +
                '}';
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public long getLastInbox() {
        return lastInbox;
    }
}