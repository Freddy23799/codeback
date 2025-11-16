package codeqr.code.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import codeqr.code.dto.AcademicYearCreateDto;
import codeqr.code.dto.AcademicYearDto;
import codeqr.code.model.AcademicYear;
import codeqr.code.repository.AcademicYearRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;







@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository repo;

    // public List<AcademicYear> findAll() {
    //     return repository.findAll();
    // }

    public Optional<AcademicYear> findById(Long id) {
        return repo.findById(id);
    }

    // public AcademicYear save(AcademicYear ay) {
    //     return repository.save(ay);
    // }

    // public void delete(Long id) {
    //     repository.deleteById(id);
    // }






  @Transactional
@Cacheable(cacheNames = "academicYears", key = "'all'")
    public List<AcademicYearDto> listAll() {
        return repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicYearDto create(AcademicYearCreateDto in) {
        AcademicYear a = new AcademicYear();
        a.setLabel(in.getLabel());
        a.setStartDate(in.getStartDate());
        a.setEndDate(in.getEndDate());
        a.setActive(in.isActive());
        AcademicYear saved = repo.save(a);
        return toDto(saved);
    }

    @Transactional
    public AcademicYearDto update(Long id, AcademicYearCreateDto in) {
        AcademicYear a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AcademicYear not found"));
        a.setLabel(in.getLabel());
        a.setStartDate(in.getStartDate());
        a.setEndDate(in.getEndDate());
        a.setActive(in.isActive());
        AcademicYear saved = repo.save(a);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        repo.deleteById(id);
    }

    private AcademicYearDto toDto(AcademicYear a) {
        return new AcademicYearDto(a.getId(), a.getLabel(), a.getStartDate(), a.getEndDate(), a.isActive());
    }





























}
