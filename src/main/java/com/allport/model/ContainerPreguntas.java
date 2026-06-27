package com.allport.model;
import java.util.List;

public class ContainerPreguntas {
    private List<PreguntaItem> db_preguntas_p1;
    private List<PreguntaItem> db_preguntas_p2;

    public ContainerPreguntas() {}

    public List<PreguntaItem> getDb_preguntas_p1() { return db_preguntas_p1; }
    public void setDb_preguntas_p1(List<PreguntaItem> db_preguntas_p1) { this.db_preguntas_p1 = db_preguntas_p1; }
    public List<PreguntaItem> getDb_preguntas_p2() { return db_preguntas_p2; }
    public void setDb_preguntas_p2(List<PreguntaItem> db_preguntas_p2) { this.db_preguntas_p2 = db_preguntas_p2; }
}