package com.allport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.allport.model.Usuario;
import com.allport.model.Resultado;
import com.allport.model.ContainerPreguntas;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {
    private final ObjectMapper mapper = new ObjectMapper();

    // 🔍 SOLUCIÓN: Rutas relativas directas a la raíz del proyecto
    private final String PATH_USUARIOS = "data/usuarios.json";
    private final String PATH_PREGUNTAS = "data/preguntas.json";
    private final String PATH_RESULTADOS = "data/resultados.json";

    public FileStorageService() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
    public List<Usuario> obtenerUsuarios() {
        try { return mapper.readValue(new File(PATH_USUARIOS), new TypeReference<List<Usuario>>(){}); }
        catch (IOException e) { return new ArrayList<>(); }
    }

    public void guardarUsuarios(List<Usuario> datos) {
        try { mapper.writerWithDefaultPrettyPrinter().writeValue(new File(PATH_USUARIOS), datos); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public ContainerPreguntas obtenerPreguntas() {
        try { return mapper.readValue(new File(PATH_PREGUNTAS), ContainerPreguntas.class); }
        catch (IOException e) { return new ContainerPreguntas(); }
    }

    public void guardarPreguntas(ContainerPreguntas datos) {
        try { mapper.writerWithDefaultPrettyPrinter().writeValue(new File(PATH_PREGUNTAS), datos); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public List<Resultado> obtenerResultados() {
        try { return mapper.readValue(new File(PATH_RESULTADOS), new TypeReference<List<Resultado>>(){}); }
        catch (IOException e) { return new ArrayList<>(); }
    }

    public void guardarResultados(List<Resultado> datos) {
        try { mapper.writerWithDefaultPrettyPrinter().writeValue(new File(PATH_RESULTADOS), datos); }
        catch (IOException e) { e.printStackTrace(); }
    }
}