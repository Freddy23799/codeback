package codeqr.code.dto;


import java.util.List;


public record EnrollmentWithSessionsDTO(EnrollmentLightDTO enrollment, List<SessionLightDTO> sessions) {}