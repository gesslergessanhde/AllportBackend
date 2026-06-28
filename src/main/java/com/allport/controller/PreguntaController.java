package com.allport.controller;

import com.allport.model.PreguntaItem;
import com.allport.model.Resultado;
import com.allport.service.AllportEngineService;
import com.allport.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PreguntaController {

    @Autowired
    private AllportEngineService engine;
    @Autowired
    private EmailService emailService;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @GetMapping("/preguntas/p1")
    public ResponseEntity<?> getP1() {
        return ResponseEntity.ok(obtenerPreguntasPorSeccion("Parte 1"));
    }

    @GetMapping("/preguntas/p2")
    public ResponseEntity<?> getP2() {
        return ResponseEntity.ok(obtenerPreguntasPorSeccion("Parte 2"));
    }

    @GetMapping("/resultados")
    public ResponseEntity<?> getResultados() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT r.id_resultado, r.total_teorico, r.total_economico, r.total_estetico, " +
                "r.total_social, r.total_politico, r.total_religioso, " +
                "a.id_aspirante, a.nombre_aspirante, a.cif, a.correo, i.id_intento, i.id_estado " +
                "FROM Resultado_axiologico r " +
                "INNER JOIN Intento_Test i ON r.id_intento = i.id_intento " +
                "INNER JOIN Aspirante a ON i.id_aspirante = a.id_aspirante";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                int idIntento = rs.getInt("id_intento");
                int idAspirante = rs.getInt("id_aspirante");
                int idEstado = rs.getInt("id_estado");

                map.put("id_resultado", rs.getInt("id_resultado"));
                map.put("nombre", rs.getString("nombre_aspirante"));
                map.put("cif", rs.getString("cif"));
                map.put("email", rs.getString("correo") != null ? rs.getString("correo") : "");
                map.put("total_teorico", rs.getInt("total_teorico"));
                map.put("total_economico", rs.getInt("total_economico"));
                map.put("total_estetico", rs.getInt("total_estetico"));
                map.put("total_social", rs.getInt("total_social"));
                map.put("total_politico", rs.getInt("total_politico"));
                map.put("total_religioso", rs.getInt("total_religioso"));

                // 🎯 MAPEO DE ESTADO ORIGINAL DINÁMICO
                if (idEstado == 3) {
                    map.put("estado_evaluacion", "Resultados Enviados");
                } else if (idEstado == 2) {
                    map.put("estado_evaluacion", "Revisado");
                } else {
                    map.put("estado_evaluacion", "Recibido");
                }

                Map<String, Object> respuestasGuardadas = new HashMap<>();
                respuestasGuardadas.put("id_aspirante", idAspirante);

                Map<String, Map<String, Integer>> respuestasP1 = new HashMap<>();
                Map<String, Map<String, Integer>> respuestasP2 = new HashMap<>();

                String sqlProgreso = "SELECT r.id_item, r.letra_opcion, r.valor_puntos, i.seccion " +
                        "FROM Respuesta_progreso r " +
                        "INNER JOIN Item_cuestionario i ON r.id_item = i.id_item " +
                        "WHERE r.id_intento = ?";

                try (PreparedStatement stmtP = conn.prepareStatement(sqlProgreso)) {
                    stmtP.setInt(1, idIntento);
                    try (ResultSet rsP = stmtP.executeQuery()) {
                        while (rsP.next()) {
                            int idItem = rsP.getInt("id_item");
                            String letra = rsP.getString("letra_opcion");
                            int puntos = rsP.getInt("valor_puntos");
                            String seccionItem = rsP.getString("seccion");

                            if ("Parte 1".equals(seccionItem)) {
                                respuestasP1.computeIfAbsent(String.valueOf(idItem), k -> new HashMap<>()).put(letra, puntos);
                            } else {
                                respuestasP2.computeIfAbsent(String.valueOf(idItem - 31), k -> new HashMap<>()).put(letra, puntos);
                            }
                        }
                    }
                }

                respuestasGuardadas.put("respuestasP1", respuestasP1);
                respuestasGuardadas.put("respuestasP2", respuestasP2);
                map.put("respuestas_guardadas", respuestasGuardadas);

                lista.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/respuestas")
    public ResponseEntity<?> evaluarRespuestas(@RequestBody Map<String, Object> body) {
        int idAspirante = 0;
        if (body.get("id_aspirante") != null) {
            idAspirante = Integer.parseInt(String.valueOf(body.get("id_aspirante")));
        } else if (body.get("id_aggregate") != null) {
            idAspirante = Integer.parseInt(String.valueOf(body.get("id_aggregate")));
        }

        Map<String, Object> respuestasP1 = (Map<String, Object>) body.get("respuestasP1");
        Map<String, Object> respuestasP2 = (Map<String, Object>) body.get("respuestasP2");

        Map<String, Map<String, Integer>> p1Engine = new HashMap<>();
        if (respuestasP1 != null) {
            for (Map.Entry<String, Object> entry : respuestasP1.entrySet()) {
                Map<String, Integer> subMap = new HashMap<>();
                Map<String, Object> inner = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> subEntry : inner.entrySet()) {
                    subMap.put(subEntry.getKey(), Integer.parseInt(String.valueOf(subEntry.getValue())));
                }
                p1Engine.put(entry.getKey(), subMap);
            }
        }

        Map<String, Map<String, Integer>> p2Engine = new HashMap<>();
        if (respuestasP2 != null) {
            for (Map.Entry<String, Object> entry : respuestasP2.entrySet()) {
                Map<String, Integer> subMap = new HashMap<>();
                Map<String, Object> inner = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> subEntry : inner.entrySet()) {
                    subMap.put(subEntry.getKey(), Integer.parseInt(String.valueOf(subEntry.getValue())));
                }
                p2Engine.put(entry.getKey(), subMap);
            }
        }

        Map<String, Integer> scores = engine.calcularResultadosAllport(p1Engine, p2Engine);

        String sqlMaxIntento = "SELECT ISNULL(MAX(id_intento), 0) + 1 FROM Intento_Test";
        String sqlMaxResultado = "SELECT ISNULL(MAX(id_resultado), 0) + 1 FROM Resultado_axiologico";

        // 🎯 CORRECCIÓN ORIGINAL: El test inicia de forma limpia con id_estado = 1 ("Recibido")
        String sqlIntento = "INSERT INTO Intento_Test (id_intento, id_aspirante, id_estado, fecha_intento, tiempo_restante_segundos) VALUES (?, ?, 1, CAST(GETDATE() AS DATE), 0);";
        String sqlRes = "INSERT INTO Resultado_axiologico (id_resultado, id_intento, total_economico, total_politico, total_teorico, total_social, total_estetico, total_religioso, fecha_calculo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);";
        String sqlProgreso = "INSERT INTO Respuesta_progreso (id_intento, id_item, letra_opcion, valor_puntos) VALUES (?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);

            int nextIntentoId;
            int nextResultadoId;

            try (Statement stmt = conn.createStatement(); ResultSet rs1 = stmt.executeQuery(sqlMaxIntento)) {
                rs1.next(); nextIntentoId = rs1.getInt(1);
            }
            try (Statement stmt = conn.createStatement(); ResultSet rs2 = stmt.executeQuery(sqlMaxResultado)) {
                rs2.next(); nextResultadoId = rs2.getInt(1);
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlIntento)) {
                stmt.setInt(1, nextIntentoId);
                stmt.setInt(2, idAspirante);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlRes)) {
                stmt.setInt(1, nextResultadoId);
                stmt.setInt(2, nextIntentoId);
                stmt.setInt(3, scores.getOrDefault("economico", 0));
                stmt.setInt(4, scores.getOrDefault("politico", 0));
                stmt.setInt(5, scores.getOrDefault("teorica", 0));
                stmt.setInt(6, scores.getOrDefault("social", 0));
                stmt.setInt(7, scores.getOrDefault("estetico", 0));
                stmt.setInt(8, scores.getOrDefault("religioso", 0));
                stmt.executeUpdate();
            }

            if (respuestasP1 != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlProgreso)) {
                    for (Map.Entry<String, Object> pregunta : respuestasP1.entrySet()) {
                        int idItem = Integer.parseInt(pregunta.getKey());
                        Map<String, Object> opciones = (Map<String, Object>) pregunta.getValue();
                        for (Map.Entry<String, Object> opcion : opciones.entrySet()) {
                            stmt.setInt(1, nextIntentoId);
                            stmt.setInt(2, idItem);
                            stmt.setString(3, opcion.getKey());
                            stmt.setInt(4, Integer.parseInt(String.valueOf(opcion.getValue())));
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            if (respuestasP2 != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlProgreso)) {
                    for (Map.Entry<String, Object> pregunta : respuestasP2.entrySet()) {
                        int idItemOriginal = Integer.parseInt(pregunta.getKey());
                        int idItemConDesfase = idItemOriginal + 31;

                        Map<String, Object> opciones = (Map<String, Object>) pregunta.getValue();
                        for (Map.Entry<String, Object> opcion : opciones.entrySet()) {
                            stmt.setInt(1, nextIntentoId);
                            stmt.setInt(2, idItemConDesfase);
                            stmt.setString(3, opcion.getKey());
                            stmt.setInt(4, Integer.parseInt(String.valueOf(opcion.getValue())));
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno en el procesamiento: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Test guardado perfectamente."));
    }

    @PostMapping("/enviar-correo/{id}")
    public ResponseEntity<?> despacharCorreo(@PathVariable int id, @RequestBody Map<String, String> body) {
        String sql = "SELECT r.*, a.nombre_aspirante, a.cif, a.correo FROM Resultado_axiologico r " +
                "INNER JOIN Intento_Test i ON r.id_intento = i.id_intento " +
                "INNER JOIN Aspirante a ON i.id_aspirante = a.id_aspirante WHERE r.id_resultado = ?";

        // 🎯 Forzamos el guardado de id_estado = 3 (Resultados Enviados) al enviar por Gmail
        String sqlUpdateEstadoEmail = "UPDATE Intento_Test SET id_estado = 3 WHERE id_intento = (SELECT id_intento FROM Resultado_axiologico WHERE id_resultado = ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Resultado item = new Resultado();
                    item.setId_resultado(rs.getInt("id_resultado"));
                    item.setNombre(rs.getString("nombre_aspirante"));
                    item.setCif(rs.getString("cif"));
                    item.setEmail(rs.getString("correo"));
                    item.setTotal_teorico(rs.getInt("total_teorico"));
                    item.setTotal_economico(rs.getInt("total_economico"));
                    item.setTotal_estetico(rs.getInt("total_estetico"));
                    item.setTotal_social(rs.getInt("total_social"));
                    item.setTotal_politico(rs.getInt("total_politico"));
                    item.setTotal_religioso(rs.getInt("total_religioso"));

                    // Se ejecuta la query para mover el estado a 3 en SQL Server
                    try (PreparedStatement stmtState = conn.prepareStatement(sqlUpdateEstadoEmail)) {
                        stmtState.setInt(1, id);
                        stmtState.executeUpdate();
                    }

                    emailService.enviarReporteAxiologico(item, body.get("emailPersonalizado"));
                    return ResponseEntity.ok(Map.of("success", true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
        return ResponseEntity.status(404).body(Map.of("success", false));
    }

    private List<Map<String, Object>> obtenerPreguntasPorSeccion(String seccion) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT i.id_item, i.texto_item, o.texto_opcion, o.letra_opcion " +
                "FROM Item_cuestionario i " +
                "LEFT JOIN opcion_respuesta o ON i.id_item = o.id_item " +
                "WHERE i.seccion = ? " +
                "ORDER BY i.id_item ASC, o.letra_opcion ASC";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, seccion);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, Map<String, Object>> mapeoPreguntas = new HashMap<>();

                while (rs.next()) {
                    int idItem = rs.getInt("id_item");

                    if (!mapeoPreguntas.containsKey(idItem)) {
                        Map<String, Object> item = new HashMap<>();

                        if (seccion.equals("Parte 2")) {
                            item.put("id_item", idItem - 31);
                        } else {
                            item.put("id_item", idItem);
                        }

                        item.put("texto_item", rs.getString("texto_item"));
                        item.put("seccion", seccion);

                        if (seccion.equals("Parte 1")) {
                            item.put("opciones", null);
                        } else {
                            item.put("opciones", new ArrayList<String>());
                        }

                        mapeoPreguntas.put(idItem, item);
                        list.add(item);
                    }

                    String textoOpcion = rs.getString("texto_opcion");
                    String letraOpcion = rs.getString("letra_opcion");
                    if (textoOpcion != null && letraOpcion != null && seccion.equals("Parte 2")) {
                        List<String> opcionesList = (List<String>) mapeoPreguntas.get(idItem).get("opciones");
                        opcionesList.add(letraOpcion + ". " + textoOpcion);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}