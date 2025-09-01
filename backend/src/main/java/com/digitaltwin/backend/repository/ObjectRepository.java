package com.digitaltwin.backend.repository;

import com.digitaltwin.backend.model.ObjectEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectRepository extends Neo4jRepository<ObjectEntity, String> {

    List<ObjectEntity> findByType(String type);

    @Query("MATCH (o:Object) WHERE o.properties[$key] = $value RETURN o")
    List<ObjectEntity> findByPropertiesContaining(String key, Object value);

    @Query("MATCH (o:Object)-[r:LINKS_TO]->(target:Object) WHERE elementId(o) = $id RETURN o, collect(r), collect(target)")
    Optional<ObjectEntity> findByIdWithRelationships(String id);

    @Query("MATCH (o:Object)-[r:LINKS_TO*1..3]-(connected:Object) WHERE elementId(o) = $id RETURN DISTINCT connected")
    List<ObjectEntity> findConnectedObjects(String id, int depth);

    @Query("MATCH (o:Object)-[r:LINKS_TO]-(connected:Object) WHERE elementId(o) = $id AND r.type = $linkType RETURN connected")
    List<ObjectEntity> findConnectedObjectsByLinkType(String id, String linkType);
}
