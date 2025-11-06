package com.example.campusgo.model;

import com.google.gson.annotations.SerializedName;

public class PasajeroReservaData {
    @SerializedName("id")
    private int id;

    @SerializedName("foto_perfil")
    private String foto;

    public int getId() {
        return id;
    }

    public String getFoto() {
        return foto;
    }
}
