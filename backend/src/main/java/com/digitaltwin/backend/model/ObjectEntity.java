package com.digitaltwin.backend.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.Map;

@Node("Object")
public class ObjectEntity {

    @Id
    private String id;

    @Property
    private String type; // ObjectType name

    @Property
    private Map<String, Object> properties;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.OUTGOING)
    private List<Link> outgoingLinks;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.INCOMING)
    private List<Link> incomingLinks;

    // Constructors, getters, setters

    public ObjectEntity() {}

    public ObjectEntity(String id, String type, Map<String, Object> properties) {
        this.id = id;
        this.type = type;
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
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
