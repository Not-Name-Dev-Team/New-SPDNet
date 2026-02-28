package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.PlayerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlayerDocumentRepository extends JpaRepository<PlayerDocument, Long> {

	List<PlayerDocument> findByPlayerId(Long playerId);

	void deleteByPlayerId(Long playerId);

	@Transactional
	void deleteByPlayerIdAndDocumentType(Long playerId, String documentType);
}
