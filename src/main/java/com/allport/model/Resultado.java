package com.allport.model;
import java.util.Map;

public class Resultado {
    private int id_resultado;
    private String nombre;
    private String cif;
    private String email;
    private int total_teorico;
    private int total_economico;
    private int total_estetico;
    private int total_social;
    private int total_politico;
    private int total_religioso;
    private String estado_evaluacion;
    private String notes_entrevista;
    private String notas_entrevista;
    private Map<String, Object> respuestas_guardadas;

    public Resultado() {}

    // Getters y Setters Estructurados
    public int getId_resultado() { return id_resultado; }
    public void setId_resultado(int id_resultado) { this.id_resultado = id_resultado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getTotal_teorico() { return total_teorico; }
    public void setTotal_teorico(int total_teorico) { this.total_teorico = total_teorico; }
    public int getTotal_economico() { return total_economico; }
    public void setTotal_economico(int total_economico) { this.total_economico = total_economico; }
    public int getTotal_estetico() { return total_estetico; }
    public void setTotal_estetico(int total_estetico) { this.total_estetico = total_estetico; }
    public int getTotal_social() { return total_social; }
    public void setTotal_social(int total_social) { this.total_social = total_social; }
    public int getTotal_politico() { return total_politico; }
    public void setTotal_politico(int total_politico) { this.total_politico = total_politico; }
    public int getTotal_religioso() { return total_religioso; }
    public void setTotal_religioso(int total_religioso) { this.total_religioso = total_religioso; }
    public String getEstado_evaluacion() { return estado_evaluacion; }
    public void setEstado_evaluacion(String estado_evaluacion) { this.estado_evaluacion = estado_evaluacion; }
    public String getNotes_entrevista() { return notes_entrevista; }
    public void setNotes_entrevista(String notes_entrevista) { this.notes_entrevista = notes_entrevista; }
    public String getNotas_entrevista() { return notas_entrevista; }
    public void setNotas_entrevista(String notas_entrevista) { this.notas_entrevista = notas_entrevista; }
    public Map<String, Object> getRespuestas_guardadas() { return respuestas_guardadas; }
    public void setRespuestas_guardadas(Map<String, Object> respuestas_guardadas) { this.respuestas_guardadas = respuestas_guardadas; }
}