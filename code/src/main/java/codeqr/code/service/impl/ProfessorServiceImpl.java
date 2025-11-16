
package codeqr.code.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // si ta classe s'appelle Professor, change ici
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import codeqr.code.dto.ProfessorDto;
import codeqr.code.model.Teacher;
import codeqr.code.repository.TeacherRepository;
import codeqr.code.service.interfaces.ProfessorService;

@Service
public class ProfessorServiceImpl implements ProfessorService {

    private final TeacherRepository repo;

    @Autowired
    public ProfessorServiceImpl(TeacherRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page<ProfessorDto> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").ascending());
        Page<Teacher> p;
        if (q == null || q.trim().isEmpty()) {
             System.out.println(">>> AUCUN FILTRE, ON APPELLE findAll()");
            p = repo.findAll(pageable);
        } else {
               System.out.println(">>> RECHERCHE AVEC FILTRE : " + q);
            String term = q.trim();
            p = repo.findByFullNameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(term, term, pageable);
        }
        return p.map(t -> new ProfessorDto(t.getId(), t.getMatricule(), t.getFullName(), t.getEmail()));
    }
}
