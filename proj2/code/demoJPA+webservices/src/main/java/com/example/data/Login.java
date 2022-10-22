package com.example.data;

public class Login {
    private String password;
    private String username;

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public Login(String password, String username) {
        this.password = password;
        this.username = username;
    }
}
