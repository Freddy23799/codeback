package codeqr.code.controller;

import codeqr.code.dto.PublishPayload;
import codeqr.code.dto.PublishResponse;
import codeqr.code.service.TimetablePublishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/timetables")
public class TimetableControllers {

    private final TimetablePublishService service;

    public TimetableControllers(TimetablePublishService service) {
        this.service = service;
    }

    // ========================= PUBLISH =========================
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody PublishPayload payload) {
        try {
            PublishResponse resp = service.publish(payload);
            return ResponseEntity.ok(resp);
        } 
        catch (IllegalArgumentException e) {
            // Conflits détectés (prof, salle, etc.)
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(parseConflictMessage(e.getMessage()));
        } 
        catch (Exception e) {
            // Erreurs imprévues
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur interne du serveur", e.getMessage()));
        }
    }

    // ========================= UPDATE =========================
    @PutMapping("/{semaineId}")
    public ResponseEntity<?> update(@PathVariable("semaineId") Long semaineId,
                                    @RequestBody PublishPayload payload) {
        try {
            PublishResponse resp = service.update(semaineId, payload);
            return ResponseEntity.ok(resp);
        } 
        catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(parseConflictMessage(e.getMessage()));
        } 
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur interne du serveur", e.getMessage()));
        }
    }

    // ========================= STRUCTURES =========================

    private static class ErrorResponse {
        private final String title;
        private final Object details;

        public ErrorResponse(String title, Object details) {
            this.title = title;
            this.details = details;
        }

        public String getTitle() { return title; }
        public Object getDetails() { return details; }
    }

    private static class ConflictDetail {
        private final String type;
        private final String message;

        public ConflictDetail(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
    }

    // ========================= FORMATAGE DES CONFLITS =========================

    private ErrorResponse parseConflictMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return new ErrorResponse("Conflits détectés", List.of());
        }

        // Nettoyage : on supprime "Conflits détectés :" si présent
        String clean = rawMessage.replace("Conflits détectés :", "").trim();

        // Découpage par lignes commençant par "-"
        String[] parts = clean.split("(?m)^\\s*-\\s*");

        List<ConflictDetail> conflicts = new ArrayList<>();
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            // Détection du type (ROOM, PROF, OTHER)
            String type;
            String lower = part.toLowerCase();
            if (lower.contains("salle")) {
                type = "ROOM";
            } else if (lower.contains("prof") || lower.contains("enseignant")) {
                type = "PROF";
            } else {
                type = "OTHER";
            }

            conflicts.add(new ConflictDetail(type, part));
        }

        return new ErrorResponse("Conflits détectés", conflicts);
    }
}
