package com.example.campusgo.request;

import com.google.gson.annotations.SerializedName;

public class DetalleViajeRequest {
    @SerializedName("viaje_id")
    private int viajeId;

    @SerializedName("estado_id")
    private int estadoId;

    public DetalleViajeRequest(int viajeId, int estadoId) {
        this.viajeId = viajeId;
        this.estadoId = estadoId;
    }

    public int getViajeId() {
        return viajeId;
    }

    public void setViajeId(int viajeId) {
        this.viajeId = viajeId;
    }

    public int getEstadoId() {
        return estadoId;
    }

    public void setEstadoId(int estadoId) {
        this.estadoId = estadoId;
    }
}
