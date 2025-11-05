package com.example.campusgo.request;

import com.google.gson.annotations.SerializedName;

public class DetalleViajeRequest {
    @SerializedName("viaje_id")
    private int viajeId;

    @SerializedName("estado_id")
    private int estadoID;

    public DetalleViajeRequest(int viajeId, int estadoID) {
        this.viajeId = viajeId;
        this.estadoID = estadoID;
    }

    public int getViajeId() {
        return viajeId;
    }

    public void setViajeId(int viajeId) {
        this.viajeId = viajeId;
    }

    public int getEstadoID() {
        return estadoID;
    }

    public void setEstadoID(int estadoID) {
        this.estadoID = estadoID;
    }
}
