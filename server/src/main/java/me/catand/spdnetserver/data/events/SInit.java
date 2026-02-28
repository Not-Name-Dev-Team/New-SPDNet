package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SInit extends Data {
	private String name;
	private String motd;
	private Map<String, Long> seeds;
	private Set<String> achievements;
}
