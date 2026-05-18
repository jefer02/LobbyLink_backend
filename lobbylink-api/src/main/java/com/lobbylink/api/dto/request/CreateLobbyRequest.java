package com.lobbylink.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request body for creating a new lobby.
 */
@Data
public class CreateLobbyRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Game is required")
    private String game;

    @NotNull(message = "Scheduled date and time is required")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Max players is required")
    @Min(value = 2,   message = "A lobby must allow at least 2 players")
    @Max(value = 100, message = "A lobby cannot exceed 100 players")
    private Integer maxPlayers;

    /** Optional Discord invite link for coordination */
    private String discordLink;
}
