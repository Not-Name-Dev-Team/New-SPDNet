package com.shatteredpixel.shatteredpixeldungeon.spdnet.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.NetNote;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.NetNoteStore;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;

import java.util.List;
import java.util.Map;

/**
 * SPDNet: 地牢留言(Ping)系统 - 留言标记渲染层（§4 阶段D.3）。
 * 采用 Group + 每格小 Image 标记（非继承 DungeonTilemap 维护全层 tile 数组），
 * 天然支持动态 setData；未探索(未 visited)格上的标记在 fog 之上透出，正文受放大镜 visited 门控。
 *
 * 由 GameScene 挂载（fog 之后、最高显示），换层重建场景后由 Handler 重灌 setData。
 */
public class NetNoteOverlay extends Group {

	// 描边金色（高不透明度，fog 之上透出）
	private static final int BORDER_COLOR = 0xE0FFD700;
	// 边框厚度：窄细描边，紧贴格子最边缘
	private static final float BORDER = 1.0f;

	/**
	 * 用 NetNoteStore 当前层数据重建标记。服务端不做 pos 校验，越界在此跳过（唯一防线）。
	 * 标记改为在地块最边缘勾勒一圈窄细金色边框（4 段 ColorBlock），不再实心覆盖该格，
	 * 避免大号实心方块遮挡地形，同时保持 fog 之上透出。
	 */
	public void setData() {
		clear();
		if (Dungeon.level == null) {
			return;
		}
		int len = Dungeon.level.length();
		if (len <= 0) {
			return;
		}
		for (Map.Entry<Integer, List<NetNote>> entry : NetNoteStore.allByPos().entrySet()) {
			int pos = entry.getKey();
			if (pos < 0 || pos >= len) {
				continue;
			}
			addBorder(pos);
		}
	}

	// 在格子最边缘画一圈宽度为 BORDER 的窄边框
	private void addBorder(int pos) {
		float x = DungeonTilemap.tileToWorld(pos).x;
		float y = DungeonTilemap.tileToWorld(pos).y;
		int size = DungeonTilemap.SIZE;

		// 上
		ColorBlock top = new ColorBlock(size, BORDER, BORDER_COLOR);
		top.x = x; top.y = y; add(top);
		// 下
		ColorBlock bottom = new ColorBlock(size, BORDER, BORDER_COLOR);
		bottom.x = x; bottom.y = y + size - BORDER; add(bottom);
		// 左
		ColorBlock left = new ColorBlock(BORDER, size, BORDER_COLOR);
		left.x = x; left.y = y; add(left);
		// 右
		ColorBlock right = new ColorBlock(BORDER, size, BORDER_COLOR);
		right.x = x + size - BORDER; right.y = y; add(right);
	}
}