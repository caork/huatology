package com.digitaltwin.backend.service;

import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.repository.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IngestionService {

    @Autowired
    private ObjectRepository objectRepository;

    @Autowired
    private ObjectService objectService;

    /**
     * Ingest structured data from various sources
     */
    public List<ObjectEntity> ingestStructuredData(String sourceType, List<Map<String, Object>> data) {
        List<ObjectEntity> ingestedObjects = new ArrayList<>();

        for (Map<String, Object> item : data) {
            ObjectEntity object = new ObjectEntity();
            object.setId(UUID.randomUUID().toString());
            object.setType(sourceType);
            object.setProperties(item);
            ingestedObjects.add(objectService.saveObject(object));
        }

        return ingestedObjects;
    }

    /**
     * Ingest unstructured text data with entity extraction
     * This is a placeholder for LLM-powered entity extraction
     */
    public List<ObjectEntity> ingestUnstructuredData(String text, String sourceType) {
        List<ObjectEntity> extractedEntities = new ArrayList<>();

        // Placeholder for LLM entity extraction
        // In a real implementation, this would call an LLM service to extract entities
        List<Map<String, Object>> mockEntities = extractEntitiesWithLLM(text);

        for (Map<String, Object> entity : mockEntities) {
            ObjectEntity object = new ObjectEntity();
            object.setId(UUID.randomUUID().toString());
            object.setType(sourceType);
            object.setProperties(entity);
            extractedEntities.add(objectService.saveObject(object));
        }

        return extractedEntities;
    }

    /**
     * Mock LLM entity extraction - replace with actual LLM integration
     */
    private List<Map<String, Object>> extractEntitiesWithLLM(String text) {
        // This is a placeholder implementation
        // In production, this would call an LLM API (OpenAI, Anthropic, etc.)

        List<Map<String, Object>> entities = new ArrayList<>();

        // Simple keyword-based extraction as placeholder
        String[] keywords = {"person", "company", "product", "location", "event"};

        for (String keyword : keywords) {
            if (text.toLowerCase().contains(keyword)) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("name", keyword + "_extracted");
                entity.put("type", keyword);
                entity.put("confidence", 0.85);
                entity.put("source_text", text.substring(0, Math.min(100, text.length())));
                entities.add(entity);
            }
        }

        return entities;
    }

    /**
     * Create relationships between ingested objects based on common properties
     */
    public void createRelationshipsFromIngestedData(List<ObjectEntity> objects) {
        // Group objects by type
        Map<String, List<ObjectEntity>> objectsByType = objects.stream()
                .collect(Collectors.groupingBy(ObjectEntity::getType));

        // Create relationships between related objects
        for (Map.Entry<String, List<ObjectEntity>> entry : objectsByType.entrySet()) {
            String type = entry.getKey();
            List<ObjectEntity> typeObjects = entry.getValue();

            // Create relationships within the same type if they share properties
            for (int i = 0; i < typeObjects.size(); i++) {
                for (int j = i + 1; j < typeObjects.size(); j++) {
                    ObjectEntity obj1 = typeObjects.get(i);
                    ObjectEntity obj2 = typeObjects.get(j);

                    if (haveCommonProperties(obj1, obj2)) {
                        createRelationship(obj1.getId(), obj2.getId(), "RELATED_TO",
                                Map.of("relationship_type", "common_properties"));
                    }
                }
            }
        }
    }

    private boolean haveCommonProperties(ObjectEntity obj1, ObjectEntity obj2) {
        Set<String> keys1 = obj1.getProperties().keySet();
        Set<String> keys2 = obj2.getProperties().keySet();

        return !Collections.disjoint(keys1, keys2);
    }

    private void createRelationship(String sourceId, String targetId, String type, Map<String, Object> properties) {
        // This would use the LinkService to create relationships
        // For now, this is a placeholder
    }

    /**
     * Batch ingest data with progress tracking
     */
    public IngestionResult batchIngest(List<Map<String, Object>> batchData, String objectType) {
        IngestionResult result = new IngestionResult();
        result.setTotalRecords(batchData.size());

        List<ObjectEntity> successful = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> data : batchData) {
            try {
                ObjectEntity object = new ObjectEntity();
                object.setId(UUID.randomUUID().toString());
                object.setType(objectType);
                object.setProperties(data);
                successful.add(objectService.saveObject(object));
            } catch (Exception e) {
                errors.add("Failed to ingest record: " + e.getMessage());
            }
        }

        result.setSuccessfulRecords(successful.size());
        result.setFailedRecords(errors.size());
        result.setErrors(errors);
        result.setIngestedObjects(successful);

        return result;
    }

    public static class IngestionResult {
        private int totalRecords;
        private int successfulRecords;
        private int failedRecords;
        private List<String> errors;
        private List<ObjectEntity> ingestedObjects;

        // Getters and setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

        public int getSuccessfulRecords() { return successfulRecords; }
        public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }

        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }

        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }

        public List<ObjectEntity> getIngestedObjects() { return ingestedObjects; }
        public void setIngestedObjects(List<ObjectEntity> ingestedObjects) { this.ingestedObjects = ingestedObjects; }
    }
}