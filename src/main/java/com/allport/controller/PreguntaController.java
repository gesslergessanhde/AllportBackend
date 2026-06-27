package com.allport.controller;

import com.allport.dto.RespuestasRequest;
import com.allport.model.ContainerPreguntas;
import com.allport.model.Resultado;
import com.allport.model.Usuario;
import com.allport.service.AllportEngineService;
import com.allport.service.FileStorageService;
import com.allport.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PreguntaController {

    @Autowired
    private FileStorageService storage;
    @Autowired
    private AllportEngineService engine;
    @Autowired
    private EmailService emailService;

    @GetMapping("/preguntas/p1")
    public ResponseEntity<?> getP1() { return ResponseEntity.ok(storage.obtenerPreguntas().getDb_preguntas_p1()); }

    @GetMapping("/preguntas/p2")
    public ResponseEntity<?> getP2() { return ResponseEntity.ok(storage.obtenerPreguntas().getDb_preguntas_p2()); }

    @GetMapping("/resultados")
    public ResponseEntity<?> getResultados() { return ResponseEntity.ok(storage.obtenerResultados()); }

    @PostMapping("/respuestas")
    public ResponseEntity<?> evaluarRespuestas(@RequestBody Map<String, Object> body) {
        List<Usuario> users = storage.obtenerUsuarios();
        List<Resultado> resultados = storage.obtenerResultados();

        // 🔍 Extraemos el ID del aspirante de forma segura controlando el tipo numérico
        int idAspirante = ((Number) body.get("id_aspirante")).intValue();

        // 🔍 Extraemos los mapas dinámicos de respuestas sin forzar la firma estricta del DTO
        Map<String, Map<String, Integer>> respuestasP1 = (Map<String, Map<String, Integer>>) body.get("respuestasP1");
        Map<String, Map<String, Integer>> respuestasP2 = (Map<String, Map<String, Integer>>) body.get("respuestasP2");

        Usuario u = users.stream()
                .filter(usr -> usr.getId_aspirante() == idAspirante)
                .findFirst()
                .orElse(null);

        // Ejecutamos tu motor psicométrico de Allport
        Map<String, Integer> scores = engine.calcularResultadosAllport(respuestasP1, respuestasP2);

        // Buscamos si el aspirante ya tenía un registro en resultados o generamos uno nuevo
        Resultado item = resultados.stream()
                .filter(r -> r.getId_resultado() == idAspirante)
                .findFirst()
                .orElse(null);

        boolean esNuevo = false;
        if (item == null) {
            item = new Resultado();
            esNuevo = true;
        }

        // Sincronizamos las propiedades de Allport exactamente a como venían de Node.js
        item.setId_resultado(idAspirante);
        item.setNombre(u != null ? u.getNombre_aspirante() : "Aspirante de Admisión");
        item.setCif(u != null ? u.getCif() : "N/A");
        item.setEmail("tu_correo_real_aqui@gmail.com");
        item.setTotal_teorico(scores.get("teorica"));
        item.setTotal_economico(scores.get("economico"));
        item.setTotal_estetico(scores.get("estetico"));
        item.setTotal_social(scores.get("social"));
        item.setTotal_politico(scores.get("politico"));
        item.setTotal_religioso(scores.get("religioso"));
        item.setEstado_evaluacion("Completado");
        item.setRespuestas_guardadas(body); // Persistimos todo el payload dinámico original

        if (esNuevo) {
            resultados.add(item);
        } else {
            // Si ya existía, actualizamos la posición del arreglo en memoria
            int index = resultados.indexOf(item);
            resultados.set(index, item);
        }

        // Guardamos físicamente en la carpeta externa 'data/resultados.json'
        storage.guardarResultados(resultados);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Respuestas evaluadas y guardadas con éxito de forma persistente.");
        return ResponseEntity.ok(res);
    }
    @PostMapping("/entrevista/{id}")
    public ResponseEntity<?> registrarEntrevista(@PathVariable int id, @RequestBody Map<String, String> body) {
        List<Resultado> resultados = storage.obtenerResultados();
        Resultado item = resultados.stream().filter(r -> r.getId_resultado() == id).findFirst().orElse(null);
        if (item != null) {
            item.setNotas_entrevista(body.get("notas"));
            item.setNotas_entrevista(body.get("notas")); // Compatibilidad dual de nombres de variables
            item.setEstado_evaluacion("Entrevistado");
            storage.guardarResultados(resultados);
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.status(404).body(Map.of("success", false));
    }

    @PostMapping("/enviar-correo/{id}")
    public ResponseEntity<?> despacharCorreo(@PathVariable int id, @RequestBody Map<String, String> body) {
        List<Resultado> resultados = storage.obtenerResultados();
        Resultado item = resultados.stream().filter(r -> r.getId_resultado() == id).findFirst().orElse(null);
        if (item == null) return ResponseEntity.status(404).body(Map.of("success", false));

        try {
            emailService.enviarReporteAxiologico(item, body.get("emailPersonalizado"));
            item.setEstado_evaluacion("Resultados Enviados");
            storage.guardarResultados(resultados);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}