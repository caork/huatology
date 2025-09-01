package com.digitaltwin.backend.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.util.HashMap;
import java.util.Map;

@RelationshipProperties
public class Link {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue
    private Long internalId;

    private String id;

    private String type; // LinkType name

    private ObjectEntity source;

    @TargetNode
    private ObjectEntity target;

    @Property
    private String propertiesJson;

    // Constructors, getters, setters

    public Link() {}

    public Link(String id, String type, ObjectEntity source, ObjectEntity target, Map<String, Object> properties) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.target = target;
        setProperties(properties);
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ObjectEntity getSource() {
        return source;
    }

    public void setSource(ObjectEntity source) {
        this.source = source;
    }

    public ObjectEntity getTarget() {
        return target;
    }

    public void setTarget(ObjectEntity target) {
        this.target = target;
    }

    public Map<String, Object> getProperties() {
        if (propertiesJson == null || propertiesJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(propertiesJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse properties JSON", e);
        }
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties == null) {
            this.propertiesJson = null;
        } else {
            try {
                this.propertiesJson = objectMapper.writeValueAsString(properties);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize properties to JSON", e);
            }
        }
    }
}
