package com.allport.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EntrevistaController {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @GetMapping("/entrevista/{idAspirante}")
    public ResponseEntity<?> obtenerEntrevista(@PathVariable int idAspirante) {
        Map<String, Object> respuesta = new HashMap<>();
        String sql = "SELECT id_intento, notas_entrevista FROM Intento_Test WHERE id_aspirante = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAspirante);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    respuesta.put("id_intento", rs.getInt("id_intento"));
                    String notas = rs.getString("notas_entrevista");
                    respuesta.put("notas_entrevista", notas != null ? notas : "");
                    return ResponseEntity.ok(respuesta);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

        respuesta.put("id_intento", idAspirante);
        respuesta.put("notas_entrevista", "");
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/entrevista/{idAspirante}")
    public ResponseEntity<?> guardarEntrevista(@PathVariable int idAspirante, @RequestBody Map<String, String> body) {
        String notas = body.get("notas");
        if (notas == null) {
            notas = body.get("notas_entrevista");
        }

        String sqlCheck = "SELECT COUNT(*) FROM Intento_Test WHERE id_aspirante = ?";
        String sqlUpdate = "UPDATE Intento_Test SET notas_entrevista = ?, id_estado = 2 WHERE id_aspirante = ?";
        String sqlMaxIntento = "SELECT ISNULL(MAX(id_intento), 0) + 1 FROM Intento_Test";
        String sqlInsert = "INSERT INTO Intento_Test (id_intento, id_aspirante, id_estado, fecha_intento, tiempo_restante_segundos, notas_entrevista) VALUES (?, ?, 2, CAST(GETDATE() AS DATE), 0, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            boolean existeIntento = false;

            try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
                checkStmt.setInt(1, idAspirante);
                try (ResultSet rsCheck = checkStmt.executeQuery()) { // 🎯 Corregido: rsCheck para no colisionar
                    if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                        existeIntento = true;
                    }
                }
            }

            if (existeIntento) {
                try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                    updateStmt.setString(1, notas);
                    updateStmt.setInt(2, idAspirante);
                    updateStmt.executeUpdate();
                }
            } else {
                int nextIntentoId = 1;
                try (Statement stmtMax = conn.createStatement();
                     ResultSet rsMax = stmtMax.executeQuery(sqlMaxIntento)) { // 🎯 Corregido: rsMax para evitar duplicados
                    if (rsMax.next()) {
                        nextIntentoId = rsMax.getInt(1);
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setInt(1, nextIntentoId);
                    insertStmt.setInt(2, idAspirante);
                    insertStmt.setString(3, notas);
                    insertStmt.executeUpdate();
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Guardado exitoso con actualización de estado."));

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}