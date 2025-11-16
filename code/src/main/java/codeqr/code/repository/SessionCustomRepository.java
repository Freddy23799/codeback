package codeqr.code.repository;

import codeqr.code.model.Session;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

import org.springframework.data.repository.query.Param;

public interface SessionCustomRepository {
    @Query("""
        SELECT s
        FROM Session s
        WHERE (:teacherId IS NULL OR s.user.id = :teacherId)
          AND (:search IS NULL OR s.course.title LIKE %:search%)
          AND (:date IS NULL OR FUNCTION('DATE', s.startTime) = :date)
    """)
    Page<Session> findFiltered(@Param("teacherId") Long teacherId,
                               @Param("search") String search,
                               @Param("date") LocalDate date,
                               Pageable pageable);
}
