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
public class SAnkhUsed extends Data {
	private String name;
	private String cause;
	private int unusedBlessedAnkh;
	private int unusedUnblessedAnkh;
}
