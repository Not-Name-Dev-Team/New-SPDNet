package me.catand.spdnetserver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spd")
public class SpdProperties {
    private String version;
    private String netVersion;
    private String motd;
}