package com.lobbylink.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a gaming lobby created by a user.
 * Participants are stored as a list of User IDs (references).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lobbies")
public class Lobby {

    @Id
    private String id;

    /** Short descriptive title for the lobby */
    private String title;

    /** Name of the game the lobby is for */
    private String game;

    /** Date and time when the gaming session is planned to start */
    private LocalDateTime scheduledAt;

    /** Maximum number of players allowed in the lobby */
    private int maxPlayers;

    /** ID of the User who created this lobby */
    private String creatorId;

    /** Ordered list of User IDs who have joined the lobby */
    private List<String> participants;

    /** Optional Discord invite link for voice/text coordination */
    private String discordLink;

    /** Current lifecycle status of the lobby */
    private LobbyStatus status;
}
