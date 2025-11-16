package codeqr.code.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Nombre total d'enregistrements (ex: total des présences/attendances)
     */
    private long totalCourses;

    /**
     * Nombre d'enregistrements marqués PRESENT
     */
    private long attended;

    /**
     * Nombre d'enregistrements marqués ABSENT
     */
    private long absent;

    /**
     * Notifications non lues pour l'utilisateur
     */
    private long unreadNotifications;
}
