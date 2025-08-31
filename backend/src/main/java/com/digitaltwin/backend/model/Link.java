package com.digitaltwin.backend.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.util.Map;

@RelationshipProperties
public class Link {

    @Id
    private String id;

    private String type; // LinkType name

    @TargetNode
    private ObjectEntity target;

    private Map<String, Object> properties;

    // Constructors, getters, setters

    public Link() {}

    public Link(String id, String type, ObjectEntity target, Map<String, Object> properties) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.properties = properties;
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

    public ObjectEntity getTarget() {
        return target;
    }

    public void setTarget(ObjectEntity target) {
        this.target = target;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
