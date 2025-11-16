package codeqr.code.dto;


import java.util.List;


public record StudentListWithEnrollmentsDTO(StudentListDTO student, List<EnrollmentWithSessionsDTO> enrollments) {}