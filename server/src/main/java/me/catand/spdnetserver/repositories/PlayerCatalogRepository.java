package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.PlayerCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlayerCatalogRepository extends JpaRepository<PlayerCatalog, Long> {

	List<PlayerCatalog> findByPlayerId(Long playerId);

	void deleteByPlayerId(Long playerId);

	@Transactional
	void deleteByPlayerIdAndCatalogType(Long playerId, String catalogType);
}
