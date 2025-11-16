package codeqr.code.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import codeqr.code.model.ExamDocument;

/**
 * Repository pour ExamDocument.
 * JpaSpecificationExecutor permet des recherches dynamiques et performantes.
 */
public interface ExamDocumentRepository extends JpaRepository<ExamDocument, Long>, JpaSpecificationExecutor<ExamDocument> {
    Optional<ExamDocument> findByUuid(String uuid);
}
