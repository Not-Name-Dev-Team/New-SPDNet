package me.catand.spdnetserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

	// SPDNet: 跨域来源白名单（逗号分隔）。生产环境前端由后端同源托管，通常无需跨域；
	// 仅开发环境（vite dev server 3000 端口）需要放行，故默认只允许 localhost。
	// 如需允许其他站点跨域调用，请在 application.yml 配置 spd.cors.allowedOrigins 补充域名。
	@Value("${spd.cors.allowedOrigins:http://localhost:3000,http://127.0.0.1:3000}")
	private String allowedOrigins;

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		// 禁止使用 "*"; 仅放行白名单内的来源，避免任意站点携带凭据跨域访问
		List<String> whitelist = Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isEmpty())
			.toList();
		config.setAllowedOriginPatterns(whitelist);
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);

		return new CorsFilter(source);
	}
}
