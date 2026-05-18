package com.lobbylink.api.controller;

import com.lobbylink.api.dto.request.CreateLobbyRequest;
import com.lobbylink.api.dto.response.LobbyResponse;
import com.lobbylink.api.service.LobbyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for lobby management.
 * All endpoints require a valid JWT (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
@Tag(name = "Lobbies", description = "Create, browse and manage gaming lobbies")
@SecurityRequirement(name = "bearerAuth")
public class LobbyController {

    private final LobbyService lobbyService;

    // ─── POST /api/lobbies ────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new lobby")
    public ResponseEntity<LobbyResponse> create(
            @Valid @RequestBody CreateLobbyRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String creatorId = getCallerEmail(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lobbyService.create(request, creatorId));
    }

    // ─── GET /api/lobbies ─────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all lobbies, optionally filtered by game")
    public ResponseEntity<List<LobbyResponse>> findAll(
            @RequestParam(required = false) String game,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String callerId = getCallerEmail(principal);
        return ResponseEntity.ok(lobbyService.findAll(game, callerId));
    }

    // ─── POST /api/lobbies/{id}/join ──────────────────────────────────────────

    @PostMapping("/{id}/join")
    @Operation(summary = "Join an existing lobby")
    public ResponseEntity<LobbyResponse> join(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String callerId = getCallerEmail(principal);
        return ResponseEntity.ok(lobbyService.join(id, callerId));
    }

    // ─── PUT /api/lobbies/{id}/cancel ─────────────────────────────────────────

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a lobby (creator only)")
    public ResponseEntity<LobbyResponse> cancel(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String callerId = getCallerEmail(principal);
        return ResponseEntity.ok(lobbyService.cancel(id, callerId));
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    /** Returns the email stored as the JWT subject (Spring Security username). */
    private String getCallerEmail(UserDetails principal) {
        return principal.getUsername();
    }
}
