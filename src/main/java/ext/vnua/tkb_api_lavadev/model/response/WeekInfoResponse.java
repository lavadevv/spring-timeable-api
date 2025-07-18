package ext.vnua.tkb_api_lavadev.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WeekInfoResponse {
    private int weekInTerm;
    private String weekInfo;
    private String weekStartDate;
    private String weekEndDate;
    private List<TimeableListInWeek> timeableListInWeekList;
}
