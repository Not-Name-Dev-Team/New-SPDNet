package me.catand.spdnetserver.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.entitys.DungeonNote;
import me.catand.spdnetserver.entitys.NoteLike;
import me.catand.spdnetserver.repositories.DungeonNoteRepository;
import me.catand.spdnetserver.repositories.NoteLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SPDNet: 地牢留言(Ping)系统 - 留言业务（DB 层）。
 * 服务端权威存储：seed/depth 由 Handler 从请求者 Status 取，本服务只负责存取与一致性。
 * 见 test/ping-design.md §6 / §8 阶段B.1-B.3。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
	private final DungeonNoteRepository noteRepository;
	private final NoteLikeRepository noteLikeRepository;

	// 同 (seed, depth) 留言上限
	public static final int MAX_NOTES_PER_FLOOR = 100;

	/**
	 * 新建留言（非 PLAYER 直接携带快照；PLAYER 由外部随后回填 snapshot=null 占位）。
	 */
	public DungeonNote createNote(long seed, int depth, int pos, String noteType,
	                              String snapshot, String message, String author, String authorMode) {
		DungeonNote note = new DungeonNote();
		note.setSeed(seed);
		note.setDepth(depth);
		note.setPos(pos);
		note.setNoteType(noteType);
		note.setSnapshot(snapshot);
		note.setMessage(message);
		note.setAuthor(author);
		note.setAuthorMode(authorMode);
		note.setLikes(0);
		note.setCreateTime(LocalDateTime.now());
		return noteRepository.save(note);
	}

	public long countBySeedAndDepth(long seed, int depth) {
		return noteRepository.countBySeedAndDepth(seed, depth);
	}

	public DungeonNote getNote(int id) {
		return noteRepository.findById(id).orElse(null);
	}

	public List<DungeonNote> findBySeedAndDepth(long seed, int depth) {
		return noteRepository.findBySeedAndDepth(seed, depth);
	}

	/**
	 * 该客户端对 (seed, depth) 层已点赞的留言 id 集合（用于 REPLACE 初始化"我已赞"）。
	 */
	public List<Integer> myLikedIds(long seed, int depth, String playerName) {
		List<Integer> noteIds = findBySeedAndDepth(seed, depth).stream().map(DungeonNote::getId).toList();
		if (noteIds.isEmpty()) {
			return List.of();
		}
		return noteLikeRepository.findByPlayerNameAndNoteIdIn(playerName, noteIds).stream()
			.map(NoteLike::getNoteId)
			.toList();
	}

	/**
	 * 点赞 toggle：已赞则取消(likes-1)、未赞则点赞(likes+1)。
	 * 更新 NoteLike 表与 DungeonNote.likes 计数器列同事务，保证一致性。
	 *
	 * @return toggle 后是否处于已赞状态
	 */
	@Transactional
	public boolean toggleLike(DungeonNote note, String playerName) {
		int noteId = note.getId();
		if (noteLikeRepository.existsByNoteIdAndPlayerName(noteId, playerName)) {
			noteLikeRepository.deleteByNoteIdAndPlayerName(noteId, playerName);
			note.setLikes(Math.max(0, note.getLikes() - 1));
			noteRepository.save(note);
			return false;
		} else {
			NoteLike like = new NoteLike();
			like.setNoteId(noteId);
			like.setPlayerName(playerName);
			noteLikeRepository.save(like);
			note.setLikes(note.getLikes() + 1);
			noteRepository.save(note);
			return true;
		}
	}

	/**
	 * 删除留言并连带其点赞记录（仅作者调用）。
	 */
	@Transactional
	public void deleteNote(DungeonNote note) {
		noteRepository.delete(note);
		noteLikeRepository.deleteByNoteId(note.getId());
	}

	/**
	 * 按 id 删除留言（用于 pending 占位行清理）。
	 */
	@Transactional
	public void deleteNoteById(int id) {
		DungeonNote note = getNote(id);
		if (note != null) {
			deleteNote(note);
		}
	}

	/**
	 * 回填 PLAYER 类型留言的服务端权威快照。
	 */
	public DungeonNote fillSnapshot(DungeonNote note, String snapshot) {
		note.setSnapshot(snapshot);
		return noteRepository.save(note);
	}

	/**
	 * 单条留言 JSON（阶段A约定字段，PLAYER/FLOOR 无 snapshot 时不输出该键）。
	 * {id, noteType, pos, snapshot?, message, author, authorMode, likes, createTime}
	 */
	public String serializeNote(DungeonNote note) {
		JSONObject json = new JSONObject();
		json.put("id", note.getId());
		json.put("noteType", note.getNoteType());
		json.put("pos", note.getPos());
		if (note.getSnapshot() != null) {
			json.put("snapshot", note.getSnapshot());
		}
		json.put("message", note.getMessage());
		json.put("author", note.getAuthor());
		json.put("authorMode", note.getAuthorMode());
		json.put("likes", note.getLikes());
		json.put("createTime", note.getCreateTime());
		return JSON.toJSONString(json);
	}
}