package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    // SPDNet: 修改查询逻辑支持铁人模式筛选（customSeed为空表示铁人模式）
    @Query("SELECT g FROM GameRecord g WHERE " +
            "(g.player.name = :username OR :username IS NULL) AND " +
            "(g.win = :win OR :win IS NULL) AND " +
            "(:gameMode IS NULL OR " +
            "  (:gameMode = 'NORMAL' AND (g.customSeed IS NOT NULL AND g.customSeed != '')) OR " +
            "  (:gameMode = 'IRONMAN' AND (g.customSeed IS NULL OR g.customSeed = '')) OR " +
            "  (:gameMode = 'DAILY' AND g.daily = true)" +
            ") AND " +
            "(g.challengeAmount = :challengeAmount OR :challengeAmount IS NULL) AND " +
            "(:bannedOnly = false AND g.player.role != 'BANNED' OR :bannedOnly = true AND g.player.role = 'BANNED')")
    Page<GameRecord> findWithFilters(@Param("username") String username,
                                     @Param("win") Boolean win,
                                     @Param("gameMode") String gameMode,
                                     @Param("challengeAmount") Integer challengeAmount,
                                     @Param("bannedOnly") Boolean bannedOnly,
                                     Pageable pageable);

    Page<GameRecord> findByWinTrue(Pageable pageable);
    Page<GameRecord> findByWinFalse(Pageable pageable);
    List<GameRecord> findByPlayerOrderByScoreDesc(Player player);
    Page<GameRecord> findByPlayerOrderByScoreDesc(Player player, Pageable pageable);
    Page<GameRecord> findByPlayer(Player player, Pageable pageable);
    long countByPlayer(Player player);
    long countByPlayerAndWinTrue(Player player);
    long countByWinTrue();
    void deleteByPlayer(Player player);

    // 新增查询方法
    Page<GameRecord> findByChallengeAmount(int challengeAmount, Pageable pageable);
    Page<GameRecord> findByGameMode(String gameMode, Pageable pageable);
    Page<GameRecord> findByWinTrueAndGameMode(String gameMode, Pageable pageable);

    // SPDNet: 获取铁人模式前三名（customSeed为空表示随机种子，即铁人模式）
    @Query("SELECT g FROM GameRecord g WHERE " +
            "g.player.role != 'BANNED' AND " +
            "(g.customSeed IS NULL OR g.customSeed = '') AND " +
            "g.daily = false " +
            "ORDER BY g.score DESC")
    List<GameRecord> findTop3IronmanPlayers(Pageable pageable);
}
