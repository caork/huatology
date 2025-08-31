package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/objects")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ObjectController {

    @Autowired
    private ObjectService objectService;

    @GetMapping
    public List<ObjectEntity> getAllObjects() {
        return objectService.getAllObjects();
    }

    @GetMapping("/{id}")
    public Optional<ObjectEntity> getObjectById(@PathVariable String id) {
        return objectService.getObjectById(id);
    }

    @GetMapping("/type/{type}")
    public List<ObjectEntity> getObjectsByType(@PathVariable String type) {
        return objectService.getObjectsByType(type);
    }

    @PostMapping
    public ObjectEntity createObject(@RequestBody ObjectEntity object) {
        return objectService.saveObject(object);
    }

    @PutMapping("/{id}")
    public ObjectEntity updateObject(@PathVariable String id, @RequestBody ObjectEntity object) {
        object.setId(id);
        return objectService.saveObject(object);
    }

    @DeleteMapping("/{id}")
    public void deleteObject(@PathVariable String id) {
        objectService.deleteObject(id);
    }
}
