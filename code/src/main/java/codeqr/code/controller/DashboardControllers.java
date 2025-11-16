package codeqr.code.controller;

import codeqr.code.dto.DashboardDtos;
import codeqr.code.service.DashboardServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Endpoint REST pour le dashboard.
 *
 * GET /api/dashboard        -> dashboard pour user courant (via security principal)
 * GET /api/dashboard?userId=42  -> dashboard pour userId (utile pour tests/admin)
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardControllers {

    private final DashboardServices dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDtos> getDashboard(@RequestParam(value = "userId", required = false) Long userId,
                                                     Principal principal) {
        // 1) si userId fourni en param, on l'utilise (utile pour tests).
        // 2) sinon on tente de récupérer l'id depuis Principal (s'il contient l'id). Sinon, renvoie 401.
        Long resolvedUserId = userId;
        if (resolvedUserId == null) {
            // Adaptation : si tu utilises Spring Security avec JWT tu récupères l'id différemment.
            // Ici on essaye de parser principal.getName() en Long (si ton principal est l'id).
            if (principal != null) {
                try {
                    resolvedUserId = Long.parseLong(principal.getName());
                } catch (NumberFormatException ex) {
                    // else : tu peux remplacer par injection d'un UserService pour récupérer l'id.
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.status(401).build();
            }
        }

        DashboardDtos dto = dashboardService.getDashboardForUser(resolvedUserId);
        return ResponseEntity.ok(dto);
    }
}
