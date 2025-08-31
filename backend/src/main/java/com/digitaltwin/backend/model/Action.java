package com.digitaltwin.backend.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;
import java.util.Map;

@Node("Action")
public class Action {

    @Id
    private String id;

    @Property
    private String type;

    @Property
    private String objectId;

    @Property
    private Map<String, Object> changes;

    @Property
    private LocalDateTime timestamp;

    @Property
    private String user;

    // Constructors, getters, setters

    public Action() {}

    public Action(String id, String type, String objectId, Map<String, Object> changes, LocalDateTime timestamp, String user) {
        this.id = id;
        this.type = type;
        this.objectId = objectId;
        this.changes = changes;
        this.timestamp = timestamp;
        this.user = user;
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

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Map<String, Object> getChanges() {
        return changes;
    }

    public void setChanges(Map<String, Object> changes) {
        this.changes = changes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
