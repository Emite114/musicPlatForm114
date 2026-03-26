package com.example.musicplatform.dto.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String account;
    private String password;

    public LoginRequest(){}
    public LoginRequest(String account, String password){
        this.account = account;
        this.password = password;
    }
}

