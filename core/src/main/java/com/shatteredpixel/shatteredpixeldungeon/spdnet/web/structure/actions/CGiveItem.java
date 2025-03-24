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
		bundle.put("item", item);
		this.item = bundle.toString();
	}
}
