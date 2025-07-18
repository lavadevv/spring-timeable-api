package ext.vnua.tkb_api_lavadev.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagingRequest {
    private int limit;
    private int page;
}
