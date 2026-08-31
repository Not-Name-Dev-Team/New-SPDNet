package me.catand.spdnetserver.data.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

/**
 * SPDNet: 地牢留言(Ping)系统 - 客户端发送留言点赞/删除请求（复用同一 id 结构）。
 * 见 test/ping-design.md §7 NOTE_LIKE / NOTE_DELETE。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CNoteId extends Data {
	// 目标留言 id
	private int id;
}