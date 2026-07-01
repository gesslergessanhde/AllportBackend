package com.allport.controller;

import com.allport.model.Usuario;
import com.allport.model.PreguntaItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> getUsuariosMaster() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_aspirante, nombre_aspirante, cif, password_hash, correo, rol FROM Aspirante";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId_aspirante(rs.getInt("id_aspirante"));
                u.setNombre_aspirante(rs.getString("nombre_aspirante"));
                u.setCif(rs.getString("cif"));
                u.setPassword_hash(rs.getString("password_hash"));
                u.setRol(rs.getString("rol"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario u) {
        String sqlMax = "SELECT ISNULL(MAX(id_aspirante), 0) + 1 FROM Aspirante";
        String sqlIns = "INSERT INTO Aspirante (id_aspirante, nombre_aspirante, cif, fecha_registro, password_hash, correo, rol, id_estado, ultima_actualizacion) VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?, ?, 1, CURRENT_TIMESTAMP)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            int nextId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlMax)) {
                rs.next();
                nextId = rs.getInt(1);
            }
            u.setId_aspirante(nextId);

            try (PreparedStatement stmt = conn.prepareStatement(sqlIns)) {
                stmt.setInt(1, nextId);
                stmt.setString(2, u.getNombre_aspirante());
                stmt.setString(3, u.getCif());
                stmt.setString(4, u.getPassword_hash());
                stmt.setString(5, u.getCif() + "@allport.com");
                stmt.setString(6, u.getRol() != null && !u.getRol().trim().isEmpty() ? u.getRol() : "usuario");
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error SQL al crear usuario: " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of("success", true, "user", u));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> borrarUsuario(@PathVariable int id) {
        String sqlDeleteProgreso = "DELETE FROM Respuesta_progreso WHERE id_intento IN (SELECT id_intento FROM Intento_Test WHERE id_aspirante = ?)";
        String sqlDeleteResultados = "DELETE FROM Resultado_axiologico WHERE id_intento IN (SELECT id_intento FROM Intento_Test WHERE id_aspirante = ?)";
        String sqlDeleteIntentos = "DELETE FROM Intento_Test WHERE id_aspirante = ?";
        String sqlDeleteAspirante = "DELETE FROM Aspirante WHERE id_aspirante = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteProgreso)) { stmt.setInt(1, id); stmt.executeUpdate(); }
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteResultados)) { stmt.setInt(1, id); stmt.executeUpdate(); }
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteIntentos)) { stmt.setInt(1, id); stmt.executeUpdate(); }
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteAspirante)) { stmt.setInt(1, id); stmt.executeUpdate(); }
                conn.commit();
                return ResponseEntity.ok(Map.of("success", true));
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error SQL al eliminar usuario en cascada: " + e.getMessage());
        }
    }

    @PostMapping("/preguntas/{parte}")
    public ResponseEntity<?> añadirPregunta(@PathVariable String parte, @RequestBody Map<String, Object> body) {
        String seccion = parte.equals("p1") ? "Parte 1" : "Parte 2";
        String sqlMax = "SELECT ISNULL(MAX(id_item), 0) + 1 FROM Item_cuestionario";
        String sqlIns = "INSERT INTO Item_cuestionario (id_item, texto_item, seccion, valor_asociado, dimension_allport) VALUES (?, ?, ?, 0, 'N/A')";
        String sqlInsOpciones = "INSERT INTO opcion_respuesta (id_item, texto_opcion, letra_opcion) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);
            try {
                int nextId;
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlMax)) {
                    rs.next();
                    nextId = rs.getInt(1);
                }

                String textoItem = String.valueOf(body.get("texto_item") != null ? body.get("texto_item") : body.get("texto"));
                try (PreparedStatement stmt = conn.prepareStatement(sqlIns)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, textoItem);
                    stmt.setString(3, seccion);
                    stmt.executeUpdate();
                }

                if (parte.equals("p2")) {
                    try (PreparedStatement stmtOpc = conn.prepareStatement(sqlInsOpciones)) {
                        String[] letras = {"a", "b", "c", "d"};
                        Map<String, Object> opcionesFormulario = (Map<String, Object>) body.get("opciones_nuevas");
                        List<String> opcionesLista = (List<String>) body.get("opciones");

                        for (String letra : letras) {
                            String textoOpcionReal = "";
                            if (opcionesFormulario != null && opcionesFormulario.containsKey(letra)) {
                                textoOpcionReal = String.valueOf(opcionesFormulario.get(letra));
                            } else if (opcionesLista != null) {
                                int index = letra.charAt(0) - 'a';
                                if (index < opcionesLista.size()) {
                                    String str = opcionesLista.get(index);
                                    textoOpcionReal = str.contains(". ") ? str.substring(str.indexOf(". ") + 2) : str;
                                }
                            }

                            if (textoOpcionReal == null || textoOpcionReal.trim().isEmpty() || textoOpcionReal.equals("null")) {
                                textoOpcionReal = "Opción " + letra.toUpperCase();
                            }

                            stmtOpc.setInt(1, nextId);
                            stmtOpc.setString(2, textoOpcionReal.trim());
                            stmtOpc.setString(3, letra);
                            stmtOpc.addBatch();
                        }
                        stmtOpc.executeBatch();
                    }
                }

                conn.commit();
                return ResponseEntity.ok(Map.of("success", true));
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error SQL al añadir pregunta con opciones reales: " + e.getMessage());
        }
    }

    @PutMapping("/preguntas/{parte}/{id}")
    public ResponseEntity<?> editarPregunta(@PathVariable String parte, @PathVariable int id, @RequestBody Map<String, Object> body) {
        // 🎯 CORRECCIÓN: Si es Parte 2, el ID real en la BD siempre viene desfasado por +31 sin importar si es menor a 15
        int idReal = parte.equals("p2") ? id + 31 : id;

        String sqlUpdatePregunta = "UPDATE Item_cuestionario SET texto_item = ? WHERE id_item = ?";
        String sqlUpdateOpcion = "UPDATE opcion_respuesta SET texto_opcion = ? WHERE id_item = ? AND letra_opcion = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);
            try {
                if (body.get("texto_item") != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlUpdatePregunta)) {
                        stmt.setString(1, String.valueOf(body.get("texto_item")));
                        stmt.setInt(2, idReal);
                        stmt.executeUpdate();
                    }
                }

                if (body.get("opciones") != null) {
                    List<String> opciones = (List<String>) body.get("opciones");
                    try (PreparedStatement stmtOpc = conn.prepareStatement(sqlUpdateOpcion)) {
                        for (String opc : opciones) {
                            if (opc != null && opc.contains(". ")) {
                                String letra = opc.split("\\. ")[0].trim().toLowerCase();
                                String texto = opc.substring(opc.indexOf(". ") + 2).trim();

                                stmtOpc.setString(1, texto);
                                stmtOpc.setInt(2, idReal);
                                stmtOpc.setString(3, letra);
                                stmtOpc.addBatch();
                            }
                        }
                        stmtOpc.executeBatch();
                    }
                }

                conn.commit();
                return ResponseEntity.ok(Map.of("success", true));
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error SQL al modificar pregunta u opciones: " + e.getMessage());
        }
    }

    @DeleteMapping("/preguntas/{parte}/{id}")
    public ResponseEntity<?> removerPregunta(@PathVariable String parte, @PathVariable int id) {
        // 🎯 CORRECCIÓN MUTUA: Sincroniza el ID real sumando 31 siempre que la petición provenga de la Parte 2
        int idReal = parte.equals("p2") ? id + 31 : id;

        String sqlDeleteProgreso = "DELETE FROM Respuesta_progreso WHERE id_item = ?";
        String sqlDeleteOpciones = "DELETE FROM opcion_respuesta WHERE id_item = ?";
        String sqlDeleteItem = "DELETE FROM Item_cuestionario WHERE id_item = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);
            try {
                // 1. Limpiar respuestas de alumnos si ya respondieron este ítem de prueba
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteProgreso)) {
                    stmt.setInt(1, idReal);
                    stmt.executeUpdate();
                }

                // 2. Limpiar las 4 opciones de la pregunta (a, b, c, d)
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteOpciones)) {
                    stmt.setInt(1, idReal);
                    stmt.executeUpdate();
                }

                // 3. Borrar la pregunta raíz del cuestionario
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteItem)) {
                    stmt.setInt(1, idReal);
                    stmt.executeUpdate();
                }

                conn.commit();
                return ResponseEntity.ok(Map.of("success", true));
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error SQL al remover pregunta e incisos en cascada: " + e.getMessage());
        }
    }
}