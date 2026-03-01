package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

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
public class SGiveItem extends Data {
	private String name;
	private String item;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SGiveItem(String name, String item) {
		this.name = name;
		this.item = item;
	}

	public Item getItemObject() {
		Bundle bundle = Bundle.fromString(item);
		return (Item) bundle.get("item");
	}
}
