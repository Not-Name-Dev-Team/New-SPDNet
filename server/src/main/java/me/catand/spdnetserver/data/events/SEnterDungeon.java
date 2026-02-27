package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;
import me.catand.spdnetserver.data.Status;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SEnterDungeon extends Data {
	private String name;
	private Status status;
}
