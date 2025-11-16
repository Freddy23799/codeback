package codeqr.code.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import codeqr.code.security.qr.QrJwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/test/qr")
@RequiredArgsConstructor
public class QrTestController {

    private final QrJwtService qrJwtService;

    // Génère un token QR (signé)
    @GetMapping("/gen/{sessionId}")
    public ResponseEntity<Map<String,String>> gen(@PathVariable Long sessionId) {
        String token = qrJwtService.generateQrToken(sessionId, 2 * 60 * 60 * 1000); // 2h
        return ResponseEntity.ok(Map.of("token", token));
    }

    // Valide un token envoyé (body: {"token":"..."} )
    @PostMapping("/validate")
    public ResponseEntity<Map<String,Object>> validate(@RequestBody Map<String,String> body) {
        String token = body.get("token");
        Long sessionId = qrJwtService.validateQrToken(token);
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }
}