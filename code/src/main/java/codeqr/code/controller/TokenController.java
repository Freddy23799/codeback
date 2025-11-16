

package codeqr.code.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenController {

    private final String SECRET_KEY = "ReplaceThisWithASuperLongRandomSecretKey_ChangeInProd_32+chars"; // doit faire 256 bits pour HS256

    @PostMapping("/api/verify-token")
    public Map<String, Object> verifyToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");

        if (token == null || token.isBlank()) {
            response.put("valid", false);
            return response;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);
            response.put("valid", true);
            response.put("role", role);

        } catch (ExpiredJwtException e) {
            response.put("valid", false);
            response.put("message", "Token expiré");
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token invalide");
        }

        return response;
    }
}
