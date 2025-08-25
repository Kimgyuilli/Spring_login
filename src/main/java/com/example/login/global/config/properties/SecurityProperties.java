package com.example.login.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> publicUrls;
    private List<String> adminUrls;
    private String defaultRole;
    private long corsMaxAge = 3600L;
    
    public String[] getPublicUrlsArray() {
        return publicUrls != null ? publicUrls.toArray(new String[0]) : new String[0];
    }
    
    public String[] getAdminUrlsArray() {
        return adminUrls != null ? adminUrls.toArray(new String[0]) : new String[0];
    }
}