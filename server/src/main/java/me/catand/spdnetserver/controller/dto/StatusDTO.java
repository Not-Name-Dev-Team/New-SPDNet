package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.Challenges;
import me.catand.spdnetserver.data.Status;

/**
 * SPDNet: 玩家状态信息DTO
 * 用于向前端返回处理后的状态信息，将挑战位掩码转换为实际挑战数量
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO {
    private int challenges;
    private long seed;
    private int heroClass;
    private int gameMode;
    private int depth;
    private int armorTier;
    private int pos;

    /**
     * 从 Status 实体创建 DTO，将挑战位掩码转换为实际挑战数量
     */
    public static StatusDTO fromStatus(Status status) {
        if (status == null) {
            return null;
        }
        StatusDTO dto = new StatusDTO();
        // SPDNet: 将挑战位掩码转换为实际开启的挑战数量
        dto.challenges = Challenges.countActiveChallenges(status.getChallenges());
        dto.seed = status.getSeed();
        dto.heroClass = status.getHeroClass();
        dto.gameMode = status.getGameMode();
        dto.depth = status.getDepth();
        dto.armorTier = status.getArmorTier();
        dto.pos = status.getPos();
        return dto;
    }
}
