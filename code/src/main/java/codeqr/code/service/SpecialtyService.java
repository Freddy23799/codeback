package codeqr.code.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import codeqr.code.dto.SpecialtyCreateDto;
import codeqr.code.dto.SpecialtyDto;
import codeqr.code.model.Department;
import codeqr.code.model.Specialty;
import codeqr.code.repository.DepartmentRepository;
import codeqr.code.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository repo;
    private final DepartmentRepository departmentRepository;

    // Conversion en DTO
    @Transactional(readOnly = true)
@Cacheable(cacheNames = "specialties", key = "'all'")
    public List<SpecialtyDto> listAll() {
        // findAll is annotated @EntityGraph => department préchargé -> pas de N+1
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SpecialtyDto create(SpecialtyCreateDto in) {
        Department d = departmentRepository.findById(in.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
        Specialty s = new Specialty();
        s.setName(in.getName());
        s.setDepartment(d);
        Specialty saved = repo.save(s);
        return toDto(saved);
    }

    @Transactional
    public SpecialtyDto update(Long id, SpecialtyCreateDto in) {
        Specialty s = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        s.setName(in.getName());
        Department d = departmentRepository.findById(in.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
        s.setDepartment(d);
        Specialty saved = repo.save(s);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repo.deleteById(id);
    }

    private SpecialtyDto toDto(Specialty s) {
        Long depId = null;
        String depName = null;
        Department d = s.getDepartment();
        if (d != null) {
            depId = d.getId();
            depName = d.getName();
        }
        return new SpecialtyDto(s.getId(), s.getName(), depId, depName);
    }

    // Récupérer par ID
    public Optional<Specialty> findById(Long id) {
        return repo.findById(id);
    }

    // Récupérer par department
    public List<Specialty> findByDepartmentId(Long departmentId) {
        return repo.findByDepartmentId(departmentId);
    }

    
}
