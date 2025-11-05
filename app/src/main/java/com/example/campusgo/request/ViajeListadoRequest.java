package com.example.campusgo.request;

public class ViajeListadoRequest {
    private String campo_busqueda;
    private String texto_busqueda;
    private boolean asientos_disponibles;
    private boolean sin_restricciones;
    private String desde;
    private String hasta;

    public ViajeListadoRequest(String campo_busqueda, String texto_busqueda, boolean asientos_disponibles, boolean sin_restricciones, String desde, String hasta) {
        this.campo_busqueda = campo_busqueda;
        this.texto_busqueda = texto_busqueda;
        this.asientos_disponibles = asientos_disponibles;
        this.sin_restricciones = sin_restricciones;
        this.desde = desde;
        this.hasta = hasta;
    }

    public String getCampo_busqueda() {
        return campo_busqueda;
    }

    public void setCampo_busqueda(String campo_busqueda) {
        this.campo_busqueda = campo_busqueda;
    }

    public String getTexto_busqueda() {
        return texto_busqueda;
    }

    public void setTexto_busqueda(String texto_busqueda) {
        this.texto_busqueda = texto_busqueda;
    }

    public boolean isAsientos_disponibles() {
        return asientos_disponibles;
    }

    public void setAsientos_disponibles(boolean asientos_disponibles) {
        this.asientos_disponibles = asientos_disponibles;
    }

    public boolean isSin_restricciones() {
        return sin_restricciones;
    }

    public void setSin_restricciones(boolean sin_restricciones) {
        this.sin_restricciones = sin_restricciones;
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
