package com.digitaltwin.backend.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Node("Object")
public class ObjectEntity {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private String id;

    @Property
    private String type; // ObjectType name

    @Property
    private String propertiesJson;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.OUTGOING)
    private List<Link> outgoingLinks;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.INCOMING)
    private List<Link> incomingLinks;

    // Constructors, getters, setters

    public ObjectEntity() {}

    public ObjectEntity(String id, String type, Map<String, Object> properties) {
        this.id = id;
        this.type = type;
        setProperties(properties);
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

    public List<Link> getOutgoingLinks() {
        return outgoingLinks;
    }

    public void setOutgoingLinks(List<Link> outgoingLinks) {
        this.outgoingLinks = outgoingLinks;
    }

    public List<Link> getIncomingLinks() {
        return incomingLinks;
    }

    public void setIncomingLinks(List<Link> incomingLinks) {
        this.incomingLinks = incomingLinks;
    }
}
