package com.digitaltwin.backend.repository;

import com.digitaltwin.backend.model.Link;
import com.digitaltwin.backend.model.ObjectEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkRepository extends Neo4jRepository<Link, Long> {

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE r.type = $type RETURN r")
    List<Link> findLinksByType(String type);

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE elementId(source) = $sourceId RETURN r")
    List<Link> findLinksBySourceId(String sourceId);

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE elementId(target) = $targetId RETURN r")
    List<Link> findLinksByTargetId(String targetId);

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE elementId(r) = $linkId RETURN r")
    Link findLinkById(String linkId);
}
