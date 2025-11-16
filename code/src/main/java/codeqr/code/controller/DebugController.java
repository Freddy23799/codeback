package codeqr.code.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    // GET http://localhost:9001/api/debug/auth
    @GetMapping("/auth")
    public Map<String, Object> debugAuth(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();

        if (authentication == null) {
            result.put("authenticated", false);
            result.put("message", "Aucune Authentication dans le contexte (token absent ou invalide).");
            return result;
        }

        result.put("authenticated", authentication.isAuthenticated());
        result.put("principalClass", authentication.getPrincipal() == null ? null : authentication.getPrincipal().getClass().getName());
        result.put("principal", authentication.getPrincipal().toString());
        result.put("authorities", authentication.getAuthorities());
        result.put("details", authentication.getDetails());

        return result;
    }
}