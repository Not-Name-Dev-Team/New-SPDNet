package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.PlayerPrefix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SPDNet: 玩家前缀/称号数据访问层
 */
@Repository
public interface PlayerPrefixRepository extends JpaRepository<PlayerPrefix, Long> {
	Optional<PlayerPrefix> findByName(String name);

	boolean existsByName(String name);
}
