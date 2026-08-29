package me.catand.spdnetserver.security;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.entitys.UserRole;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * SPDNet: 管理员接口鉴权拦截器。
 *
 * 用途: 保护 /api/admin/** 下的管理接口，只有持有有效 JWT 且当前角色为 ADMIN 的用户才能访问。
 * 设计:
 *  - 从 Authorization 请求头读取 "Bearer <token>"。
 *  - 校验 token 合法性（签名、过期）。
 *  - 用 token 里的用户名实时查询数据库角色，确保管理员被降权/封禁后立即失效。
 *  - 玩家自助(/prefixes/my*)与公开(/prefixes/public*)接口在拦截器注册处排除，不进入本拦截器。
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeError(response, HttpStatus.UNAUTHORIZED.value(), "未登录");
            return false;
        }

        String token = header.substring(7);
        String name;
        try {
            name = jwtUtil.parseName(token);
        } catch (Exception e) {
            writeError(response, HttpStatus.UNAUTHORIZED.value(), "登录已过期，请重新登录");
            return false;
        }

        Player player = playerRepository.findByName(name);
        if (player == null || player.getRole() != UserRole.ADMIN) {
            writeError(response, HttpStatus.FORBIDDEN.value(), "无管理员权限");
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSON.toJSONString(ApiResponse.error(message)));
    }
}