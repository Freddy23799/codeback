
package codeqr.code.dto;

import lombok.Data;

@Data
public class RoomCreateDto {
    private String name;
    private Integer capacity;
    private Long campusId;
}