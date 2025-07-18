package ext.vnua.tkb_api_lavadev.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeableListInWeek {
    private int weekday;
    private int lessonPeriodStart;
    private int lessonCount;
    private String courseCode;
    private String courseName;
    private String credit;
    private String groupCode;
    private String groupPracticeCode;
    private String combinationCode;
    private String lecturerCode;
    private String lecturerName;
    private String classCode;
    private String className;
    private String roomCode;
    private Boolean isMakeupClass;
    private String learnDay;
}
