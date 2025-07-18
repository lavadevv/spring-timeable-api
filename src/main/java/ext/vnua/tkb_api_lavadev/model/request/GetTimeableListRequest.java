package ext.vnua.tkb_api_lavadev.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTimeableListRequest {
    @JsonProperty("termCode")
    private String termCode;
}
