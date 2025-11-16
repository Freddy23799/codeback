package codeqr.code.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.CourseDO;
import codeqr.code.dto.TeacherSessionReportDTO;
import codeqr.code.model.Session;
import codeqr.code.model.Teacher;
import codeqr.code.repository.SessionRepository;
import codeqr.code.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherSessionReportService {

    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
 @Transactional
  public List<TeacherSessionReportDTO> getTeacherReports(String specialty, String level, Long academicYearId,
                                                      LocalDateTime dateFrom, LocalDateTime dateTo) {
    List<Teacher> teachers = teacherRepository.findAll();

    return teachers.stream().map(teacher -> {
        TeacherSessionReportDTO dto = new TeacherSessionReportDTO();
        dto.setTeacherId(teacher.getId());
        dto.setTeacherName(teacher.getFullName());
        dto.setTeacherEmail(teacher.getEmail());
        dto.setTeacherMatricule(teacher.getMatricule());
        dto.setTeacherGender(teacher.getSexe() != null ? teacher.getSexe().getName() : "N/A");

        // ---- Nouveauté : cours du professeur (id, code, title)
        if (teacher.getCourses() != null) {
            List<CourseDO> teacherCourses = teacher.getCourses().stream()
                .map(c -> new CourseDO(c.getId(), c.getCode(), c.getTitle()))
                .collect(Collectors.toList());
            dto.setTeacherCourses(teacherCourses);
        } else {
            dto.setTeacherCourses(new ArrayList<>());
        }

        List<Session> sessions = sessionRepository.findByUser(teacher.getUser())
            .stream()
            .filter(s -> specialty == null
                    || (s.getExpectedSpecialty() != null
                        && s.getExpectedSpecialty().getName() != null
                        && s.getExpectedSpecialty().getName().equalsIgnoreCase(specialty)))
            .filter(s -> level == null
                    || (s.getExpectedLevel() != null
                        && s.getExpectedLevel().getName() != null
                        && s.getExpectedLevel().getName().equalsIgnoreCase(level)))
            .filter(s -> academicYearId == null
                    || (s.getAcademicYear() != null
                        && s.getAcademicYear().getId() != null
                        && s.getAcademicYear().getId().equals(academicYearId)))
            .filter(s -> dateFrom == null || (s.getStartTime() != null && !s.getStartTime().isBefore(dateFrom)))
            .filter(s -> dateTo == null || (s.getStartTime() != null && !s.getStartTime().isAfter(dateTo)))
            .collect(Collectors.toList());

        List<TeacherSessionReportDTO.SessionDTO> sessionDTOs = sessions.stream().map(session -> {
            TeacherSessionReportDTO.SessionDTO sessDTO = new TeacherSessionReportDTO.SessionDTO();
            sessDTO.setSessionId(session.getId());
            sessDTO.setCourseTitle(session.getCourse() != null ? session.getCourse().getTitle() : null);
            sessDTO.setStartTime(session.getStartTime());
            sessDTO.setEndTime(session.getEndTime());
            sessDTO.setCampusName(session.getCampus() != null ? session.getCampus().getName() : null);
            sessDTO.setRoomName(session.getRoom() != null ? session.getRoom().getName() : null);
            sessDTO.setSpecialtyName(session.getExpectedSpecialty() != null ? session.getExpectedSpecialty().getName() : null);
            sessDTO.setLevelName(session.getExpectedLevel() != null ? session.getExpectedLevel().getName() : null);
            sessDTO.setAcademicYearLabel(session.getAcademicYear() != null ? session.getAcademicYear().getLabel() : null);

            List<TeacherSessionReportDTO.StudentDTO> studentDTOs = session.getAttendances()
                .stream()
                .map(a -> {
                    TeacherSessionReportDTO.StudentDTO st = new TeacherSessionReportDTO.StudentDTO();
                    if (a.getStudentYearProfile() != null && a.getStudentYearProfile().getStudent() != null) {
                        st.setStudentId(a.getStudentYearProfile().getStudent().getId());
                        st.setStudentName(a.getStudentYearProfile().getStudent().getFullName());
                        st.setStudentMatricule(a.getStudentYearProfile().getStudent().getMatricule());
                    }
                    st.setStatus(a.getStatus() != null ? a.getStatus().name() : "ABSENT");
                    return st;
                })
                .collect(Collectors.toList());

            sessDTO.setStudents(studentDTOs);
            return sessDTO;
        }).collect(Collectors.toList());

        dto.setSessions(sessionDTOs);
        return dto;
    }).collect(Collectors.toList());
}

}
