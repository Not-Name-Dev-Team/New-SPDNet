package me.catand.spdnetserver.config;

import me.catand.spdnetserver.security.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

	// SPDNet: 静态资源缓存策略。
	// 带内容哈希的 /assets/** 用一年强缓存(immutable)；index.html 及无哈希资源不设强缓存，
	// 依赖 Last-Modified 回源校验，避免客户端长期引用旧页面。
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/assets/**")
			.addResourceLocations("classpath:/static/assets/")
			.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

		registry.addResourceHandler("/**")
			.addResourceLocations("classpath:/static/")
			.setCacheControl(CacheControl.noCache())
			.resourceChain(true)
			.addResolver(new PathResourceResolver() {
				@Override
				protected Resource getResource(String resourcePath, Resource location) throws IOException {
					Resource requestedResource = location.createRelative(resourcePath);
					if (requestedResource.exists() && requestedResource.isReadable()) {
						return requestedResource;
					}
					// SPA 回退：未知路径一律交给前端路由处理
					return new ClassPathResource("/static/index.html");
				}
			});
	}
}
