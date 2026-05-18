package com.lobbylink.api.service;

import com.lobbylink.api.dto.request.CreateLobbyRequest;
import com.lobbylink.api.dto.response.LobbyResponse;
import com.lobbylink.api.exception.BadRequestException;
import com.lobbylink.api.exception.ForbiddenException;
import com.lobbylink.api.exception.ResourceNotFoundException;
import com.lobbylink.api.model.Lobby;
import com.lobbylink.api.model.LobbyStatus;
import com.lobbylink.api.model.User;
import com.lobbylink.api.repository.LobbyRepository;
import com.lobbylink.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for lobby creation, listing, joining, and cancellation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final UserRepository  userRepository;

    // ─── Create ──────────────────────────────────────────────────────────────

    /**
     * Creates a new lobby. The creator is automatically added as the first participant.
     *
     * @param request      DTO with lobby details
     * @param creatorEmail email resolved from the JWT by the controller
     */
    public LobbyResponse create(CreateLobbyRequest request, String creatorEmail) {
        String creatorId = resolveUserId(creatorEmail);
        List<String> participants = new ArrayList<>();
        participants.add(creatorId);

        Lobby lobby = Lobby.builder()
                .title(request.getTitle())
                .game(request.getGame())
                .scheduledAt(request.getScheduledAt())
                .maxPlayers(request.getMaxPlayers())
                .creatorId(creatorId)
                .participants(participants)
                .discordLink(request.getDiscordLink())
                .status(LobbyStatus.PUBLISHED)
                .build();

        lobbyRepository.save(lobby);
        log.info("Lobby created: {} by user {}", lobby.getId(), creatorId);

        return toResponse(lobby, creatorId);
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    /**
     * Returns all lobbies, optionally filtered by game.
     * The discordLink is hidden unless the caller is the creator or a participant.
     *
     * @param game     optional game filter (null = no filter)
     * @param callerId the authenticated user's ID
     */
    public List<LobbyResponse> findAll(String game, String callerEmail) {
        String callerId = resolveUserId(callerEmail);
        List<Lobby> lobbies = (game != null && !game.isBlank())
                ? lobbyRepository.findByGame(game)
                : lobbyRepository.findAll();

        return lobbies.stream()
                .map(lobby -> toResponse(lobby, callerId))
                .toList();
    }

    // ─── Join ─────────────────────────────────────────────────────────────────

    /**
     * Adds the caller to a lobby's participant list.
     * Business rules enforced:
     * <ul>
     *   <li>Lobby must be PUBLISHED</li>
     *   <li>User cannot join twice</li>
     *   <li>If capacity is reached after joining, status becomes FULL</li>
     * </ul>
     */
    public LobbyResponse join(String lobbyId, String callerEmail) {
        String callerId = resolveUserId(callerEmail);
        Lobby lobby = findLobbyById(lobbyId);

        if (lobby.getStatus() != LobbyStatus.PUBLISHED) {
            throw new BadRequestException("Cannot join a lobby that is not PUBLISHED");
        }
        if (lobby.getParticipants().contains(callerId)) {
            throw new BadRequestException("You have already joined this lobby");
        }

        lobby.getParticipants().add(callerId);

        if (lobby.getParticipants().size() >= lobby.getMaxPlayers()) {
            lobby.setStatus(LobbyStatus.FULL);
            log.info("Lobby {} is now FULL", lobbyId);
        }

        lobbyRepository.save(lobby);
        log.info("User {} joined lobby {}", callerId, lobbyId);

        return toResponse(lobby, callerId);
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    /**
     * Cancels a lobby. Only the creator is allowed to perform this action.
     */
    public LobbyResponse cancel(String lobbyId, String callerEmail) {
        String callerId = resolveUserId(callerEmail);
        Lobby lobby = findLobbyById(lobbyId);

        if (!lobby.getCreatorId().equals(callerId)) {
            throw new ForbiddenException("Only the creator can cancel this lobby");
        }

        lobby.setStatus(LobbyStatus.CANCELLED);
        lobbyRepository.save(lobby);
        log.info("Lobby {} cancelled by user {}", lobbyId, callerId);

        return toResponse(lobby, callerId);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private String resolveUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getId();
    }

    private Lobby findLobbyById(String id) {
        return lobbyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lobby", "id", id));
    }

    /**
     * Maps a Lobby document to a LobbyResponse DTO.
     * Resolves participant IDs to gamertags and hides the discordLink
     * if the caller is neither the creator nor a participant.
     */
    private LobbyResponse toResponse(Lobby lobby, String callerId) {
        boolean isInsider = lobby.getCreatorId().equals(callerId)
                || lobby.getParticipants().contains(callerId);

        // Batch-load users to avoid N+1 queries
        List<User> users = userRepository.findAllById(lobby.getParticipants());
        Map<String, String> idToGamertag = users.stream()
                .collect(Collectors.toMap(User::getId, User::getGamertag));

        List<String> gamertags = lobby.getParticipants().stream()
                .map(id -> idToGamertag.getOrDefault(id, "Unknown"))
                .toList();

        // Creator is always in participants, so idToGamertag always contains them.
        String creatorGamertag = idToGamertag.getOrDefault(lobby.getCreatorId(), "Unknown");

        return LobbyResponse.builder()
                .id(lobby.getId())
                .title(lobby.getTitle())
                .game(lobby.getGame())
                .scheduledAt(lobby.getScheduledAt())
                .maxPlayers(lobby.getMaxPlayers())
                .creatorGamertag(creatorGamertag)
                .participantGamertags(gamertags)
                .discordLink(isInsider ? lobby.getDiscordLink() : null)
                .status(lobby.getStatus())
                .currentPlayers(lobby.getParticipants().size())
                .build();
    }
}
