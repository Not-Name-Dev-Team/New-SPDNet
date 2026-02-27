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
public class CAnkhUsed extends Data {
	private String cause;
	private int unusedBlessedAnkh;
	private int unusedUnblessedAnkh;
}
