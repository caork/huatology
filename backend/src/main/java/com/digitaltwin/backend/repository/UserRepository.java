package com.digitaltwin.backend.repository;

import com.digitaltwin.backend.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("MATCH (u:User) WHERE u.username = $username AND u.enabled = true RETURN u")
    Optional<User> findByUsernameAndEnabled(String username);

    @Query("MATCH (u:User) WHERE u.email = $email AND u.enabled = true RETURN u")
    Optional<User> findByEmailAndEnabled(String email);

    @Query("MATCH (u:User) WHERE u.username = $username OR u.email = $username AND u.enabled = true RETURN u")
    Optional<User> findByUsernameOrEmailAndEnabled(String username);
}