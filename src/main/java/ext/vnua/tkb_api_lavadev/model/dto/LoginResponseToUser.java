package ext.vnua.tkb_api_lavadev.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseToUser {
    private String author;
    private String message;
    private String token;
    private String username;
    private String name;
    private String email;
    private String role;
}
