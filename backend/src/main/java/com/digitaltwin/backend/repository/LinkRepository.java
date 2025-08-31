package com.digitaltwin.backend.repository;

import com.digitaltwin.backend.model.Link;
import com.digitaltwin.backend.model.ObjectEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkRepository extends Neo4jRepository<ObjectEntity, String> {

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE r.type = $type RETURN source, collect(r), collect(target)")
    List<ObjectEntity> findLinksByType(String type);

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE source.id = $sourceId RETURN r, target")
    List<Link> findLinksBySourceId(String sourceId);

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE target.id = $targetId RETURN r, source")
    List<Link> findLinksByTargetId(String targetId);

    @Query("MATCH (source:Object)-[r:LINKS_TO]->(target:Object) WHERE r.id = $linkId RETURN r")
    Link findLinkById(String linkId);
}
