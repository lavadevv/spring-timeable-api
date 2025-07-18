package ext.vnua.tkb_api_lavadev.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalRequest {
    private PagingRequest paging;
    private List<OrderingRequest> ordering;
}
