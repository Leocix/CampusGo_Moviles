package com.example.campusgo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginData {
    private String email;
    private int id;
    private String nombre;
    private String token;

    @SerializedName("rol_id")
    private int rolId;

    private String foto;

    private List<Integer> roles;

    //Agregar un atributo que permita almacenar los datos de la sesión
    public static LoginData DATOS_SESION_USUARIO;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Integer> getRoles() {
        return roles;
    }

    public void setRoles(List<Integer> roles) {
        this.roles = roles;
    }
}
