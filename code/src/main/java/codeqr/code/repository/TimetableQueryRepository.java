// TimetableQueryRepository.java
package codeqr.code.repository;

public interface TimetableQueryRepository {
    /**
     * Retourne le JSON (string) de la dernière semaine pour userId (ou null si aucune)
     */
    String fetchLastWeekJson(Long userId);
}
