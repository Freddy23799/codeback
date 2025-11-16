



package codeqr.code.service;

import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;
// import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto2.ResponsableDTO;
import codeqr.code.dto2.SurveillantDTO;
import codeqr.code.dto2.TeacherDTO;
 import codeqr.code.dto.*;
import codeqr.code.exception.NotFoundException;
import codeqr.code.model.*;
import codeqr.code.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
       private final SessionRepository sessionRepository;
    private final ResponsableRepository responsableRepository;
     private final SurveillantRepository surveillantRepository;
    private final UserRepository userRepository;
        private final CourseRepository courseRepository;
    private final TeacherYearProfileRepository teacherYearProfileRepository;
    private final PasswordEncoder passwordEncoder;
@Autowired
private SexeRepository sexeRepository;
    public Optional<Teacher> findByUsername(String username) {
        return teacherRepository.findByUserUsername(username);
    }

    public Optional<Teacher> getById(Long id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> getByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }

    public List<TeacherYearProfile> getProfiles(Long teacherId) {
        return teacherYearProfileRepository.findByTeacherId(teacherId);
    }

    public List<TeacherYearProfile> getActiveProfiles(Long teacherId) {
        return teacherYearProfileRepository.findByTeacherIdAndActiveTrue(teacherId);
    }





 private SurveillantDTO toDTOS(Surveillant surveillant) {
    SurveillantDTO dto = new SurveillantDTO();
    dto.setId(surveillant.getId());
    dto.setFullName(surveillant.getFullName());
     dto.setMatricule(surveillant.getMatricule());
     
    dto.setEmail(surveillant.getEmail());
    dto.setUsername(surveillant.getUser() != null ? surveillant.getUser().getUsername() : null);

    if (surveillant.getSexe() != null) {
        dto.setSexeId(surveillant.getSexe().getId());
        dto.setSexeName(surveillant.getSexe().getName());
    }

    return dto;
}
















