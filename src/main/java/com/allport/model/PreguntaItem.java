package com.allport.model;
import java.util.List;

public class PreguntaItem {
    private int id_item;
    private String texto_item;
    private String seccion;
    private List<String> opciones;

    public PreguntaItem() {}

    public int getId_item() { return id_item; }
    public void setId_item(int id_item) { this.id_item = id_item; }
    public String getTexto_item() { return texto_item; }
    public void setTexto_item(String texto_item) { this.texto_item = texto_item; }
    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }
    public List<String> getOpciones() { return opciones; }
    public void setOpciones(List<String> opciones) { this.opciones = opciones; }
}