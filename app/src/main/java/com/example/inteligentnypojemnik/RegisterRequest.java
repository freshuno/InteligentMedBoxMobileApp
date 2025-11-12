package com.example.inteligentnypojemnik;

public class RegisterRequest {
    String display_name;
    String username;
    String email;
    String password;

    public RegisterRequest(String display_name, String username, String email, String password) {
        this.display_name = display_name;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}