package codeqr.code.mise;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Contrôleur REST pour la vérification de version de l'application.
 *
 * GET /api/app/version?platform=ANDROID&clientVersion=2.2.1
 *
 * Lit le fichier JSON (par défaut releases/update.json) puis renvoie la
 * partie correspondant à la plateforme demandée, enrichie par :
 *   - updateAvailable (boolean)
 *   - mandatory (boolean)
 *
 * Si platform absent -> renvoie le JSON entier.
 */
@RestController
@RequestMapping("/api/app")
@CrossOrigin(origins = "*")
public class AppVersionController {

    @Value("${app.update.filepath:releases/update.json}")
    private String updateFilePath;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> checkVersion(
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String clientVersion) {

        try {
            Path path = Paths.get(updateFilePath);
            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"update.json introuvable sur le serveur.\"}");
            }

            String json = Files.readString(path);
            // parse to map
            Map<String, Object> root = mapper.readValue(json, new TypeReference<>() {});

            // if no platform requested -> return whole file (raw)
            if (platform == null || platform.isBlank()) {
                HttpHeaders headers = buildHeaders();
                return new ResponseEntity<>(json, headers, HttpStatus.OK);
            }

            String key = platform.trim().toLowerCase();
            if (!root.containsKey(key)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"Plateforme inconnue: " + platform + "\"}");
            }

            // platform node (may be Map or simple value)
            Object nodeObj = root.get(key);
            Map<String, Object> node;
            if (nodeObj instanceof Map) {
                //noinspection unchecked
                node = new LinkedHashMap<>((Map<String, Object>) nodeObj);
            } else {
                // not map -> return it as-is
                node = new LinkedHashMap<>();
                node.put("value", nodeObj);
            }

            // normalize common keys
            String latestVersion = extractString(node, "latestVersion", "latest", "version");
            String minRequired = extractString(node, "minRequiredVersion", "minRequired", "minSupported");
            Boolean forceUpdate = extractBoolean(node, "forceUpdate", "force", "mandatory");

            // compute updateAvailable / mandatory based on clientVersion if provided
            boolean updateAvailable = false;
            boolean mandatory = false;
            if (clientVersion != null && !clientVersion.isBlank()) {
                if (latestVersion != null && semverIsNewer(latestVersion, clientVersion)) {
                    updateAvailable = true;
                }
                if (minRequired != null && semverIsNewer(minRequired, clientVersion)) {
                    mandatory = true;
                }
            } else {
                // if clientVersion absent, rely on forceUpdate flag if present
                if (Boolean.TRUE.equals(forceUpdate)) mandatory = true;
            }

            // if node already contains 'updateAvailable' or 'mandatory', prefer computed ones but keep them
            node.put("updateAvailable", updateAvailable);
            node.put("mandatory", mandatory || Boolean.TRUE.equals(forceUpdate));

            // respond with platform-specific JSON
            String platformJson = mapper.writeValueAsString(node);
            HttpHeaders headers = buildHeaders();
            return new ResponseEntity<>(platformJson, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erreur lecture update.json: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erreur interne: " + e.getMessage() + "\"}");
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return headers;
    }

    private static String extractString(Map<String, Object> node, String... keys) {
        for (String k : keys) {
            Object v = node.get(k);
            if (v instanceof String && !((String) v).isBlank()) return (String) v;
            if (v != null) return String.valueOf(v);
        }
        return null;
    }

    private static Boolean extractBoolean(Map<String, Object> node, String... keys) {
        for (String k : keys) {
            Object v = node.get(k);
            if (v instanceof Boolean) return (Boolean) v;
            if (v instanceof String) {
                String s = ((String) v).trim().toLowerCase();
                if ("true".equals(s)) return true;
                if ("false".equals(s)) return false;
            }
            if (v instanceof Number) return ((Number) v).intValue() != 0;
        }
        return null;
    }

    /**
     * Compare simple semantic versions (dot-separated integers).
     * Return true if a > b (a is newer than b).
     * Works for versions like "2.2.1", "4.7.0".
     */
    private static boolean semverIsNewer(String a, String b) {
        if (a == null || b == null) return false;
        String[] sa = a.split("\\.");
        String[] sb = b.split("\\.");
        int n = Math.max(sa.length, sb.length);
        for (int i = 0; i < n; i++) {
            int ia = i < sa.length ? tryParseInt(sa[i]) : 0;
            int ib = i < sb.length ? tryParseInt(sb[i]) : 0;
            if (ia > ib) return true;
            if (ia < ib) return false;
        }
        return false;
    }

    private static int tryParseInt(String s) {
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0; }
    }
}
