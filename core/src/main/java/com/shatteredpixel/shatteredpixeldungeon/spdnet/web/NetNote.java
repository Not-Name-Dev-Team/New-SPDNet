package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * SPDNet: 地牢留言(Ping)系统 - 客户端留言 DTO。
 * 见 test/ping-design.md §6/§7：服务端下发的单条留言 JSON
 * {id, noteType, pos, snapshot?, message, author, authorMode, likes, createTime}
 * （snapshot 仅 PLAYER 之外的实体类型携带；PLAYER/FLOOR 可能无 snapshot）。
 */
public class NetNote {
	// 留言唯一 id（服务端 DB 主键）
	public int id;
	// 留言类型: PLAYER / MOB / ITEM / PLANT / TRAP / FLOOR
	public String noteType;
	// 留言格坐标
	public int pos;
	// 对象快照 Bundle 字符串（PLAYER/FLOOR 可能为 null；PLAYER 占位未回填时也为 null）
	public String snapshot;
	// 留言文本
	public String message;
	// 作者玩家名
	public String author;
	// 作者游戏模式名（IRONMAN/FUN/DAILY，服务端已过滤，实际只会是 FUN/DAILY），用于观看向作者名着色
	public String authorMode;
	// 点赞计数（全局计数；是否被我赞由 NetNoteStore.myLikedIds 推导）
	public int likes;
	// 创建时间（ISO 字符串，仅展示/排序参考）
	public String createTime;

	public static NetNote parse(String json) {
		NetNote note = new NetNote();
		JSONObject o = JSON.parseObject(json);
		note.id = o.getIntValue("id");
		note.noteType = o.getString("noteType");
		note.pos = o.getIntValue("pos");
		if (o.containsKey("snapshot") && o.get("snapshot") != null) {
			note.snapshot = o.getString("snapshot");
		}
		note.message = o.getString("message");
		note.author = o.getString("author");
		note.authorMode = o.getString("authorMode");
		note.likes = o.getIntValue("likes");
		note.createTime = o.getString("createTime");
		return note;
	}
}