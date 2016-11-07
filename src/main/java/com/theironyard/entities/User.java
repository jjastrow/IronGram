package com.theironyard.entities;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Id
    @GeneratedValue
    int id;

    @Column(nullable = false, unique = true)
    String name;

    @Column(nullable = false)
    String password;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}

