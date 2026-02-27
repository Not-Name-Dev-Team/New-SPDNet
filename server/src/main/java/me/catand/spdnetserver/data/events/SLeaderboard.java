package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;
import me.catand.spdnetserver.data.Status;
import me.catand.spdnetserver.entitys.GameRecord;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SLeaderboard extends Data {
	private int totalPages;
	private int currentPage;
	private int totalElements;
	private List<String> gameRecords;
}
