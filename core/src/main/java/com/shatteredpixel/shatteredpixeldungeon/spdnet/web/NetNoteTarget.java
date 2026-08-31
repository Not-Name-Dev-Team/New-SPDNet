package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.watabou.noosa.Image;

/**
 * SPDNet: 地牢留言(Ping)系统 - 一次"ping 目标"选择所产生的目标对象封装。
 * 见 test/ping-design.md 阶段C / §6：
 * - 携带留言所需最小数据（格坐标、类型、快照、目标玩家名）
 * - 提供预览图标(icon) + 展示名(name) 供 NetWndLeaveNote 顶部预览；
 *   icon 为分离图片，不注册进场景 group，避免污染当前层 / 线程竞争。
 * 字段约定：
 * - 仅 PLAYER 类型带 targetName（客户端不提交 snapshot，由服务端主动向目标索取快照）；
 * - 仅 MOB/ITEM/PLANT/TRAP 带 snapshot（现场对象以 Bundle 串行化，保留具体类名以便观看向还原）；
 * - FLOOR 两者皆空（只存坐标 + 文本）。
 */
public class NetNoteTarget {
	// 留言格坐标
	public final int pos;
	// 留言类型: PLAYER / MOB / ITEM / PLANT / TRAP / FLOOR
	public final String noteType;
	// 展示名（预览标题 / 选项名）
	public final String name;
	// 预览图标（分离图片）
	public final Image icon;
	// 非 PLAYER/FLOOR 的对象快照（Bundle 字符串）；PLAYER/FLOOR 为 null
	public final String snapshot;
	// PLAYER 类型: 目标玩家名；其余为 null
	public final String targetName;

	public NetNoteTarget(int pos, String noteType, String name, Image icon, String snapshot, String targetName) {
		this.pos = pos;
		this.noteType = noteType;
		this.name = name;
		this.icon = icon;
		this.snapshot = snapshot;
		this.targetName = targetName;
	}
}