private ResponsableDTO toDTOS(Responsable responsable) {
    ResponsableDTO dto = new ResponsableDTO();
    dto.setId(responsable.getId());
    dto.setFullName(responsable.getFullName());
     dto.setMatricule(responsable.getMatricule());
     
    dto.setEmail(responsable.getEmail());
    dto.setUsername(responsable.getUser() != null ? responsable.getUser().getUsername() : null);

    if (responsable.getSexe() != null) {
        dto.setSexeId(responsable.getSexe().getId());
        dto.setSexeName(responsable.getSexe().getName());
    }

    return dto;
}










  private TeacherDTO toDTO(Teacher t) {
    TeacherDTO dto = new TeacherDTO();
    dto.setId(t.getId());
    dto.setFullName(t.getFullName());
    dto.setMatricule(t.getMatricule());

    dto.setEmail(t.getEmail());
    dto.setUsername(t.getUser() != null ? t.getUser().getUsername() : null);

    // Sexe
    if (t.getSexe() != null) {
        dto.setSexeId(t.getSexe().getId());
        dto.setSexeName(t.getSexe().getName());
    } else {
        dto.setSexeName("—");
    }

    // Cours associés
    if (t.getCourses() != null) {
        List<CourseDO> courses = t.getCourses().stream()
            .map(c -> new CourseDO(c.getId(), c.getCode(), c.getTitle()))
            .toList();
        dto.setCourses(courses);
    } else {
        dto.setCourses(new ArrayList<>());
    }

    return dto;
}

    // public record StudentDTO(Long id,String fullName,String email,String sexeName,String matricule){}
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


 public List<SurveillantDTO> getAllSurveillant() {
        return surveillantRepository.findAll().stream()
                .map(this::toDTOS)
                .collect(Collectors.toList());
    }


    public List<ResponsableDTO> getAllResponsable() {
        return responsableRepository.findAll().stream()
                .map(this::toDTOS)
                .collect(Collectors.toList());
    }





    public TeacherDTO getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("teacher not found: " + id));
    }

 public TeacherDTO createTeacher(TeacherRequest req) {
    Teacher t = new Teacher();
    t.setFullName(req.getFullName());
    t.setEmail(req.getEmail());
    t.setMatricule(req.getMatricule());

    // Sexe
    if (req.getSexeId() != null) {
        Sexe sexe = sexeRepository.findById(req.getSexeId())
                .orElseThrow(() -> new NotFoundException("Sexe non trouvé: " + req.getSexeId()));
        t.setSexe(sexe);
    }

    // User
    User u = new User();
    u.setUsername(req.getUsername());
    u.setPassword(passwordEncoder.encode(req.getPassword()));
    u.setRole(Role.PROFESSEUR);

    t.setUser(u);
    u.setTeacher(t);

    // ✅ Cours sélectionnés
    if (req.getCourseIds() != null && !req.getCourseIds().isEmpty()) {
        List<Course> courses = courseRepository.findAllById(req.getCourseIds());
        if (courses.size() != req.getCourseIds().size()) {
            throw new NotFoundException("Un ou plusieurs cours n'existent pas");
        }
        t.setCourses(courses);
    }

    userRepository.save(u);
    teacherRepository.save(t);

    return toDTO(t);
}

































    public SurveillantDTO createSurveillant(codeqr.code.dto.SurveillantRequest req) {
        Surveillant s = new Surveillant();
        s.setFullName(req.getFullName());
        s.setEmail(req.getEmail());
         s.setMatricule(req.getMatricule());
  
        if (req.getSexeId() != null) {
        Sexe sexe = sexeRepository.findById(req.getSexeId())
                .orElseThrow(() -> new NotFoundException("Sexe non trouvé: " + req.getSexeId()));
        s.setSexe(sexe);
    }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.SURVEILLANT);

        s.setUser(u);
        u.setSurveillant(s);

        userRepository.save(u);
       surveillantRepository.save(s);
        return toDTOS(s);
    }






    public ResponsableDTO createResponsable(codeqr.code.dto.ResponsableRequest req) {
        Responsable r = new Responsable();
        r.setFullName(req.getFullName());
        r.setEmail(req.getEmail());
         r.setMatricule(req.getMatricule());
  
        if (req.getSexeId() != null) {
        Sexe sexe = sexeRepository.findById(req.getSexeId())
                .orElseThrow(() -> new NotFoundException("Sexe non trouvé: " + req.getSexeId()));
        r.setSexe(sexe);
    }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.RESPONSABLE);

        r.setUser(u);
        u.setResponsable(r);

        userRepository.save(u);
        responsableRepository.save(r);
        return toDTOS(r);
    }












 public SurveillantDTO updateSurveillant(Long id, codeqr.code.dto.SurveillantRequest req) {
        Surveillant s = surveillantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));

        s.setFullName(req.getFullName());
        s.setEmail(req.getEmail());
      s.setMatricule(req.getMatricule());
   

        User u = s.getUser();
        if (u != null) {
            u.setUsername(req.getUsername());
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            userRepository.save(u);
        }

        surveillantRepository.save(s);
        return toDTOS(s);
    }






    public ResponsableDTO updateResponsable(Long id, codeqr.code.dto.ResponsableRequest req) {
        Responsable r = responsableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));

        r.setFullName(req.getFullName());
        r.setEmail(req.getEmail());
      r.setMatricule(req.getMatricule());
   

        User u = r.getUser();
        if (u != null) {
            u.setUsername(req.getUsername());
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            userRepository.save(u);
        }

        responsableRepository.save(r);
        return toDTOS(r);
    }



   public TeacherDTO updateTeacher(Long id, TeacherRequest req) {
    Teacher t = teacherRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Teacher not found: " + id));

    // ✅ Mise à jour des infos de base
    t.setFullName(req.getFullName());
    t.setEmail(req.getEmail());
    t.setMatricule(req.getMatricule());

    // ✅ Mise à jour du sexe si fourni
    if (req.getSexeId() != null) {
        Sexe sexe = sexeRepository.findById(req.getSexeId())
                .orElseThrow(() -> new NotFoundException("Sexe non trouvé: " + req.getSexeId()));
        t.setSexe(sexe);
    }

    // ✅ Mise à jour de l'utilisateur associé
    User u = t.getUser();
    if (u != null) {
        u.setUsername(req.getUsername());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(u);
    }

    // ✅ Mise à jour des cours associés
    if (req.getCourseIds() != null) {
        List<Course> courses = courseRepository.findAllById(req.getCourseIds());
        if (courses.size() != req.getCourseIds().size()) {
            throw new NotFoundException("Un ou plusieurs cours n'existent pas");
        }
        t.setCourses(courses); // ⚡ remplace l'ancienne liste
    }

    teacherRepository.save(t);
    return toDTO(t);
}

    public void deleteTeacherAndUser(Long id) {
        Teacher t = teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Teacher not found: " + id));
        User u = t.getUser();
       teacherRepository.delete(t);
        if (u != null) userRepository.delete(u);
    }






