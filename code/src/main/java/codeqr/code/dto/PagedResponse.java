
package codeqr.code.dto;

import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    private Long lastCursor; // last ID returned (client can use as next cursor)
    private boolean more;

    public PagedResponse() {}
    public PagedResponse(List<T> content, Long lastCursor, boolean more) {
        this.content = content; this.lastCursor = lastCursor; this.more = more;
    }
    public List<T> getContent(){ return content; }
    public void setContent(List<T> c){ this.content = c; }
    public Long getLastCursor(){ return lastCursor; }
    public void setLastCursor(Long l){ this.lastCursor = l; }
    public boolean isMore(){ return more; }
    public void setMore(boolean m){ this.more = m; }
}
