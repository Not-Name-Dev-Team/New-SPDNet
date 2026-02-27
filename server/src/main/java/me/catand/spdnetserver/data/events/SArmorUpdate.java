package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SArmorUpdate extends Data {
	private String name;
	private int armorTier;
}
