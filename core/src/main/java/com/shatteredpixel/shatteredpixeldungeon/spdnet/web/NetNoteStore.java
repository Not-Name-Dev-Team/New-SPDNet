package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SPDNet: 地牢留言(Ping)系统 - 客户端集中存储（§6）。
 * 持有当前 (seed, depth) 层的留言缓存 Map&lt;id,NetNote&gt; + 本地"我已赞"集合 myLikedIds。
 * overlay / examine 虚对象 / NetWndNoteList / 点赞 toggle 统一从这里读写；
 * `Handler.handleNoteList` 只负责更新它，展示层只管取数。
 *
 * 说明：
 * - myLikedIds 仅在 REPLACE 时由服务端初始化；DELTA 本地乐观 toggle 维护，失败靠 error 回告回滚。
 * - "是否为我自己的留言"按作者名 == Net.name 判定。
 * - 服务端不做 pos 校验（定案 2026-08-31），越界/无快照等由观看向渲染端跳过兜底。
 */
public class NetNoteStore {

	// id -> 留言（当前 seed+depth 层）
	private static final Map<Integer, NetNote> byId = new HashMap<>();
	// 本地"我已赞"留言 id 集合
	private static final Set<Integer> myLikedIds = new HashSet<>();

	private NetNoteStore() {
	}

	/**
	 * REPLACE 全量覆盖：清空现有缓存与"我已赞"，按服务端数据重建。
	 */
	public static void replace(List<String> noteJsons, List<Integer> likedIds) {
		byId.clear();
		myLikedIds.clear();
		if (likedIds != null) {
			myLikedIds.addAll(likedIds);
		}
		if (noteJsons != null) {
			for (String json : noteJsons) {
				if (json == null) continue;
				NetNote note = NetNote.parse(json);
				if (note.id <= 0) continue;
				byId.put(note.id, note);
			}
		}
	}

	/**
	 * DELTA_ADD 单条 upsert（创建/点赞后）。
	 */
	public static void upsert(String json) {
		NetNote note = NetNote.parse(json);
		if (note.id <= 0) return;
		byId.put(note.id, note);
	}

	/**
	 * DELTA_REMOVE 按 id 移除（删除后）。
	 */
	public static void remove(int id) {
		byId.remove(id);
		myLikedIds.remove(id);
	}

	/**
	 * 该格是否留有留言（examineCell 虚对象判定）。
	 */
	public static boolean hasNotes(int pos) {
		for (NetNote note : byId.values()) {
			if (note.pos == pos) return true;
		}
		return false;
	}

	/**
	 * 该格的留言列表（按创建序，id 递增即时间序）。
	 */
	public static List<NetNote> notesAt(int pos) {
		List<NetNote> list = new ArrayList<>();
		for (NetNote note : byId.values()) {
			if (note.pos == pos) list.add(note);
		}
		list.sort(Comparator.comparingInt(n -> n.id));
		return list;
	}

	/**
	 * 供 overlay 分组：pos -> 该格留言；已过滤越界 pos（服务端不做校验，这里是唯一防线）。
	 */
	public static Map<Integer, List<NetNote>> allByPos() {
		Map<Integer, List<NetNote>> map = new HashMap<>();
		int len = Dungeon.level == null ? -1 : Dungeon.level.length();
		if (len > 0) {
			for (NetNote note : byId.values()) {
				if (note.pos < 0 || note.pos >= len) continue;
				map.computeIfAbsent(note.pos, k -> new ArrayList<>()).add(note);
			}
		}
		return map;
	}

	// ================= 辅助 =================

	public static Set<Integer> allIds() {
		return Collections.unmodifiableSet(byId.keySet());
	}

	public static boolean isMyLiked(int id) {
		return myLikedIds.contains(id);
	}

	/**
	 * 本地乐观 toggle"我已赞"（失败经 error 回告时由调用方回滚）。
	 */
	public static void toggleMyLiked(int id, boolean nowLiked) {
		if (nowLiked) {
			myLikedIds.add(id);
		} else {
			myLikedIds.remove(id);
		}
	}

	/**
	 * 是否为当前登录玩家自己的留言（决定显示"删除"而非"点赞"）。
	 */
	public static boolean isMine(NetNote note) {
		return note.author != null && note.author.equals(Net.name);
	}

	/**
	 * PLAYER 占位未回填（快照为 null）的留言，观看向一律跳过渲染（双保险）。
	 */
	public static boolean renderable(NetNote note) {
		if (note == null) return false;
		// PLAYER / FLOOR 之外的实体类型若快照缺失（越权/占位残留）→ 画不出图标，跳过
		if ("PLAYER".equals(note.noteType) || "FLOOR".equals(note.noteType)) {
			return true; // 由各自分支决定：PLAYER 无快照跳、FLOOR 靠当前层地形
		}
		return note.snapshot != null;
	}

	public static void clear() {
		byId.clear();
		myLikedIds.clear();
	}

	/**
	 * SPDNet: 查看专用"该格有留言"虚对象。只在 examineCell 的查看收集里并入，
	 * 不加入 getObjectsAtCell / 物理 gameplay，不影响移动、右键、投掷等判断。
	 */
	public static class CellNotes {
		public final int pos;

		public CellNotes(int pos) {
			this.pos = pos;
		}
	}

	// 该虚对象类型的一个工具引用，便于 NetNoteStore 复用时避免误判（保留 NetHero 导入不被裁剪）
	@SuppressWarnings("unused")
	private static final Class NET_HERO = NetHero.class;
}