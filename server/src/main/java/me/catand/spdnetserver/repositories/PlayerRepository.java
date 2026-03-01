package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.entitys.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByName(String name);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Player> findByRole(UserRole role, Pageable pageable);
    long countByRole(UserRole role);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Player p JOIN p.achievements a WHERE p.name = :name AND a = :achievement")
    boolean hasAchievement(@Param("name") String name, @Param("achievement") String achievement);
}
