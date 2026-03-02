package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.DailyGameRecord;
import me.catand.spdnetserver.entitys.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyGameRecordRepository extends JpaRepository<DailyGameRecord, Long> {

	@Query("SELECT d FROM DailyGameRecord d LEFT JOIN FETCH d.gameRecord WHERE d.recordDate = :recordDate AND d.groupIndex = :groupIndex AND d.status = :status ORDER BY d.gameRecord.totalScore DESC")
	List<DailyGameRecord> findByRecordDateAndGroupIndexAndStatusOrderByScoreDesc(@Param("recordDate") LocalDate recordDate, @Param("groupIndex") Integer groupIndex, @Param("status") DailyGameRecord.DailyRecordStatus status);

	@Query("SELECT d FROM DailyGameRecord d LEFT JOIN FETCH d.gameRecord WHERE d.recordDate = :recordDate ORDER BY d.groupIndex ASC, d.gameRecord.totalScore DESC")
	List<DailyGameRecord> findByRecordDateOrderByGroupIndexAscScoreDesc(@Param("recordDate") LocalDate recordDate);

	@Query("SELECT d FROM DailyGameRecord d WHERE d.recordDate = :recordDate AND d.groupIndex = :groupIndex")
	List<DailyGameRecord> findByRecordDateAndGroupIndex(@Param("recordDate") LocalDate recordDate, @Param("groupIndex") Integer groupIndex);

	Optional<DailyGameRecord> findByPlayerAndRecordDateAndGroupIndex(Player player, LocalDate recordDate, Integer groupIndex);

	Optional<DailyGameRecord> findByPlayerIdAndRecordDateAndGroupIndex(Long playerId, LocalDate recordDate, Integer groupIndex);

	List<DailyGameRecord> findByPlayerAndStatus(Player player, DailyGameRecord.DailyRecordStatus status);

	List<DailyGameRecord> findByPlayerIdAndStatus(Long playerId, DailyGameRecord.DailyRecordStatus status);

	@Query("SELECT d FROM DailyGameRecord d WHERE d.player.name = :playerName AND d.recordDate = :recordDate AND d.groupIndex = :groupIndex")
	Optional<DailyGameRecord> findByPlayerNameAndRecordDateAndGroupIndex(@Param("playerName") String playerName, @Param("recordDate") LocalDate recordDate, @Param("groupIndex") Integer groupIndex);

	boolean existsByPlayerAndRecordDateAndGroupIndexAndStatusIn(Player player, LocalDate recordDate, Integer groupIndex, List<DailyGameRecord.DailyRecordStatus> statuses);

	@Query("SELECT d FROM DailyGameRecord d LEFT JOIN FETCH d.gameRecord WHERE d.player = :player AND d.recordDate = :recordDate AND d.groupIndex = :groupIndex")
	Optional<DailyGameRecord> findByPlayerAndRecordDateAndGroupIndexWithGameRecord(@Param("player") Player player, @Param("recordDate") LocalDate recordDate, @Param("groupIndex") Integer groupIndex);
}
