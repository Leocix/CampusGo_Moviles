package com.example.campusgo.request;

import com.google.gson.annotations.SerializedName;

public class ViajeListadoRequest {
    @SerializedName("campo_busqueda")
    private String campoBusqueda;

    @SerializedName("texto_busqueda")
    private String textoBusqueda;

    @SerializedName("asientos_disponibles")
    private boolean asientosDisponibles;

    @SerializedName("sin_restricciones")
    private boolean sinRestricciones;

    private String desde;
    private String hasta;

    public ViajeListadoRequest(String campoBusqueda, String textoBusqueda, boolean asientosDisponibles, boolean sinRestricciones, String desde, String hasta) {
        this.campoBusqueda = campoBusqueda;
        this.textoBusqueda = textoBusqueda;
        this.asientosDisponibles = asientosDisponibles;
        this.sinRestricciones = sinRestricciones;
        this.desde = desde;
        this.hasta = hasta;
    }

    public String getCampoBusqueda() {
        return campoBusqueda;
    }

    public void setCampoBusqueda(String campoBusqueda) {
        this.campoBusqueda = campoBusqueda;
    }

    public String getTextoBusqueda() {
        return textoBusqueda;
    }

    public void setTextoBusqueda(String textoBusqueda) {
        this.textoBusqueda = textoBusqueda;
    }

    public boolean isAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(boolean asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public boolean isSinRestricciones() {
        return sinRestricciones;
    }

    public void setSinRestricciones(boolean sinRestricciones) {
        this.sinRestricciones = sinRestricciones;
    }

    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }
}
