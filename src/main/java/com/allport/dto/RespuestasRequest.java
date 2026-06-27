package com.allport.dto;
import java.util.Map;

public class RespuestasRequest {
    private int id_aspirante;
    private Map<String, Map<String, Integer>> respuestasP1;
    private Map<String, Map<String, Integer>> respuestasP2;

    public int getId_aspirante() { return id_aspirante; }
    public void setId_aspirante(int id_aspirante) { this.id_aspirante = id_aspirante; }
    public Map<String, Map<String, Integer>> getRespuestasP1() { return respuestasP1; }
    public void setRespuestasP1(Map<String, Map<String, Integer>> respuestasP1) { this.respuestasP1 = respuestasP1; }
    public Map<String, Map<String, Integer>> getRespuestasP2() { return respuestasP2; }
    public void setRespuestasP2(Map<String, Map<String, Integer>> respuestasP2) { this.respuestasP2 = respuestasP2; }
}