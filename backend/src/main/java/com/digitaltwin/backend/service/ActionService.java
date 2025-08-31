package com.digitaltwin.backend.service;

import com.digitaltwin.backend.model.Action;
import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.repository.ActionRepository;
import com.digitaltwin.backend.repository.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ActionService {

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ObjectRepository objectRepository;

    public List<Action> getAllActions() {
        return actionRepository.findAll();
    }

    public Optional<Action> getActionById(String id) {
        return actionRepository.findById(id);
    }

    public List<Action> getActionsByObjectId(String objectId) {
        return actionRepository.findByObjectId(objectId);
    }

    public Action performAction(String actionType, String objectId, Map<String, Object> changes, String user) {
        // Log the action
        Action action = new Action();
        action.setId(java.util.UUID.randomUUID().toString());
        action.setType(actionType);
        action.setObjectId(objectId);
        action.setChanges(changes);
        action.setTimestamp(LocalDateTime.now());
        action.setUser(user);

        // Apply changes to object
        Optional<ObjectEntity> objOpt = objectRepository.findById(objectId);
        if (objOpt.isPresent()) {
            ObjectEntity obj = objOpt.get();
            obj.getProperties().putAll(changes);
            objectRepository.save(obj);
        }

        return actionRepository.save(action);
    }

    public Action saveAction(Action action) {
        return actionRepository.save(action);
    }

    public void deleteAction(String id) {
        actionRepository.deleteById(id);
    }
}
