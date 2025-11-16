package codeqr.code.dto2;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String matricule;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
