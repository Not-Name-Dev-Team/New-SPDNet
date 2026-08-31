package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SPDNet: 地牢留言(Ping)系统 - 服务端下发该层留言列表（进/换层单播 REPLACE 与同层 DELTA 增量）。
 * 见 test/ping-design.md §7 NOTE_LIST。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SNoteList extends Data {
	// 模式: REPLACE(全量覆盖) / DELTA_ADD(单条 upsert) / DELTA_REMOVE(按 id 移除)
	private String mode;
	// 种子为毫秒时间戳（如 FUN/Daily ~1.7e12），必须 long，避免 int 溢出破坏客户端按 seed 过滤
	private long seed;
	private int depth;
	// 每条为单条留言 JSON 字符串: {id, noteType, pos, snapshot?, message, author, authorMode, likes, createTime}
	private List<String> notes;
	// REPLACE 时携带: 本客户端已点赞的留言 id 集合, 用于初始化"我已赞"
	private List<Integer> myLikedIds;
}