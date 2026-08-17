package com.nagy_mark.mygamevault.models;

public class AuthRequest {
    private String email;
    private  String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
