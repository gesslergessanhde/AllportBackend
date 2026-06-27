package com.allport.model;

public class Usuario {
    private int id_aspirante;
    private String nombre_aspirante;
    private String cif;
    private String password_hash;
    private String rol;

    public Usuario() {}
    public Usuario(int id_aspirante, String nombre_aspirante, String cif, String password_hash, String rol) {
        this.id_aspirante = id_aspirante;
        this.nombre_aspirante = nombre_aspirante;
        this.cif = cif;
        this.password_hash = password_hash;
        this.rol = rol;
    }

    public int getId_aspirante() { return id_aspirante; }
    public void setId_aspirante(int id_aspirante) { this.id_aspirante = id_aspirante; }
    public String getNombre_aspirante() { return nombre_aspirante; }
    public void setNombre_aspirante(String nombre_aspirante) { this.nombre_aspirante = nombre_aspirante; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}