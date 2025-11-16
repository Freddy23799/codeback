
package codeqr.code.security.qr;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Service
public class QrJwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public QrJwtService(KeyPair keyPair) {
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        System.out.println("PRIVATE KEY format  =" + privateKey.getFormat() +"alg="+ privateKey.getAlgorithm());
        System.out.println("PUBLIC KEY format  =" + publicKey.getFormat() +"alg="+ publicKey.getAlgorithm());
}
    // Générer un token signé pour une session (QR Code)
    public String generateQrToken(Long sessionId, long durationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + durationMs);

        return Jwts.builder()
                .setSubject(sessionId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // Valider un token scanné par l’étudiant
    public Long validateQrToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token);

        return Long.parseLong(claims.getBody().getSubject());
    }
}



