package com.example.campusgo.model;

import androidx.annotation.NonNull;

public class VehiculoData {
    private String color;
    private int id;
    private String placa;
    private String marca;
    private String modelo;
    private int pasajeros;

    public int getPasajeros() {
        return pasajeros;
    }

    public void setPasajeros(int pasajeros) {
        this.pasajeros = pasajeros;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    @NonNull
    @Override
    public String toString() {
        // Esto es lo que el usuario verá en la lista desplegable
        return marca + " " + modelo + " (" + placa + ")";
    }

}
