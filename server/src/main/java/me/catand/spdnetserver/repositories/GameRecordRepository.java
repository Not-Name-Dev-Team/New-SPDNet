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
    @Query("SELECT g FROM GameRecord g WHERE " +
            "(g.player.name = :username OR :username IS NULL) AND " +
            "(g.win = :win OR :win IS NULL) AND " +
            "(g.gameMode = :gameMode OR :gameMode IS NULL) AND " +
            "(g.challengeAmount = :challengeAmount OR :challengeAmount IS NULL)")
    Page<GameRecord> findWithFilters(@Param("username") String username,
                                     @Param("win") Boolean win,
                                     @Param("gameMode") String gameMode,
                                     @Param("challengeAmount") Integer challengeAmount,
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
}
