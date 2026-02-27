package me.catand.spdnetserver.data.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;
import me.catand.spdnetserver.entitys.GameRecord;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CGameEnd extends Data {
	private GameRecord record;
}
