package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.entitys.PlayerPrefix;
import me.catand.spdnetserver.entitys.PlayerPrefixAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SPDNet: 玩家前缀分配数据访问层
 */
@Repository
public interface PlayerPrefixAssignmentRepository extends JpaRepository<PlayerPrefixAssignment, Long> {

	List<PlayerPrefixAssignment> findByPlayer(Player player);

	List<PlayerPrefixAssignment> findByPlayerId(Long playerId);

	Optional<PlayerPrefixAssignment> findByPlayerAndPrefix(Player player, PlayerPrefix prefix);

	boolean existsByPlayerAndPrefix(Player player, PlayerPrefix prefix);

	@Query("SELECT ppa FROM PlayerPrefixAssignment ppa WHERE ppa.player.id = :playerId AND ppa.active = true")
	Optional<PlayerPrefixAssignment> findActivePrefixByPlayerId(@Param("playerId") Long playerId);

	@Modifying
	@Query("UPDATE PlayerPrefixAssignment ppa SET ppa.active = false WHERE ppa.player.id = :playerId")
	void deactivateAllPrefixesForPlayer(@Param("playerId") Long playerId);

	void deleteByPlayerAndPrefix(Player player, PlayerPrefix prefix);

	long countByPrefix(PlayerPrefix prefix);
}
