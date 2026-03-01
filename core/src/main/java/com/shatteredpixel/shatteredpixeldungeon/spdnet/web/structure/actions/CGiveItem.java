package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;
import com.watabou.utils.Bundle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CGiveItem extends Data {
	private String targetName;
	private String item;

	public CGiveItem(String targetName, Item item) {
		this.targetName = targetName;
		Bundle bundle = new Bundle();
		// 修复刷物品bug - 只发送1个物品，而不是全部数量
		// 在序列化前临时将数量设为1
		int originalQuantity = item.quantity();
		item.quantity(1);
		bundle.put("item", item);
		this.item = bundle.toString();
		// 恢复原始数量，因为本地只移除1个
		item.quantity(originalQuantity);
	}
}
