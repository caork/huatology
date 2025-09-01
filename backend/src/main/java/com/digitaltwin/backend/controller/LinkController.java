package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.model.Link;
import com.digitaltwin.backend.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/links")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class LinkController {

    @Autowired
    private LinkService linkService;

    @GetMapping
    public List<Link> getAllLinks() {
        return linkService.getAllLinks();
    }

    @GetMapping("/{id}")
    public Optional<Link> getLinkById(@PathVariable String id) {
        return linkService.getLinkById(id);
    }

    @GetMapping("/type/{type}")
    public List<Link> getLinksByType(@PathVariable String type) {
        return linkService.getLinksByType(type);
    }

    @GetMapping("/source/{sourceId}")
    public List<Link> getLinksBySourceId(@PathVariable String sourceId) {
        return linkService.getLinksBySourceId(sourceId);
    }

    @GetMapping("/target/{targetId}")
    public List<Link> getLinksByTargetId(@PathVariable String targetId) {
        return linkService.getLinksByTargetId(targetId);
    }

    @PostMapping
    public Link createLink(@RequestBody Link link) {
        return linkService.saveLink(link);
    }

    @PutMapping("/{id}")
    public Link updateLink(@PathVariable String id, @RequestBody Link link) {
        link.setId(id);
        return linkService.saveLink(link);
    }

    @DeleteMapping("/{id}")
    public void deleteLink(@PathVariable String id) {
        linkService.deleteLink(id);
    }
}
