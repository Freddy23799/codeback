package codeqr.code.controller;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import codeqr.code.dto.ExamDocumentResponse;
import codeqr.code.dto.ExamDocumentUpdateRequest;
import codeqr.code.dto.ExamDocumentUploadRequest;
import codeqr.code.model.ExamDocument;
import codeqr.code.model.FileCategory;
import codeqr.code.repository.ExamDocumentRepository;
import codeqr.code.service.ExamDocumentService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/documents")
public class ExamDocumentController {

    private final Logger log = LoggerFactory.getLogger(ExamDocumentController.class);
    private final ExamDocumentService service;
    private final ExamDocumentRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ExamDocumentController(ExamDocumentService service, ExamDocumentRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    /**
     * Upload : meta en JSON (part "meta") ; fichiers dans "files".
     * Header X-Username facultatif (utilisé si pas de security principal).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ExamDocumentResponse>> upload(
            @RequestPart(value = "meta", required = false) String metaJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            @RequestParam(value = "username", required = false) String paramUsername
    ) {
        try {
            log.debug("Reçu upload metaJson present={} filesCount={}", metaJson != null, files == null ? 0 : files.length);

            if (files == null || files.length == 0) {
                log.warn("Upload tenté sans fichiers");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun fichier envoyé");
            }

            if (metaJson == null || metaJson.isBlank()) {
                log.warn("Upload meta manquante");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meta manquante");
            }

            ExamDocumentUploadRequest meta = mapper.readValue(metaJson, ExamDocumentUploadRequest.class);

            String username = headerUsername != null ? headerUsername : paramUsername;

            List<ExamDocumentResponse> res = service.uploadFiles(files, meta, username);
            log.info("Upload réussi: {} fichiers par user={}", res.size(), username);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            log.error("Erreur upload", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'upload: " + ex.getMessage(), ex);
        }
    }

    /**
     * Liste / recherche paginée.
     * Si X-Username est fourni (en header), on renvoie uniquement les documents uploadés par cet user.
     */
    @GetMapping
    public ResponseEntity<Page<ExamDocumentResponse>> list(
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            @RequestParam Optional<Long> yearId,
            @RequestParam Optional<Long> courseId,
            @RequestParam Optional<Long> levelId,
            @RequestParam Optional<List<Long>> specialtyIds,
            @RequestParam Optional<String> q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ExamDocumentResponse> p = service.search(
            yearId,
            courseId,
            levelId,
            specialtyIds,
            Optional.<FileCategory>empty(),
            q.orElse(null),
            Optional.ofNullable(headerUsername),
            page,
            size
        );
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ExamDocumentResponse> getMeta(@PathVariable String uuid) {
        ExamDocument doc = repo.findByUuid(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
        return ResponseEntity.ok(service.toResponse(doc));
    }

    /**
     * Téléchargement : on stream le flux décompressé (service.openDecompressedStream)
     * et on renvoie Content-Disposition avec filename* UTF-8.
     */
    @GetMapping("/{uuid}/download")
    public void download(@PathVariable String uuid, HttpServletResponse response) {
        try {
            ExamDocument doc = repo.findByUuid(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
            try (InputStream in = service.openDecompressedStream(doc)) {
                String original = doc.getOriginalFilename();
                if (!StringUtils.hasText(original)) original = doc.getStoredFilename() + ".bin";
                String encoded = URLEncoder.encode(original, StandardCharsets.UTF_8);
                // Content-Disposition: both filename and filename*
                String contentDisposition = "attachment; filename=\"" + original.replace("\"", "'") + "\"; filename*=UTF-8''" + encoded;
                response.setHeader("Content-Disposition", contentDisposition);

                String mime = doc.getMimeType() != null ? doc.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
                response.setContentType(mime);
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");

                org.springframework.util.StreamUtils.copy(in, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            log.error("Erreur téléchargement document uuid={}", uuid, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur téléchargement: " + ex.getMessage(), ex);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamDocumentResponse> updateMetadata(
            @PathVariable Long id,
            @RequestBody ExamDocumentUpdateRequest updateRequest,
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            @RequestParam(value = "username", required = false) String paramUsername
    ) {
        String username = headerUsername != null ? headerUsername : paramUsername;
        ExamDocument doc = service.updateMetadata(id, updateRequest, username);
        return ResponseEntity.ok(service.toResponse(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        service.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
