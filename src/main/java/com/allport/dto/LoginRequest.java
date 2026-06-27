package com.allport.dto;
import java.util.Map;

public class LoginRequest {
    private String cif;
    private String password;
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}