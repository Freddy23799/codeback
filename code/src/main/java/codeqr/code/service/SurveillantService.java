// file: codeqr/code/service/SurveillantService.java
package codeqr.code.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import codeqr.code.dto.SurveillantListDTO;
import codeqr.code.repository.SurveillantRepository;
// import codeqr.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurveillantService {
    private final SurveillantRepository surveillantRepository;
    // private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;
@Cacheable(cacheNames = "surveillants", key = "'list_'+#limit+'_'+#cursor")
    public List<SurveillantListDTO> list(Long cursor, int limit, String q) {
        return surveillantRepository.fetchSurveillantsLight(cursor, limit, q);
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
