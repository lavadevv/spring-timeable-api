package ext.vnua.tkb_api_lavadev.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetListTermResponse {

    private String termCode;

    private String termName;

    private String termStartDate;

    private String termEndDate;
}
