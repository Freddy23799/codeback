// codeqr/code/dto/SessionsResponse.java
package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionsResponse {
    private List<SessionDTO> sessions;
    private long total;
    private int page; // 1-based (conforme front)
    private int size;
    private String teacherName; // facultatif
}
