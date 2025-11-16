package codeqr.code.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret:}")
    private String secretKey; // configure JWT_SECRET en production

    @Value("${jwt.expiration:36000000}") // 10h par défaut (ms)
    private long EXPIRATION;

    private Key getSignKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret non configuré. Set JWT_SECRET / jwt.secret");
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> {
                    String r = a.getAuthority();
                    return r.startsWith("ROLE_") ? r : "ROLE_" + r;
                })
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        if (token == null) throw new JwtException("Token null");
        token = token.trim();
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims c = parseClaims(token);
        return c.get("roles", List.class);
    }

    public boolean isTokenExpired(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }
}




// //package codeqr.code.security.jwt;

// import io.jsonwebtoken.*;
// import io.jsonwebtoken.security.Keys;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Service;

// import java.security.Key;
// import java.util.Date;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// @Service
// public class JwtService {

//     private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

//     /**
//      * -> EN PROD : stocker la clé dans une variable d'environnement / vault.
//      *    Doit être au moins 32 bytes (ou plus) pour HS256.
//      */
//     private static final String SECRET_KEY = "ReplaceThisWithASuperLongRandomSecretKey_ChangeInProd_32+chars";
//     private static final long EXPIRATION = 1000L * 60 * 60 * 10; // 10h

//     private Key getSignKey() {
//         return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//     }

//     /**
//      * Génère un token contenant le username en subject et la liste des rôles en claim "roles".
//      */
//     public String generateToken(UserDetails userDetails) {
//         return generateToken(Map.of(), userDetails);
//     }

//     /**
//      * Génère un token en incluant des claims supplémentaires fournis.
//      * Le subject reste userDetails.getUsername().
//      */
//     public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//         List<String> roles = userDetails.getAuthorities().stream()
//                 .map(a -> {
//                     String r = a.getAuthority();
//                     return r.startsWith("ROLE_") ? r : "ROLE_" + r;
//                 })
//                 .collect(Collectors.toList());

//         Date now = new Date();
//         Date exp = new Date(System.currentTimeMillis() + EXPIRATION);

//         JwtBuilder builder = Jwts.builder()
//                 .setClaims(extraClaims)
//                 .setSubject(userDetails.getUsername())
//                 .claim("roles", roles)
//                 .setIssuedAt(now)
//                 .setExpiration(exp)
//                 .signWith(getSignKey(), SignatureAlgorithm.HS256);

//         String token = builder.compact();
//         logger.debug("Generated JWT for user '{}' (expires at {})", userDetails.getUsername(), exp);
//         return token;
//     }

//     /**
//      * Parse les claims et retourne l'objet Claims. Lance JwtException si invalide.
//      * Gère également le préfixe "Bearer " si présent.
//      */
//     public Claims parseClaims(String token) throws JwtException {
//         if (token == null) throw new JwtException("Token null");
//         token = token.trim();
//         if (token.toLowerCase().startsWith("bearer ")) {
//             token = token.substring(7).trim();
//         }
//         return Jwts.parserBuilder()
//                 .setSigningKey(getSignKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     /**
//      * Extrait le username (subject) du token; retourne null si le token est invalide.
//      */
//     public String extractUsername(String token) {
//         try {
//             return parseClaims(token).getSubject();
//         } catch (JwtException e) {
//             logger.debug("extractUsername: token invalide ou expiré: {}", e.getMessage());
//             return null;
//         } catch (Exception e) {
//             logger.error("extractUsername unexpected error", e);
//             return null;
//         }
//     }

//     /**
//      * Extrait la liste des roles depuis le claim "roles". Retourne null si impossible.
//      */
//     @SuppressWarnings("unchecked")
//     public List<String> extractRoles(String token) {
//         try {
//             Claims c = parseClaims(token);
//             return c.get("roles", List.class);
//         } catch (JwtException e) {
//             logger.debug("extractRoles: token invalide: {}", e.getMessage());
//             return null;
//         } catch (Exception e) {
//             logger.error("extractRoles unexpected error", e);
//             return null;
//         }
//     }

//     /**
//      * Vérifie si le token est expiré. En cas d'erreur retourne true (considéré expiré / invalide).
//      */
//     public boolean isTokenExpired(String token) {
//         try {
//             Date exp = parseClaims(token).getExpiration();
//             return exp == null || exp.before(new Date());
//         } catch (JwtException e) {
//             logger.debug("isTokenExpired: token invalide ou parsing failed: {}", e.getMessage());
//             return true;
//         } catch (Exception e) {
//             logger.error("isTokenExpired unexpected error", e);
//             return true;
//         }
//     }

//     /**
//      * Valide que le token correspond à userDetails (username identique) et n'est pas expiré.
//      */
//     public boolean isTokenValid(String token, UserDetails userDetails) {
//         try {
//             final String username = extractUsername(token);
//             if (username == null) return false;
//             boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//             if (!valid) {
//                 logger.debug("isTokenValid: valid={} for token subject='{}' vs user='{}'",
//                         valid, username, userDetails.getUsername());
//             }
//             return valid;
//         } catch (Exception e) {
//             logger.debug("isTokenValid: exception validating token", e);
//             return false;
//         }
//     }
// }
// //