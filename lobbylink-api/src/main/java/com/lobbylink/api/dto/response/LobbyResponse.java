package com.lobbylink.api.dto.response;

import com.lobbylink.api.model.LobbyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body returned for lobby read operations.
 * Never exposes internal MongoDB document details beyond what is needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LobbyResponse {

    private String id;
    private String title;
    private String game;
    private LocalDateTime scheduledAt;
    private int maxPlayers;

    /** Gamertag of the user who created the lobby */
    private String creatorGamertag;

    /** List of gamertags of players who have joined */
    private List<String> participantGamertags;

    private String discordLink;
    private LobbyStatus status;

    /** Derived field: number of current participants */
    private int currentPlayers;
}
