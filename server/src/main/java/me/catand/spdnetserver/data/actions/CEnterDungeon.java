package me.catand.spdnetserver.data.actions;

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
public class CEnterDungeon extends Data {
	private Status status;
}
