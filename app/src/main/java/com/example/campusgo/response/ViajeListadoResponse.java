package com.example.campusgo.response;

import com.example.campusgo.model.ViajeListadoData;

public class ViajeListadoResponse {
    private ViajeListadoData[] data;
    private String message;
    private boolean status;

    public ViajeListadoData[] getData() {
        return data;
    }

    public void setData(ViajeListadoData[] data) {
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
