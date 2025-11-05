package com.example.campusgo.response;

import com.example.campusgo.model.LoginData;

public class LoginResponse {
    private LoginData data;
    private String message;
    private boolean status;

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
