package com.theironyard.entities;

import javax.persistence.*;

@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue
    int id;

    @ManyToOne
    User sender;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @ManyToOne
    User receiver;

    @Column(nullable = false)
    String filename;

    public Photo() {
    }

    public Photo(User sender, User receiver, String filename) {
        this.sender = sender;
        this.receiver = receiver;
        this.filename = filename;
    }
}
