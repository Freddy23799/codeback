package codeqr.code.service;

import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import codeqr.code.dto.ProfileDTO;
import codeqr.code.repository.AdminRepository;
import codeqr.code.repository.StudentRepository;
import codeqr.code.repository.SurveillantRepository;
import codeqr.code.repository.ResponsableRepository;
import codeqr.code.repository.TeacherRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final AdminRepository adminRepo;
    private final SurveillantRepository surveillantRepo;
  private final ResponsableRepository responRepo;
    @Cacheable(
        value = "profiles",
        key = "#authId + '-' + (#role != null ? #role.toUpperCase() : '')",
        unless = "#result == null"
    )
    public ProfileDTO getProfile(Long authId, String role) {
        if (role == null) {
            throw new IllegalArgumentException("Rôle non fourni");
        }

        switch (role.toUpperCase()) {
            case "ETUDIANT":
                return studentRepo.findById(authId)
                        .map(s -> new ProfileDTO(
                                s.getId(),
                                s.getFullName(),
                                s.getEmail(),
                                s.getMatricule(),
                                s.getSexe() != null ? s.getSexe().getName() : null,
                                "ETUDIANT"
                        ))
                        .orElse(null);

            case "PROFESSEUR":
                return teacherRepo.findById(authId)
                        .map(t -> {
                            // construire une chaîne des cours (titres), ex: "Maths, Physique"
                            String courses = null;
                            if (t.getCourses() != null && !t.getCourses().isEmpty()) {
                                courses = t.getCourses().stream()
                                        .map(c -> c.getTitle() != null ? c.getTitle() : (c.getCode() != null ? c.getCode() : ""))
                                        .filter(s -> s != null && !s.isBlank())
                                        .collect(Collectors.joining(", "));
                            }

                            return new ProfileDTO(
                                    t.getId(),
                                    t.getFullName(),
                                    t.getEmail(),
                                    t.getMatricule(),
                                    courses, // remplace l'ancien champ matiere
                                    t.getSexe() != null ? t.getSexe().getName() : null,
                                    "PROFESSEUR"
                            );
                        })
                        .orElse(null);

            case "SURVEILLANT":
                return surveillantRepo.findById(authId)
                        .map(s -> new ProfileDTO(
                                s.getId(),
                                s.getFullName(),
                                s.getEmail(),
                                s.getMatricule(),
                                s.getSexe() != null ? s.getSexe().getName() : null,
                                "SURVEILLANT"
                        ))
                        .orElse(null);

            case "ADMIN":
                return adminRepo.findById(authId)
                        .map(a -> new ProfileDTO(
                                a.getId(),
                                a.getFullName(),
                                a.getEmail(),
                                a.getMatricule(),
                                a.getSexe() != null ? a.getSexe().getName() : null,
                                "ADMIN"
                        ))
                        .orElse(null);
 case "RESPONSABLE":
                return responRepo.findById(authId)
                        .map(a -> new ProfileDTO(
                                a.getId(),
                                a.getFullName(),
                                a.getEmail(),
                                a.getMatricule(),
                                a.getSexe() != null ? a.getSexe().getName() : null,
                                "RESPONSABLE"
                        ))
                        .orElse(null);
            default:
                throw new IllegalArgumentException("Rôle inconnu : " + role);
        }
    }
}
