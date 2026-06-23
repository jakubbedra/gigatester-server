package com.konfyrm.gigatester.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "giga-tester")
@Getter
@Setter
public class GigaTesterProperties {

    private String adminUsername;
    private String adminPassword;
    private Security security = new Security();
    private Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Security {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret = "gigatester-jwt-secret-key-must-be-at-least-32-bytes-long";
        private long expirationMs = 86400000L;
    }
}
