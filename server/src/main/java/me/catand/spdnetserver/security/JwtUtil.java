package me.catand.spdnetserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/**
 * SPDNet: JWT 工具类（HS256 自签名）。
 *
 * 用途: 为管理员接口（/api/admin/**）提供无状态身份令牌。
 * 设计:
 *  - secret 从配置项 spd.jwt.secret 读取（需 >= 32 字节，HS256 要求）。
 *    该项目会发布到开源环境，因此 secret 不应硬编码进源码，而应通过环境变量
 *    SPD_JWT_SECRET 注入；未配置时使用运行时随机密钥兜底，可开箱即用。
 *  - 过期时间由 spd.jwt.expireDays 控制，默认 1 天。
 *  - token 中仅保存用户名(sub)与角色(role)，鉴权时用用户名查询数据库最新角色，
 *    从而保证管理员被降权/封禁后可立即失效，避免依赖 token 内的旧角色。
 */
@Component
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key;
    private final long expireDurationMs;

    public JwtUtil(@Value("${spd.jwt.secret:}") String secret,
                   @Value("${spd.jwt.expireDays:1}") long expireDays) {
        byte[] keyBytes;
        if (secret == null || secret.isBlank()) {
            // 未配置固定密钥：生成运行时随机密钥，避免将密钥提交到开源仓库
            keyBytes = new byte[48];
            new SecureRandom().nextBytes(keyBytes);
            LOGGER.warn("[SPDNet] 未配置 spd.jwt.secret，本次使用运行时随机密钥；重启后所有登录令牌将失效。"
                    + "生产环境请通过环境变量 SPD_JWT_SECRET 配置固定密钥（至少 32 字节）。");
        } else {
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                // 短密钥通过 SHA-256 派生为 32 字节，满足 HS256 强密钥要求
                LOGGER.warn("[SPDNet] spd.jwt.secret 长度不足 32 字节，已用 SHA-256 派生补足，安全性有限，建议更换更长的密钥。");
                raw = sha256(raw);
            }
            keyBytes = raw;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expireDurationMs = expireDays * 24L * 60 * 60 * 1000;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /**
     * 生成 JWT token。
     */
    public String generateToken(String name, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(name)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireDurationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 token 并返回用户名。
     *
     * @param token JWT 字符串
     * @return claim 中的用户名
     * @throws RuntimeException 当 token 非法、被篡改或已过期时抛出（由调用方处理为 401）
     */
    public String parseName(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}