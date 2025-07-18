package ext.vnua.tkb_api_lavadev.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequest {
    private int hoc_ky;
    private String ten_hoc_ky;
}
