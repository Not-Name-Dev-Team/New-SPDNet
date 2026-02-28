package me.catand.spdnetserver.data.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CBestiaryUpdate extends Data {
	private String bestiaryType;
	private String entityClass;
	private boolean seen;
	private int encountered;
}
