package com.digitaltwin.backend.service;

import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.repository.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObjectService {

    @Autowired
    private ObjectRepository objectRepository;

    public List<ObjectEntity> getAllObjects() {
        return objectRepository.findAll();
    }

    public Optional<ObjectEntity> getObjectById(String id) {
        return objectRepository.findById(id);
    }

    public Optional<ObjectEntity> getObjectByIdWithRelationships(String id) {
        return objectRepository.findByIdWithRelationships(id);
    }

    public List<ObjectEntity> getObjectsByType(String type) {
        return objectRepository.findByType(type);
    }

    public List<ObjectEntity> getConnectedObjects(String id, int depth) {
        return objectRepository.findConnectedObjects(id, depth);
    }

    public List<ObjectEntity> getConnectedObjectsByLinkType(String id, String linkType) {
        return objectRepository.findConnectedObjectsByLinkType(id, linkType);
    }

    public ObjectEntity saveObject(ObjectEntity object) {
        return objectRepository.save(object);
    }

    public void deleteObject(String id) {
        objectRepository.deleteById(id);
    }
}
