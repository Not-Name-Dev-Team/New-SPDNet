package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SChatMessage extends Data {
	private String name;
	private String message;
	private LocalDateTime time;

	public SChatMessage(String name, String message) {
		this.name = name;
		this.message = message;
		this.time = LocalDateTime.now();
	}
}
