package codeqr.code.dto;

import java.util.List;

public class SessionsPageDTO {
    private List<SessionSummaryDTO> content;
    private int page;           // 0-based page (front expects data.page)
    private int size;
    private long totalElements;
    private int totalPages;

    public SessionsPageDTO() {}

    public SessionsPageDTO(List<SessionSummaryDTO> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // getters / setters
    public List<SessionSummaryDTO> getContent() { return content; }
    public void setContent(List<SessionSummaryDTO> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
