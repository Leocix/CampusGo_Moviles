package com.example.campusgo.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ViajeListadoData {

    @SerializedName("viaje_id")
    private int viaje_id;

    public int getViaje_id() {
        return viaje_id;
    }

    public void setViaje_id(int viaje_id) {
        this.viaje_id = viaje_id;
    }

    @SerializedName("asientos_disponibles")
    private int asientos_disponibles;

    @SerializedName("asientos_ofertados")
    private String asientos_ofertados;

    private String destino;
    private String estado;

    @SerializedName("fecha_hora_salida")
    private String fecha_hora_salida;

    @SerializedName("lat_destino")
    private String latdestino;

    @SerializedName("lat_partida")
    private String latpartida;

    @SerializedName("lng_destino")
    private String lngdestino;

    @SerializedName("lng_partida")
    private String lngpartida;

    @SerializedName("punto_partida")
    private String punto_partida;

    @SerializedName("restricciones")
    private String restricciones;

    private VehiculoData vehiculo;

    public int getAsientos_disponibles() {
        return asientos_disponibles;
    }

    public void setAsientos_disponibles(int asientos_disponibles) {
        this.asientos_disponibles = asientos_disponibles;
    }

    public String getAsientos_ofertados() {
        return asientos_ofertados;
    }

    public void setAsientos_ofertados(String asientos_ofertados) {
        this.asientos_ofertados = asientos_ofertados;
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

    public String getFecha_hora_salida() {
        return fecha_hora_salida;
    }

    public void setFecha_hora_salida(String fecha_hora_salida) {
        this.fecha_hora_salida = fecha_hora_salida;
    }

    public String getLatdestino() {
        return latdestino;
    }

    public void setLatdestino(String latdestino) {
        this.latdestino = latdestino;
    }

    public String getLatpartida() {
        return latpartida;
    }

    public void setLatpartida(String latpartida) {
        this.latpartida = latpartida;
    }

    public String getLngdestino() {
        return lngdestino;
    }

    public void setLngdestino(String lngdestino) {
        this.lngdestino = lngdestino;
    }

    public String getLngpartida() {
        return lngpartida;
    }

    public void setLngpartida(String lngpartida) {
        this.lngpartida = lngpartida;
    }

    public String getPunto_partida() {
        return punto_partida;
    }

    public void setPunto_partida(String punto_partida) {
        this.punto_partida = punto_partida;
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

    //ArrayList que permite gestionar los viajes agregados por el usuario
    public static List<ViajeListadoData> viajes = new ArrayList<>();
}
