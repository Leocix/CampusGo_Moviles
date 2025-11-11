package com.example.campusgo.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ViajeListadoData {
    @SerializedName("asientos_disponibles")
    private int asientosDisponibles;

    @SerializedName("asientos_ofertados")
    private int asientosOfertados;

    private String destino;
    private String estado;

    @SerializedName("fecha_hora_salida")
    private String FechaHoraSalida;

    @SerializedName("lat_destino")
    private double latDestino;

    @SerializedName("lat_partida")
    private double latPartida;

    @SerializedName("lng_destino")
    private double lngDestino;

    @SerializedName("lng_partida")
    private double lngPartida;

    @SerializedName("punto_partida")
    private String puntoPartida;

    private String restricciones;

    private VehiculoData vehiculo;

    @SerializedName("viaje_id")
    private int viajeId;

    // Esta es la lista que viene de la subconsulta JSON
    @SerializedName("pasajeros_reservados")
    private List<PasajeroReservaData> pasajerosReservados;

    public List<PasajeroReservaData> getPasajerosReservados() {
        return pasajerosReservados;
    }


    public int getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(int asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public int getAsientosOfertados() {
        return asientosOfertados;
    }

    public void setAsientosOfertados(int asientosOfertados) {
        this.asientosOfertados = asientosOfertados;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaHoraSalida() {
        return FechaHoraSalida;
    }

    public void setFechaHoraSalida(String fechaHoraSalida) {
        FechaHoraSalida = fechaHoraSalida;
    }

    public double getLatDestino() {
        return latDestino;
    }

    public void setLatDestino(double latDestino) {
        this.latDestino = latDestino;
    }

    public double getLatPartida() {
        return latPartida;
    }

    public void setLatPartida(double latPartida) {
        this.latPartida = latPartida;
    }

    public double getLngDestino() {
        return lngDestino;
    }

    public void setLngDestino(double lngDestino) {
        this.lngDestino = lngDestino;
    }

    public double getLngPartida() {
        return lngPartida;
    }

    public void setLngPartida(double lngPartida) {
        this.lngPartida = lngPartida;
    }

    public String getPuntoPartida() {
        return puntoPartida;
    }

    public void setPuntoPartida(String puntoPartida) {
        this.puntoPartida = puntoPartida;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(String restricciones) {
        this.restricciones = restricciones;
    }

    public VehiculoData getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoData vehiculo) {
        this.vehiculo = vehiculo;
    }

    public int getViajeId() {
        return viajeId;
    }

    public void setViajeId(int viajeId) {
        this.viajeId = viajeId;
    }

    //ArrayList que permite gestionar los viajes agregados por el usuario
    public static List<ViajeListadoData> viajes = new ArrayList<>();

}