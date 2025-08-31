package com.digitaltwin.backend.repository;

import com.digitaltwin.backend.model.Action;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends Neo4jRepository<Action, String> {

    List<Action> findByObjectId(String objectId);

    List<Action> findByType(String type);

    List<Action> findByUser(String user);
}
