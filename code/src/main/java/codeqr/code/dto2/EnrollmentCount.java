package codeqr.code.dto2;

import lombok.Data;

// package codeqr.code.repository;
@Data
public class EnrollmentCount {

    private String levelName;
    private Long count;

    // constructeur attendu par JPQL
    public EnrollmentCount(String levelName, Long count) {
        this.levelName = levelName;
        this.count = count;
    }

    // // getters (et éventuellement setters)
    // public String getLevelName() { return levelName; }
    // public Long getCount() { return count; }
}
