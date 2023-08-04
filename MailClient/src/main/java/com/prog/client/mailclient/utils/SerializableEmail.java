package com.prog.client.mailclient.utils;

import com.prog.client.mailclient.models.Email;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SerializableEmail implements Serializable {

    private long idEmail;
    private String sender;
    private String receiver;
    private String  subject;
    private String body;
    private LocalDateTime dateTime;
    private boolean isRead;


    public SerializableEmail(Email mail) {
        this.idEmail = mail.getIdEmail();
        this.sender = mail.getSender().get();
        this.receiver = mail.getReceiver().get();
        this.subject = mail.getSubject().get();
        this.body = mail.getBody().get();
        this.dateTime = mail.getDateTime();
        this.isRead = mail.isRead();
    }

    public SerializableEmail(long idEmail, String sender, String receiver, String subject, String body, LocalDateTime dateTime, boolean read) {
        this.idEmail = idEmail;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
        this.dateTime = dateTime;
        this.isRead = read;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getReceiver() {
        return receiver;
    }


    public String getBody() {
        return body;
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

    public long getIdEmail() {
        return idEmail;
    }

    public void setIdEmail(long idEmail) {
        this.idEmail = idEmail;
    }

    @Override
    public String toString() {
        return "SerializableEmail{" +
                "idEmail=" + idEmail +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", dateTime=" + dateTime +
                ", isRead=" + isRead +
                '}';
    }

}
