package codeqr.code.dto;


public record EnrollmentLightDTO(Long enrollmentId, Long studentId, Long studentYearProfileId,
Long specialtyId, String specialtyName,
Long levelId, String levelName,
Long academicYearId, String academicYearName) {}