package com.example.campusgo.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReservaRequest {
    @SerializedName("pasajero_id")
    private int pasajeroId;

    @SerializedName("fecha_reserva")
    private String fechaReserva;

    private String observacion;

    @SerializedName("detalle_viaje")
    private List<DetalleViajeRequest> detalleViaje;

    public ReservaRequest() {

    }

    public int getPasajeroId() {
        return pasajeroId;
    }

    public void setPasajeroId(int pasajeroId) {
        this.pasajeroId = pasajeroId;
    }

    public String getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(String fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<DetalleViajeRequest> getDetalleViaje() {
        return detalleViaje;
    }

    public void setDetalleViaje(List<DetalleViajeRequest> detalleViaje) {
        this.detalleViaje = detalleViaje;
    }
}
