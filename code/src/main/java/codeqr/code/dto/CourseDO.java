package codeqr.code.dto;

public class CourseDO {
    private Long id;
    private String code;
    private String title;

    public CourseDO() {}

    public CourseDO(Long id, String code, String title) {
        this.id = id;
        this.code = code;
        this.title = title;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
