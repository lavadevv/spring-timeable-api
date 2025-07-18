package ext.vnua.tkb_api_lavadev.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private int expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String userName;
    private String id;
    private String logtime;
    private String code;
    private String result;
    private String passtype;
    private String name;
    private String principal;
    private String roles;
    private String wcf;

    @JsonProperty(".expires")
    private String expires;

    @JsonProperty(".issued")
    private String issued;
}
