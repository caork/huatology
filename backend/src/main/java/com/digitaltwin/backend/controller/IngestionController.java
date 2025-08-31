package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.service.IngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingestion")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class IngestionController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping("/structured")
    public ResponseEntity<List<ObjectEntity>> ingestStructuredData(
            @RequestParam String sourceType,
            @RequestBody List<Map<String, Object>> data) {
        try {
            List<ObjectEntity> ingested = ingestionService.ingestStructuredData(sourceType, data);
            return ResponseEntity.ok(ingested);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/unstructured")
    public ResponseEntity<List<ObjectEntity>> ingestUnstructuredData(
            @RequestParam String sourceType,
            @RequestBody String text) {
        try {
            List<ObjectEntity> extracted = ingestionService.ingestUnstructuredData(text, sourceType);
            return ResponseEntity.ok(extracted);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<IngestionService.IngestionResult> batchIngest(
            @RequestBody List<Map<String, Object>> batchData,
            @RequestParam String objectType) {
        try {
            IngestionService.IngestionResult result = ingestionService.batchIngest(batchData, objectType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/relationships")
    public ResponseEntity<String> createRelationships(@RequestBody List<ObjectEntity> objects) {
        try {
            ingestionService.createRelationshipsFromIngestedData(objects);
            return ResponseEntity.ok("Relationships created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating relationships: " + e.getMessage());
        }
    }
}