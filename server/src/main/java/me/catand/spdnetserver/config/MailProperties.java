package me.catand.spdnetserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spd.mail")
public class MailProperties {
	private boolean enabled = false;
	private String host = "smtp.gmail.com";
	private int port = 587;
	private String username = "";
	private String password = "";
	private String from = "";
	private String fromName = "SPDNet";
	private int codeExpireMinutes = 5;
	private int codeLength = 6;
}
