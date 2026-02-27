package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.GameRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, String> {
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
}