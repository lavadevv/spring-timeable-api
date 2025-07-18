package ext.vnua.tkb_api_lavadev.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonTimeResponse {
    private int lessonPeriod;
    private String lessonTimeStart;
    private String lessonTimeEnd;
    private String lessonTime;
}
