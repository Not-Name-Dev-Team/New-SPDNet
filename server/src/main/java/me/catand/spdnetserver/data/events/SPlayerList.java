package me.catand.spdnetserver.data.events;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;
import me.catand.spdnetserver.entitys.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SPlayerList extends Data {
	private List<JSONObject> players;

	public SPlayerList(Map<UUID, Player> playersMap) {
		List<Player> playerArrayList = new ArrayList<>(playersMap.values());
		players = new ArrayList<>();
		for (Player player : playerArrayList) {
			String jsonString = JSON.toJSONString(player);
			players.add(JSONObject.parseObject(jsonString));
		}
	}
}
