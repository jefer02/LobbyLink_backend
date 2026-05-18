package com.lobbylink.api.model;

/**
 * Lifecycle states of a Lobby.
 * <ul>
 *   <li>{@link #PUBLISHED}  – open and accepting new participants</li>
 *   <li>{@link #FULL}       – max player capacity reached</li>
 *   <li>{@link #CANCELLED}  – cancelled by the creator</li>
 * </ul>
 */
public enum LobbyStatus {
    PUBLISHED,
    FULL,
    CANCELLED
}
