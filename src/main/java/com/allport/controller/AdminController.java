package com.allport.controller;

import com.allport.model.Usuario;
import com.allport.model.ContainerPreguntas;
import com.allport.model.PreguntaItem;
import com.allport.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private FileStorageService storage;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> getUsuariosMaster() { return ResponseEntity.ok(storage.obtenerUsuarios()); }

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario u) {
        List<Usuario> lista = storage.obtenerUsuarios();
        int nextId = lista.stream().mapToInt(Usuario::getId_aspirante).max().orElse(0) + 1;
        u.setId_aspirante(nextId);
        lista.add(u);
        storage.guardarUsuarios(lista);
        return ResponseEntity.ok(Map.of("success", true, "user", u));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> borrarUsuario(@PathVariable int id) {
        List<Usuario> lista = storage.obtenerUsuarios();
        lista.removeIf(u -> u.getId_aspirante() == id);
        storage.guardarUsuarios(lista);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/preguntas/{parte}")
    public ResponseEntity<?> añadirPregunta(@PathVariable String parte, @RequestBody PreguntaItem item) {
        ContainerPreguntas dbs = storage.obtenerPreguntas();
        if (parte.equals("p1")) {
            int nId = dbs.getDb_preguntas_p1().stream().mapToInt(PreguntaItem::getId_item).max().orElse(0) + 1;
            item.setId_item(nId);
            item.setSeccion("Parte 1");
            dbs.getDb_preguntas_p1().add(item);
        } else {
            int nId = dbs.getDb_preguntas_p2().stream().mapToInt(PreguntaItem::getId_item).max().orElse(0) + 1;
            item.setId_item(nId);
            dbs.getDb_preguntas_p2().add(item);
        }
        storage.guardarPreguntas(dbs);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/preguntas/{parte}/{id}")
    public ResponseEntity<?> editarPregunta(@PathVariable String parte, @PathVariable int id, @RequestBody PreguntaItem body) {
        ContainerPreguntas dbs = storage.obtenerPreguntas();
        if (parte.equals("p1")) {
            dbs.getDb_preguntas_p1().stream().filter(p -> p.getId_item() == id).findFirst().ifPresent(p -> p.setTexto_item(body.getTexto_item()));
        } else {
            dbs.getDb_preguntas_p2().stream().filter(p -> p.getId_item() == id).findFirst().ifPresent(p -> {
                p.setTexto_item(body.getTexto_item());
                if (body.getOpciones() != null) p.setOpciones(body.getOpciones());
            });
        }
        storage.guardarPreguntas(dbs);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/preguntas/{parte}/{id}")
    public ResponseEntity<?> removerPregunta(@PathVariable String parte, @PathVariable int id) {
        ContainerPreguntas dbs = storage.obtenerPreguntas();
        if (parte.equals("p1")) dbs.getDb_preguntas_p1().removeIf(p -> p.getId_item() == id);
        else dbs.getDb_preguntas_p2().removeIf(p -> p.getId_item() == id);
        storage.guardarPreguntas(dbs);
        return ResponseEntity.ok(Map.of("success", true));
    }
}