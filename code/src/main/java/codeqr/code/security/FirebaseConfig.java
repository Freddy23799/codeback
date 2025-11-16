package codeqr.code.security;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.key.path:}")
    private String firebaseKeyPath;  // ex: /etc/myapp/firebase/serviceAccountKey.json

    @Value("${firebase.key.json:}")
    private String firebaseKeyJson;  // optionnel : contenu JSON complet (moins recommandé)

    @PostConstruct
    public void init() throws IOException {
        InputStream is = null;

        // 1) contenu JSON fourni via property (env)
        if (firebaseKeyJson != null && !firebaseKeyJson.isBlank()) {
            is = new ByteArrayInputStream(firebaseKeyJson.getBytes(StandardCharsets.UTF_8));
        }

        // 2) chemin vers fichier fourni via property
        if (is == null && firebaseKeyPath != null && !firebaseKeyPath.isBlank()) {
            is = new FileInputStream(firebaseKeyPath);
        }

        // 3) fallback GOOGLE_APPLICATION_CREDENTIALS (compatibilité GCP)
        if (is == null) {
            String gac = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (gac != null && !gac.isBlank()) {
                is = new FileInputStream(gac);
            }
        }

        if (is == null) {
            throw new RuntimeException(
                "Firebase credentials introuvables. Configure firebase.key.path (chemin) " +
                "ou firebase.key.json (contenu JSON) ou GOOGLE_APPLICATION_CREDENTIALS.");
        }

        try (InputStream credentialsStream = is) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialisé");
            }
        }
    }
}
