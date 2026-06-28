package com.allport.controller;

import com.allport.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    // 🔍 CAPTURAMOS EL NUEVO USUARIO Y CONTRASEÑA DE LAS PROPIEDADES
    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Map<String, Object> res = new HashMap<>();

        String sql = "SELECT id_aspirante, nombre_aspirante, cif, rol FROM Aspirante WHERE cif = ? AND password_hash = ?";

        // 🔍 PASAMOS dbUser Y dbPassword EXPLÍCITAMENTE A LA CONEXIÓN
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, req.getCif());
            stmt.setString(2, req.getPassword());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id_aspirante", rs.getInt("id_aspirante"));
                    user.put("nombre_aspirante", rs.getString("nombre_aspirante"));
                    user.put("cif", rs.getString("cif"));
                    user.put("rol", rs.getString("rol"));

                    res.put("success", true);
                    res.put("user", user);

                    return ResponseEntity.ok(res);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> errorJson = new HashMap<>();
            errorJson.put("success", false);
            errorJson.put("message", "Error interno en SQL Server: " + e.getMessage());
            return ResponseEntity.status(500).body(errorJson);
        }

        res.put("success", false);
        res.put("message", "Credenciales incorrectas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }
}