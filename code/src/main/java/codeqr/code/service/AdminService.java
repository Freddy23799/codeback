package codeqr.code.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.*;
// import codeqr.code.dto.TeacherDTO;
import codeqr.code.exception.NotFoundException;
import codeqr.code.model.Admin;
import codeqr.code.model.Role;
import codeqr.code.model.Sexe;
// import codeqr.code.model.Teacher;
import codeqr.code.model.User;
import codeqr.code.repository.*;

// --------------- Admin Service -----------------
@Service
@Transactional
public class AdminService {
    private final AdminRepository adminRepository;
 private final PasswordEncoder passwordEncoder;
 private final UserRepository userRepository;
  private final SexeRepository sexeRepository;
//  private final AdminRepository adminRepository;
 
    public AdminService(AdminRepository adminRepository,UserRepository userRepository,PasswordEncoder passwordEncoder,SexeRepository sexeRepository) { this.adminRepository = adminRepository;this.sexeRepository= sexeRepository; this.passwordEncoder = passwordEncoder; this.userRepository = userRepository;
}

   
    public Optional<Admin> findByEmail(String email) { return adminRepository.findByEmail(email); }
    
    public Admin findByUsername(String username) {
        return adminRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));
    }
 
  
//  public AdminDto findByUsername(String username) {
//         return adminRepository.findByUsername(username)
//         .map(this::mapToDTO)
//                 .orElseThrow(() -> new RuntimeException("Admin non trouvé" + username));
//     }
 
  
   
    // Mettre à jour (user + admin)
    public Admin updateAdmin(Long id, String username,String matricule, String password, String fullName, String email) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("admin not found"));

        User user = admin.getUser();
        if (username != null && !username.isEmpty()) user.setUsername(username);
        if (password != null && !password.isEmpty()) user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        if (fullName != null && !fullName.isEmpty()) admin.setFullName(fullName);
        if (email != null && !email.isEmpty()) admin.setEmail(email);

        return adminRepository.save(admin);
    }





     







 @Autowired
    private AdminJdbcRepository repo;


@Cacheable(cacheNames = "teacherReports", key = "#specialty+'_'+#level+'_'+#academicYearId+'_'+(#dateFrom != null ? #dateFrom.toString() : 'null')+'_'+(#dateTo != null ? #dateTo.toString() : 'null')")
public List<TeacherLightDTO> listTeachersLight(String q, int limit) {
        return repo.fetchTeachersLight(q, limit);
    }

    public List<SessionLightDT> listSessionsByTeacher(Long teacherId,
                                                      LocalDateTime start,
                                                      LocalDateTime end,
                                                      LocalDateTime lastStart,
                                                      Long lastId,
                                                      int limit,
                                                      Long academicYearId,
                                                      Long specialtyId,
                                                      Long levelId) {
        return repo.fetchSessionsByTeacher(teacherId, start, end, lastStart, lastId, limit, academicYearId, specialtyId, levelId);
    }
@Cacheable(cacheNames = "sessionStudents", key = "#sessionId+'_'+#page+'_'+#size+'_'+(#q != null ? #q : '')")
    public PagedResult<StudentInSessionDTO> listStudentsBySession(Long sessionId, int page, int size, String q) {
        return repo.fetchStudentsBySession(sessionId, page, size, q);
    }






    
    private AdminDTO toDTO(Admin t) {
        AdminDTO dto = new AdminDTO();
        dto.setId(t.getId());
        dto.setFullName(t.getFullName());
         dto.setMatricule(t.getMatricule());
        dto.setEmail(t.getEmail());
   dto.setUsername(t.getUser() != null ? t.getUser().getUsername() : null);
         dto.setSexeName(t.getSexe() != null ? t.getSexe().getName() : null);
        return dto;
    }
    // public record StudentDTO(Long id,String fullName,String email,String sexeName,String matricule){}
    @Cacheable(cacheNames = "adminsList", key = "'all'")
    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
@Cacheable(cacheNames = "adminDetails", key = "#id")
    public AdminDTO getAdminById(Long id) {
        return adminRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("Admin not found: " + id));
    }

    public AdminDTO createAdmin(AdminRequest req) {
        Admin a = new Admin();
        a.setFullName(req.getFullName());
        a.setEmail(req.getEmail());
          a.setMatricule(req.getMatricule());
     if(req.getSexeId() != null){
        Sexe sexe=
        sexeRepository.findById(req.getSexeId())
        .orElseThrow(() -> new NotFoundException("Sexe not found: " + req.getSexeId()));
        a.setSexe(sexe);
     }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.ADMIN);

        a.setUser(u);
        u.setAdmin(a);

        userRepository.save(u);
       adminRepository.save(a);
        return toDTO(a);
    }












    public AdminDTO updateAdmin(Long id, AdminRequest req) {
        Admin a = adminRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Admin not found: " + id));
if(req.getFullName() != null)
        a.setFullName(req.getFullName());
        if(req.getEmail() != null)
        a.setEmail(req.getEmail());
       a.setMatricule(req.getMatricule());
if(req.getSexeId() != null){
        Sexe sexe=
        sexeRepository.findById(req.getSexeId())
        .orElseThrow(() -> new NotFoundException("Sexe not found: " + req.getSexeId()));
        a.setSexe(sexe);
     }
        User u = a.getUser();
        if (u != null) {
            u.setUsername(req.getUsername());
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            userRepository.save(u);
        }

        adminRepository.save(a);
        return toDTO(a);
    }

    public void deleteAdminAndUser(Long id) {
        Admin a = adminRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Admin not found: " + id));
        User u = a.getUser();
       adminRepository.delete(a);
        if (u != null) userRepository.delete(u);
    }

    
}