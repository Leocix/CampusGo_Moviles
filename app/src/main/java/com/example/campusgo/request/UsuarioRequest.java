package com.example.campusgo.request;

import com.example.campusgo.model.UsuarioData;

public class UsuarioRequest {
    private UsuarioData usuario;

    public UsuarioRequest(UsuarioData usuario) {
        this.usuario = usuario;
    }

    public UsuarioData getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioData usuario) {
        this.usuario = usuario;
    }



}
