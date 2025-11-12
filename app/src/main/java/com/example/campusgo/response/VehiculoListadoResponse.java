package com.example.campusgo.response;

import com.example.campusgo.model.VehiculoData;

import java.util.List;

public class VehiculoListadoResponse {
    private boolean status;

    private String message;

    private List<VehiculoData> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<VehiculoData> getData() {
        return data;
    }
}
