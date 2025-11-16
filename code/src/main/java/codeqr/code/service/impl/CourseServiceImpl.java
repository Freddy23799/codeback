



package codeqr.code.service.impl;

import codeqr.code.dto.CourseDto;
import codeqr.code.model.Course;
import codeqr.code.repository.CourseRepository;
import codeqr.code.service.interfaces.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repo;

    @Autowired
    public CourseServiceImpl(CourseRepository repo) {
        this.repo = repo;
    }

 
    public Page<CourseDto> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").ascending());
        Page<Course> p;
        if (q == null || q.trim().isEmpty()) {
            p = repo.findAll(pageable);
        } else {
            String term = q.trim();
            p = repo.findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCase(term, term, pageable);
        }
        return p.map(c -> new CourseDto(c.getId(), c.getCode(), c.getTitle()));
    }
}
