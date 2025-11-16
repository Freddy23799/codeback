package codeqr.code.controller;

import codeqr.code.service.UserUpdateService;
import codeqr.code.dto.UpdateUserRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
public class UserController {

    private final UserUpdateService userUpdateService;

    public UserController(UserUpdateService userUpdateService) {
        this.userUpdateService = userUpdateService;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest req) {
        try {
            userUpdateService.updateUser(
                    req.getId(),
                    req.getUsername(),
                    req.getPassword(),
                    req.getRole()
            );
            return ResponseEntity.ok("Utilisateur mis à jour");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erreur lors de la mise à jour");
        }
    }
}
