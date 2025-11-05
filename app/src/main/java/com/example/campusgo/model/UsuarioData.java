package com.example.campusgo.model;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

public class UsuarioData {

    @SerializedName("nombres")
    private String nombres;
    @SerializedName("apellido_paterno")
    private String apellido_paterno;
    @SerializedName("apellido_materno")
    private String apellido_materno;
    @SerializedName("dni")
    private String dni;
    @SerializedName("email")
    private String email;
    @SerializedName("telefono")
    private String telefono;
    @SerializedName("clave")
    private String clave;
    @SerializedName("clave_confirmada")
    private String clave_confirmada;
    @SerializedName("rol_id")
    private int rol_id;
    @SerializedName("vehiculo")
    private VehiculoData vehiculo;

    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellido_paterno() {
        return apellido_paterno;
    }

    public void setApellido_paterno(String apellido_paterno) {
        this.apellido_paterno = apellido_paterno;
    }

    public String getApellido_materno() {
        return apellido_materno;
    }

    public void setApellido_materno(String apellido_materno) {
        this.apellido_materno = apellido_materno;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getClave_confirmada() {
        return clave_confirmada;
    }

    public void setClave_confirmada(String clave_confirmada) {
        this.clave_confirmada = clave_confirmada;
    }

    public int getRol_id() {
        return rol_id;
    }

    public void setRol_id(int rol_id) {
        this.rol_id = rol_id;
    }

    public VehiculoData getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoData vehiculo) {
        this.vehiculo = vehiculo;
    }

}
