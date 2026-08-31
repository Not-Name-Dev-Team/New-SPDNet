package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.DungeonNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SPDNet: 地牢留言(Ping)系统 - 留言数据访问层。
 * 见 test/ping-design.md §8 阶段B.2。
 */
@Repository
public interface DungeonNoteRepository extends JpaRepository<DungeonNote, Integer> {
	// 依 (seed, depth) 复合索引查询，决定留言同步性能
	List<DungeonNote> findBySeedAndDepth(long seed, int depth);

	// 同 (seed, depth) 留言计数，用于 100 条上限判定
	long countBySeedAndDepth(long seed, int depth);
}