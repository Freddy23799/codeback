package codeqr.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data 
public class UpdateUserDTO {

    @NotNull(message = "L'ID du profil est requis")
    private Long profileId;

    @NotBlank(message = "Le rôle est requis")
    private String role;

    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password; // nullable si on ne veut pas changer
}
