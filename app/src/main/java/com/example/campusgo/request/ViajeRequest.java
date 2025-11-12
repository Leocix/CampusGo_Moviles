package com.example.campusgo.request;

import com.google.gson.annotations.SerializedName;

public class ViajeRequest {
    @SerializedName("vehiculo_id")
    private int vehiculoId;
    @SerializedName("conductor_id")
    private int conductorId;
    @SerializedName("punto_partida")
    private String puntoPartida;
    @SerializedName("destino")
    private String destino;
    @SerializedName("lat_partida")
    private double latPartida;
    @SerializedName("lng_partida")
    private double lngPartida;
    @SerializedName("lat_destino")
    private double latDestino;
    @SerializedName("lng_destino")
    private double lngDestino;
    @SerializedName("fecha_hora_salida")
    private String fechaHoraSalida;
    @SerializedName("asientos_ofertados")
    private int asientosOfertados;
    @SerializedName("restricciones")
    private String restricciones;
    @SerializedName("estado_id")
    private int estadoId;


    public int getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(int vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public int getConductorId() {
        return conductorId;
    }

    public void setConductorId(int conductorId) {
        this.conductorId = conductorId;
    }

    public String getPuntoPartida() {
        return puntoPartida;
    }

    public void setPuntoPartida(String puntoPartida) {
        this.puntoPartida = puntoPartida;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public double getLatPartida() {
        return latPartida;
    }

    public void setLatPartida(double latPartida) {
        this.latPartida = latPartida;
    }

    public double getLngPartida() {
        return lngPartida;
    }

    public void setLngPartida(double lngPartida) {
        this.lngPartida = lngPartida;
    }

    public double getLatDestino() {
        return latDestino;
    }

    public void setLatDestino(double latDestino) {
        this.latDestino = latDestino;
    }

    public double getLngDestino() {
        return lngDestino;
    }

    public void setLngDestino(double lngDestino) {
        this.lngDestino = lngDestino;
    }

    public String getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(String fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public int getAsientosOfertados() {
        return asientosOfertados;
    }

    public void setAsientosOfertados(int asientosOfertados) {
        this.asientosOfertados = asientosOfertados;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(String restricciones) {
        this.restricciones = restricciones;
    }

    public int getEstadoId() {
        return estadoId;
    }

    public void setEstadoId(int estadoId) {
        this.estadoId = estadoId;
    }
}