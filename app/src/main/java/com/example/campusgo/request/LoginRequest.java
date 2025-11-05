package com.example.campusgo.request;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String email;
    private String clave;

    @SerializedName("rol_id")
    private Integer rolId; // Se usa Integer para que el valor pueda ser nulo

    /**
     * Constructor para el login estándar, sin especificar rol.
     * El backend usará el rol por defecto del usuario.
     * @param email Email del usuario.
     * @param clave Clave del usuario.
     */
    public LoginRequest(String email, String clave) {
        this.email = email;
        this.clave = clave;
        this.rolId = null;
    }

    /**
     * Constructor para el cambio de rol.
     * @param email Email del usuario.
     * @param clave Clave del usuario.
     * @param rolId Rol específico con el que se quiere iniciar sesión.
     */
    public LoginRequest(String email, String clave, Integer rolId) {
        this.email = email;
        this.clave = clave;
        this.rolId = rolId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Integer getRolId() {
        return rolId;
    }

    public void setRolId(Integer rolId) {
        this.rolId = rolId;
    }
}
