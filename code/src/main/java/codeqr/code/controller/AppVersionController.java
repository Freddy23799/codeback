// package codeqr.code.controller;

// import codeqr.code.dto.AppVersionDTO;

// import org.checkerframework.checker.units.qual.cd;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api")
// public class AppVersionController {

//     @Value("${app.version:2.2}")          // version serveur (format a.b)
//     private String serverVersion;

//     @Value("${app.url.android:}")
//     private String androidUrl;

//     @Value("${app.url.ios:}")
//     private String iosUrl;

//     @Value("${app.url.windows:}")
//     private String windowsUrl;

//     @Value("${app.url.macos:}")
//     private String macUrl;

//     @Value("${app.minVersion:}")          // si défini, clients < minVersion => mandatory
//     private String minSupportedVersion;

//     @Value("${app.update.mandatory:false}")
//     private boolean forceMandatory;

//     @GetMapping("/app/version")
//     public AppVersionDTO getAppVersion(@RequestParam(required = false) String platform,
//                                        @RequestParam(required = false) String clientVersion) {

//         String normalizedPlatform = platform == null ? "WEB" : platform.trim().toUpperCase();
//         String downloadUrl = selectUrlForPlatform(normalizedPlatform);

//         boolean updateAvailable = false;
//         boolean mandatory = false;

//         // Compare uniquement major.minor (a.b)
//         if (clientVersion != null && !clientVersion.isBlank()) {
//             int cmp = compareMajorMinor(clientVersion.trim(), serverVersion.trim());
//             updateAvailable = (cmp < 0); // client < server => update disponible

//             if (forceMandatory) {
//                 mandatory = true;
//             } else if (minSupportedVersion != null && !minSupportedVersion.isBlank()) {
//                 int cmpMin = compareMajorMinor(clientVersion.trim(), minSupportedVersion.trim());
//                 if (cmpMin < 0) mandatory = true;
//             }
//         } else {
//             // pas de version fournie : considérer update disponible (configurable)
//             updateAvailable = true;
//             mandatory = forceMandatory;
//         }

//         String message;
//         if (!updateAvailable) {
//             message = "Votre application est à jour.";
//         } else {
//             message = "Nouvelle version disponible : " + serverVersion + (mandatory ? " (mise à jour requise)" : "");
//         }

//         AppVersionDTO dto = new AppVersionDTO(serverVersion, downloadUrl, mandatory, updateAvailable, message, normalizedPlatform);
//         return dto;
//     }

//     private String selectUrlForPlatform(String platform) {
//         switch (platform) {
//             case "ANDROID": return androidUrl;
//             case "IOS":     return iosUrl;
//             case "WINDOWS": return windowsUrl;
//             case "MACOS":   return macUrl;
//             default:        return androidUrl; // fallback
//         }
//     }

//     /**
//      * Compare major.minor formats like "2.2" or "2.2.0" (on ne regarde que major et minor).
//      * Retourne -1 si a < b, 0 si égal, 1 si a > b.
//      */
//     private int compareMajorMinor(String a, String b) {
//         if (a == null) a = "";
//         if (b == null) b = "";
//         String[] pa = a.split("\\.");
//         String[] pb = b.split("\\.");
//         int majorA = pa.length > 0 && pa[0].matches("\\d+") ? Integer.parseInt(pa[0]) : 0;
//         int majorB = pb.length > 0 && pb[0].matches("\\d+") ? Integer.parseInt(pb[0]) : 0;
//         if (majorA < majorB) return -1;
//         if (majorA > majorB) return 1;

//         int minorA = pa.length > 1 && pa[1].matches("\\d+") ? Integer.parseInt(pa[1]) : 0;
//         int minorB = pb.length > 1 && pb[1].matches("\\d+") ? Integer.parseInt(pb[1]) : 0;
//         if (minorA < minorB) return -1;
//         if (minorA > minorB) return 1;
//         return 0;
//     }
// }




















