package me.catand.spdnetserver.config;

import me.catand.spdnetserver.security.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private AdminAuthInterceptor adminAuthInterceptor;

	// SPDNet: 注册管理员接口鉴权拦截器。
	// 拦截 /api/admin/**，但排除玩家自助(/prefixes/my*)与公开(/prefixes/public*)接口，
	// 以免破坏普通玩家的前缀自选与公开查看功能。其余 /api/admin/** 均要求 ADMIN 角色。
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(adminAuthInterceptor)
			.addPathPatterns("/api/admin/**")
			.excludePathPatterns(
				"/api/admin/prefixes/my",
				"/api/admin/prefixes/my/**",
				"/api/admin/prefixes/public/**"
			);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
			.addResourceLocations("classpath:/static/")
			.resourceChain(true)
			.addResolver(new PathResourceResolver() {
				@Override
				protected Resource getResource(String resourcePath, Resource location) throws IOException {
					Resource requestedResource = location.createRelative(resourcePath);
					if (requestedResource.exists() && requestedResource.isReadable()) {
						return requestedResource;
					}
					return new ClassPathResource("/static/index.html");
				}
			});
	}
}
