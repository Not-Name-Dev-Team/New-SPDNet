package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByName(String name);

    boolean existsByEmail(String email);

    Player findByName(String name);

    Player findByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Player p JOIN p.achievements a WHERE p.name = :name AND a = :achievement")
    boolean hasAchievement(@Param("name") String name, @Param("achievement") String achievement);
}
