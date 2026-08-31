package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SPDNet: 地牢留言(Ping)系统 - 客户端发送创建留言请求。
 * 见 test/ping-design.md §7 NOTE_CREATE。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CNoteCreate extends Data {
	// 留言所在格
	private int pos;
	// 留言类型备注: PLAYER / MOB / ITEM / PLANT / TRAP / FLOOR
	private String noteType;
	// 留言文本
	private String message;
	// PLAYER 类型: 被留言的目标玩家名（其余类型为 null）
	private String targetName;
	// PLAYER 之外的对象快照 (Bundle 字符串)；PLAYER 类型客户端不提交, 由服务端主动索取
	private String snapshot;
}