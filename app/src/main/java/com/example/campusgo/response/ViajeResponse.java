package com.example.campusgo.response;

import com.google.gson.annotations.SerializedName;

public class ViajeResponse {

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
