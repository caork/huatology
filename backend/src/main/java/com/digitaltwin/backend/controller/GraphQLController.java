package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.model.Action;
import com.digitaltwin.backend.model.Link;
import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.service.ActionService;
import com.digitaltwin.backend.service.LinkService;
import com.digitaltwin.backend.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GraphQLController {

    @Autowired
    private ObjectService objectService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private ActionService actionService;

    // Object Queries
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<ObjectEntity> objects(@Argument String type, @Argument Integer limit) {
        if (type != null) {
            List<ObjectEntity> objects = objectService.getObjectsByType(type);
            if (limit != null && limit > 0) {
                return objects.subList(0, Math.min(limit, objects.size()));
            }
            return objects;
        }
        List<ObjectEntity> allObjects = objectService.getAllObjects();
        if (limit != null && limit > 0) {
            return allObjects.subList(0, Math.min(limit, allObjects.size()));
        }
        return allObjects;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ObjectEntity object(@Argument String id) {
        Optional<ObjectEntity> obj = objectService.getObjectById(id);
        return obj.orElse(null);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<ObjectEntity> connectedObjects(@Argument String id, @Argument Integer depth) {
        if (depth == null) depth = 2;
        return objectService.getConnectedObjects(id, depth);
    }

    // Link Queries
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Link> links(@Argument String type) {
        if (type != null) {
            return linkService.getLinksByType(type);
        }
        return linkService.getAllLinks();
    }

    // Action Queries
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Action> actions(@Argument String objectId, @Argument String user) {
        if (objectId != null) {
            return actionService.getActionsByObjectId(objectId);
        }
        if (user != null) {
            return actionService.getActionsByUser(user);
        }
        return actionService.getAllActions();
    }

    // Object Mutations
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ObjectEntity createObject(@Argument Map<String, Object> input) {
        String type = (String) input.get("type");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) input.get("properties");

        ObjectEntity object = new ObjectEntity();
        object.setType(type);
        object.setProperties(properties);

        return objectService.saveObject(object);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ObjectEntity updateObject(@Argument String id, @Argument Map<String, Object> input) {
        Optional<ObjectEntity> existing = objectService.getObjectById(id);
        if (existing.isEmpty()) {
            return null;
        }

        ObjectEntity object = existing.get();
        if (input.containsKey("type")) {
            object.setType((String) input.get("type"));
        }
        if (input.containsKey("properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) input.get("properties");
            object.setProperties(properties);
        }

        return objectService.saveObject(object);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteObject(@Argument String id) {
        try {
            objectService.deleteObject(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Link Mutations
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Link createLink(@Argument Map<String, Object> input) {
        String type = (String) input.get("type");
        String sourceId = (String) input.get("sourceId");
        String targetId = (String) input.get("targetId");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) input.get("properties");

        return linkService.createLink(type, sourceId, targetId, properties);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteLink(@Argument String id) {
        try {
            linkService.deleteLink(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Action Mutations
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Action createAction(@Argument Map<String, Object> input) {
        String type = (String) input.get("type");
        String objectId = (String) input.get("objectId");
        @SuppressWarnings("unchecked")
        Map<String, Object> changes = (Map<String, Object>) input.get("changes");
        String user = (String) input.get("user");

        return actionService.createAction(type, objectId, changes, user);
    }
}