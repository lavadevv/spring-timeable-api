package ext.vnua.tkb_api_lavadev.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api")
@Data
public class ApiProperties {
    private String baseUrl;
    private Auth auth = new Auth();
    private String getdshocky;
    private String gettkbtuantheohocky;
    private String getdshockyPhu;
    private String gettkbtuantheohockyPhu;

    @Data
    public static class Auth {
        private String login;
    }

    public String getLoginPath() {
        return auth.login;
    }

    public String getGetTermPath() {
        return getdshocky;
    }

    public String getGetTermPathSecond() {
        return getdshockyPhu;
    }

    public String getGetListTimeablePath() {
        return gettkbtuantheohocky;
    }

    public String getGetListTimeablePathSecond() {
        return gettkbtuantheohockyPhu;
    }

    public String getAuthor() {
        return "Lavadev - DoPhucLam";
    }
}