public void deleteSurveillantAndUser(Long id) {
        Surveillant s = surveillantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Surveillant not found: " + id));
        User u = s.getUser();
      surveillantRepository.delete(s);
        if (u != null) userRepository.delete(u);
    }




    public void deleteResponsableAndUser(Long id) {
        Responsable r = responsableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Surveillant not found: " + id));
        User u = r.getUser();
        responsableRepository.delete(r);
        if (u != null) userRepository.delete(u);
    }

    public Teacher updateTeacher(Long id, String username, String password, String fullName, Object sexeName,
            String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTeacher'");
    }



   public Page<TeacherDTO> getTeachers(int page, int size, String search) {
    int pageIndex = Math.max(0, page - 1); // assure que la page ne soit jamais < 0
    PageRequest pageable = PageRequest.of(pageIndex, size);
    Page<Teacher> teacherPage;

    if (search == null || search.isBlank()) {
        teacherPage = teacherRepository.findAll(pageable);
    } else {
        teacherPage = teacherRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
                        search, search,  search, search, pageable);
    }

    return teacherPage.map(this::toDto);
}







   public Page<SurveillantDTO> getSurveillant(int page, int size, String search) {
    int pageIndex = Math.max(0, page - 1); // assure que la page ne soit jamais < 0
    PageRequest pageable = PageRequest.of(pageIndex, size);
    Page<Surveillant> surveillantPage;

    if (search == null || search.isBlank()) {
        surveillantPage = surveillantRepository.findAll(pageable);
    } else {
        surveillantPage = surveillantRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
                        search, search, search, search, pageable);
    }

    return surveillantPage.map(this::toDtos);
}




private TeacherDTO toDto(Teacher teacher) {
    TeacherDTO dto = new TeacherDTO();
    dto.setId(teacher.getId());
    dto.setMatricule(teacher.getMatricule());
    dto.setFullName(teacher.getFullName());
    dto.setEmail(teacher.getEmail());
 
    dto.setUsername(teacher.getUser() != null ? teacher.getUser().getUsername() : null);

    // ✅ Sexe
    dto.setSexeId(teacher.getSexe() != null ? teacher.getSexe().getId() : null);
    dto.setSexeName(teacher.getSexe() != null ? teacher.getSexe().getName() : "—");

    // ✅ Cours associés (CourseDTO)
    if (teacher.getCourses() != null) {
        List<CourseDO> courses = teacher.getCourses().stream()
            .map(c -> new CourseDO(c.getId(), c.getCode(), c.getTitle()))
            .toList();
        dto.setCourses(courses);
    } else {
        dto.setCourses(new ArrayList<>()); // éviter null côté front
    }

    return dto;
}




private SurveillantDTO toDtos(Surveillant surveillant) {
        SurveillantDTO dto = new SurveillantDTO();
        dto.setId(surveillant.getId());
        dto.setMatricule(surveillant.getMatricule());
        dto.setFullName(surveillant.getFullName());
        dto.setEmail(surveillant.getEmail());
       
        dto.setUsername(surveillant.getUser() != null ? surveillant.getUser().getUsername() : null);
        dto.setSexeId(surveillant.getSexe() != null ? surveillant.getSexe().getId() : null);
        dto.setSexeName(surveillant.getSexe() != null ? surveillant.getSexe().getName() : "—");
        return dto;
    }

   
    





















































     @Transactional(readOnly = true)
    public TeacherSearchResultDto searchTeachers(String q, Long cursor, int limit,
                                                Long specialtyId, Long levelId, Long academicYearId, Long courseId) {
        // Simple implementation ignoring filters by session/course for brevity.
        // For filters, implement a native query joining session/teacher_year_profile/course and grouping by teacher.
        List<Object[]> rows = teacherRepository.searchTeachersNative(q, cursor, limit);

        List<TeacherListItemDto> items = rows.stream().map(r -> {
            Long id = ((Number) r[0]).longValue();
            String fullName = (String) r[1];
            String email = (String) r[2];
            String matricule = (String) r[3];
            Integer sessionsCount = ((Number) r[4]).intValue();
            return new TeacherListItemDto(id, fullName, email, matricule, sessionsCount);
        }).collect(Collectors.toList());

        Long nextCursor = null;
        if (!items.isEmpty()) {
            // cursor is simple: last item's id (since ordered DESC)
            nextCursor = items.get(items.size()-1).getTeacherId();
        }

        // totalCount optional: expensive for big tables; skip or cache. We'll return null to the client.
        return new TeacherSearchResultDto(items, nextCursor, null);
    }

    // Sessions mapping (calls SessionRepository)
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "teacherSessions", key = "#teacherId+'_'+#limit+'_'+(#start != null ? #start.toString() : 'null')+'_'+(#end != null ? #end.toString() : 'null')")
    public List<SessionDt> getSessionsForTeacher(Long teacherUserId, Long cursor, int limit) {
        List<Object[]> rows = sessionRepository.findSessionsByTeacherKeyset(teacherUserId, cursor, limit);
        List<SessionDt> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            String courseTitle = (String) r[1];
            java.sql.Timestamp ts = (java.sql.Timestamp) r[2];
            java.time.LocalDateTime start = ts != null ? ts.toLocalDateTime() : null;
            String campus = (String) r[3];
            String level = (String) r[4];
            String specialty = (String) r[5];
            String room = (String) r[6];
            out.add(new SessionDt(id, courseTitle, start, campus, level, specialty, room));
        }
        return out;
    }
}
