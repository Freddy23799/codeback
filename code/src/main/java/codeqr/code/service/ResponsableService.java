// file: codeqr/code/service/SurveillantService.java
package codeqr.code.service;

import codeqr.code.dto.ResponsableListDTO;
import codeqr.code.model.Responsable;
import codeqr.code.model.User;
import codeqr.code.model.Role;
import codeqr.code.repository.ResponsableRepository;
import codeqr.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponsableService {
    private final ResponsableRepository responsableRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
@Cacheable(cacheNames = "responsables", key = "'list_'+#limit+'_'+#cursor")
    public List<ResponsableListDTO> list(Long cursor, int limit, String q) {
        return responsableRepository.fetchResponsablesLight(cursor, limit, q);
    }

    // public Surveillant create(String matricule, String fullName, String email, Long sexeId, String username, String rawPassword) {
    //     User user = new User();
    //     user.setUsername(username);
    //     user.setPassword(passwordEncoder.encode(rawPassword));
    //     user.setRole(Role.SURVEILLANT);

    //     Surveillant s = new Surveillant();
    //     s.setMatricule(matricule);
    //     s.setFullName(fullName);
    //     s.setEmail(email);
    //     s.setUser(user);

    //     user.setSurveillant(s);
    //     return surveillantRepository.save(s);
    // }

    // update / delete à ajouter selon besoin
}
