package me.catand.spdnetserver.repositories;

import me.catand.spdnetserver.entitys.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * SPDNet: 地牢留言(Ping)系统 - 点赞记录数据访问层。
 * 见 test/ping-design.md §8 阶段B.2。
 */
@Repository
public interface NoteLikeRepository extends JpaRepository<NoteLike, Integer> {
	// 唯一约束 (noteId, playerName) 下判断是否已赞
	boolean existsByNoteIdAndPlayerName(int noteId, String playerName);

	// toggle 取消点赞
	void deleteByNoteIdAndPlayerName(int noteId, String playerName);

	// 删除留言时连带删除其点赞记录
	void deleteByNoteId(int noteId);

	// 查询某玩家对某批留言的点赞记录（用于生成该客户端专属 myLikedIds）
	List<NoteLike> findByPlayerNameAndNoteIdIn(String playerName, Collection<Integer> noteIds);
}