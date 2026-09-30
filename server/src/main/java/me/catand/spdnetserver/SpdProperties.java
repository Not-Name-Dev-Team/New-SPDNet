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
    private String bannedWordsFile;
    /**
     * Socket.IO 监听端口。默认 32814（历史值）；同一台机器跑第二个实例时必须改，
     * 否则第二个实例会因端口占用启动失败。
     *
     * 配置键是 {@code spd.socket-port}（kebab-case，与 Spring 官方推荐一致）。
     */
    private int socketPort = 32814;
}
