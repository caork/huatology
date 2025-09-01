package com.digitaltwin.backend.service;

import com.digitaltwin.backend.model.Link;
import com.digitaltwin.backend.model.ObjectEntity;
import com.digitaltwin.backend.repository.LinkRepository;
import com.digitaltwin.backend.repository.ObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LinkService {

    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ObjectRepository objectRepository;

    public List<Link> getAllLinks() {
        return linkRepository.findAllLinks();
    }

    public Optional<Link> getLinkById(String id) {
        return Optional.ofNullable(linkRepository.findLinkById(id));
    }

    public List<Link> getLinksByType(String type) {
        return linkRepository.findLinksByType(type);
    }

    public List<Link> getLinksBySourceId(String sourceId) {
        return linkRepository.findLinksBySourceId(sourceId);
    }

    public List<Link> getLinksByTargetId(String targetId) {
        return linkRepository.findLinksByTargetId(targetId);
    }

    public Link createLink(String type, String sourceId, String targetId, java.util.Map<String, Object> properties) {
        logger.info("Creating link with type: {}, sourceId: {}, targetId: {}, properties: {}", type, sourceId, targetId, properties);

        Optional<ObjectEntity> source = objectRepository.findById(sourceId);
        Optional<ObjectEntity> target = objectRepository.findById(targetId);

        if (source.isEmpty() || target.isEmpty()) {
            throw new IllegalArgumentException("Source or target object not found");
        }

        Link link = new Link();
        link.setId(UUID.randomUUID().toString());
        link.setType(type);
        link.setSource(source.get());
        link.setTarget(target.get());
        link.setProperties(properties);

        // Save the relationship entity directly
        return linkRepository.save(link);
    }

    public Link saveLink(Link link) {
        return linkRepository.save(link);
    }

    public void deleteLink(String id) {
        // Find the link by its elementId and delete it
        Link link = linkRepository.findLinkById(id);
        if (link != null) {
            linkRepository.delete(link);
        }
    }
}
