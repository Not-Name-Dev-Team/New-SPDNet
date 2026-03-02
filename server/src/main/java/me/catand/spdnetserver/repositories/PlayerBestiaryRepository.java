package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.PlayerBestiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlayerBestiaryRepository extends JpaRepository<PlayerBestiary, Long> {

	List<PlayerBestiary> findByPlayerId(Long playerId);

	void deleteByPlayerId(Long playerId);

	@Transactional
	void deleteByPlayerIdAndBestiaryType(Long playerId, String bestiaryType);

	Optional<PlayerBestiary> findByPlayerIdAndBestiaryTypeAndEntityClass(Long playerId, String bestiaryType, String entityClass);
}
