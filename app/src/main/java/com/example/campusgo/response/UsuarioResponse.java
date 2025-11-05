package com.example.campusgo.response;

import com.example.campusgo.model.UsuarioData;
import com.google.gson.annotations.SerializedName;

public class UsuarioResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UsuarioData data;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UsuarioData getData() {
        return data;
    }

    public void setData(UsuarioData data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
