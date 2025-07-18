package ext.vnua.tkb_api_lavadev.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeableListRequestBody {
    private FilterRequest filter;
    private AdditionalRequest additional;
}
