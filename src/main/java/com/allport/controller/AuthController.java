package com.allport.controller;

import com.allport.dto.LoginRequest;
import com.allport.model.Usuario;
import com.allport.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private FileStorageService storage;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Usuario> user = storage.obtenerUsuarios().stream()
                .filter(u -> u.getCif().equals(req.getCif()) && u.getPassword_hash().equals(req.getPassword()))
                .findFirst();

        Map<String, Object> res = new HashMap<>();
        if (user.isPresent()) {
            res.put("success", true);
            res.put("user", user.get());
            return ResponseEntity.ok(res);
        }
        res.put("success", false);
        res.put("message", "Credenciales inválidas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }
}