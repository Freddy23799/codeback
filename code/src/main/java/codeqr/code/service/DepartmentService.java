package codeqr.code.service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.DepartmentDto;
import codeqr.code.model.Department;
import codeqr.code.repository.DepartmentRepository;

// --------------- Department Service -----------------
@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository repo;

    public DepartmentService(DepartmentRepository repo) { this.repo = repo; }

   @Transactional(readOnly = true)
    public List<DepartmentDto> listAll() {
        return repo.findAllByOrderByNameAsc()
                .stream()
                .map(d -> new DepartmentDto(d.getId(), d.getName()))
                .collect(Collectors.toList());
    }
    public Optional<Department> findById(Long id) { return repo.findById(id); }
    public Optional<Department> findByName(String name) { return repo.findByName(name); }
    public Department save(Department entity) { return repo.save(entity); }
    public void delete(Long id) { repo.deleteById(id); }
}