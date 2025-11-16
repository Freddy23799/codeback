
package codeqr.code.controller;

import codeqr.code.dto.ProfileDTO;
import codeqr.code.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(
            @RequestParam Long id,
            @RequestParam String role
    ) {
        ProfileDTO profile = profileService.getProfile(id, role);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }
}
