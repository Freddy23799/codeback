package codeqr.code.dto;

import java.util.List;

public class PagedResult<T> {
    public List<T> content;
    public long totalElements;
    public int page;
    public int size;
}
