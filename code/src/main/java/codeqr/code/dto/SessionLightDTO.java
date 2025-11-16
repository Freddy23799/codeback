package codeqr.code.dto;


import java.time.LocalDateTime;

import codeqr.code.model.Attendance.Status;


public record SessionLightDTO(Long sessionId, Long studentYearProfileId, String courseTitle, String campusName,
String roomName, String teacherName, LocalDateTime startTime, LocalDateTime endTime, Status  status) {}