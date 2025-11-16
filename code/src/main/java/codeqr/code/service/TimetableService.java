package codeqr.code.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import codeqr.code.dto.TimetableWeekResponse;
import codeqr.code.repository.TimetableQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class TimetableService {

    private final TimetableQueryRepository queryRepository;
    private final ObjectMapper objectMapper;

    public TimetableService(TimetableQueryRepository queryRepository, ObjectMapper objectMapper) {
        this.queryRepository = queryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Retourne la DTO correspondant à la dernière semaine pour userId
     * @param userId identifiant utilisateur (créateur)
     * @return TimetableWeekResponse ou null si aucune
     * @throws RuntimeException si parsing JSON échoue
     */
    public TimetableWeekResponse getLastWeekForUser(Long userId) {
        log.debug("Fetching last week JSON for userId={}", userId);
        String json = queryRepository.fetchLastWeekJson(userId);
        if (!StringUtils.hasText(json)) {
            log.debug("No last week found for userId={}", userId);
            return null;
        }
        try {
            // json is a JSON string => map to DTO
            TimetableWeekResponse dto = objectMapper.readValue(json, TimetableWeekResponse.class);
            log.debug("Parsed TimetableWeekResponse for userId={} => emplois count={}", userId, dto.getEmplois() == null ? 0 : dto.getEmplois().size());
            return dto;
        } catch (Exception e) {
            log.error("Failed to parse timetable JSON for userId={}. JSON={}", userId, json, e);
            throw new RuntimeException("Erreur parsing timetable JSON", e);
        }
    }
}
