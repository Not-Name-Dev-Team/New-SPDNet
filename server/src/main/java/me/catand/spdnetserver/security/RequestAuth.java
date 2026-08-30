package me.catand.spdnetserver.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SPDNet: 登录态解析组件。
 *
 * 用途: 为普通玩家接口(/api/**)提供统一的"当前登录用户"解析能力。
 * 背景: 项目接口鉴权以 /api/admin/** 的 JWT 拦截器为主，但普通 /api/** 接口此前完全无登录态校验，
 *       导致 /api/player/{name}/private 等接口未登录即可访问并泄露他人隐私。本组件从请求头
 *       Authorization 中解析 Bearer 令牌对应的用户名，供各 controller 自行校验"只能操作/查看自己"。
 * 说明: 登录接口会对所有成功登录的用户签发 JWT（不再局限于 ADMIN），故普通玩家也持有有效 token，
 *       可被本组件解析。
 */
@Component
public class RequestAuth {

	@Autowired
	private JwtUtil jwtUtil;

	/**
	 * 从请求头解析当前登录用户名。
	 *
	 * @param request HTTP 请求
	 * @return 令牌对应的用户名；当未携带 / 格式错误 / 令牌无效或过期时返回 null
	 */
	public String resolveCurrentUsername(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		try {
			return jwtUtil.parseName(header.substring(7));
		} catch (Exception e) {
			// 令牌非法、被篡改或已过期
			return null;
		}
	}
}