package com.lobbylink.api.repository;

import com.lobbylink.api.model.Lobby;
import com.lobbylink.api.model.LobbyStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Lobby documents.
 * MongoRepository provides CRUD operations out of the box.
 * Method names follow Spring Data naming conventions — no custom queries needed.
 */
@Repository
public interface LobbyRepository extends MongoRepository<Lobby, String> {

    /** Returns all lobbies for a specific game (case-sensitive). */
    List<Lobby> findByGame(String game);

    /** Returns all lobbies in the given lifecycle status. */
    List<Lobby> findByStatus(LobbyStatus status);

    /** Returns all lobbies created by a specific user. */
    List<Lobby> findByCreatorId(String creatorId);

    /** Checks whether a user is already a participant in any lobby with the given status. */
    boolean existsByParticipantsContainingAndStatus(String userId, LobbyStatus status);
}
