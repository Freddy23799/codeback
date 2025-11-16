package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDtos {
    private long weeklyCount;
    private long totalCount;
    private List<TimetableCardDto> weeklyTimetables;
}
