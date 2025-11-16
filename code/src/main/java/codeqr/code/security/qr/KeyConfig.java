package codeqr.code.security.qr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class KeyConfig {

    @Value("${rsa.private.path:}")
    private String privateKeyPath;           // ex: /etc/myapp/keys/private_key.pem

    @Value("${rsa.private.pem:}")
    private String privateKeyPem;            // optionnel : contenu PEM complet (env var)

    @Value("${rsa.public.path:classpath:public_key.pem}")
    private String publicKeyPath;            // ex: classpath:public_key.pem ou /etc/myapp/keys/public_key.pem

    @Bean
    public KeyPair keyPair() throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        PublicKey publicKey = loadPublicKey();
        return new KeyPair(publicKey, privateKey);
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] derBytes = null;

        // 1) contenu PEM direct (env var)
        if (privateKeyPem != null && !privateKeyPem.isBlank()) {
            String pem = privateKeyPem
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            derBytes = java.util.Base64.getDecoder().decode(pem);
        }
        // 2) fichier sur disque
        else if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            byte[] fileBytes = Files.readAllBytes(Paths.get(privateKeyPath));
            String asText = new String(fileBytes, StandardCharsets.UTF_8);
            if (asText.contains("BEGIN")) {
                String pem = asText
                        .replaceAll("-----BEGIN (.*)-----", "")
                        .replaceAll("-----END (.*)-----", "")
                        .replaceAll("\\s", "");
                derBytes = java.util.Base64.getDecoder().decode(pem);
            } else {
                // assume raw DER bytes
                derBytes = fileBytes;
            }
        } else {
            throw new RuntimeException("Aucune clé privée fournie. Configure rsa.private.path ou rsa.private.pem");
        }

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(derBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        byte[] derBytes = null;
        // 1) classpath:...
        if (publicKeyPath != null && publicKeyPath.startsWith("classpath:")) {
            String resource = publicKeyPath.substring("classpath:".length());
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
            if (is == null) throw new RuntimeException("Public key introuvable dans le classpath: " + resource);
            byte[] fileBytes = is.readAllBytes();
            String asText = new String(fileBytes, StandardCharsets.UTF_8);
            if (asText.contains("BEGIN")) {
                String pem = asText
                        .replaceAll("-----BEGIN (.*)-----", "")
                        .replaceAll("-----END (.*)-----", "")
                        .replaceAll("\\s", "");
                derBytes = java.util.Base64.getDecoder().decode(pem);
            } else {
                derBytes = fileBytes;
            }
        } else {
            // treat as filesystem path
            File f = new File(publicKeyPath);
            if (!f.exists()) throw new RuntimeException("Public key file not found: " + publicKeyPath);
            byte[] fileBytes = Files.readAllBytes(f.toPath());
            String asText = new String(fileBytes, StandardCharsets.UTF_8);
            if (asText.contains("BEGIN")) {
                String pem = asText
                        .replaceAll("-----BEGIN (.*)-----", "")
                        .replaceAll("-----END (.*)-----", "")
                        .replaceAll("\\s", "");
                derBytes = java.util.Base64.getDecoder().decode(pem);
            } else {
                derBytes = fileBytes;
            }
        }

        X509EncodedKeySpec spec = new X509EncodedKeySpec(derBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
