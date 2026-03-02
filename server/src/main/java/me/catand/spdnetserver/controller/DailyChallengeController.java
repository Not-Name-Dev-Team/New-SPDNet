package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.controller.dto.DailyChallengeRecordDTO;
import me.catand.spdnetserver.controller.dto.DailySeedsDTO;
import me.catand.spdnetserver.entitys.DailyGameRecord;
import me.catand.spdnetserver.repositories.DailyGameRecordRepository;
import me.catand.spdnetserver.service.DailyChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/daily-challenge")
public class DailyChallengeController {

	@Autowired
	private DailyGameRecordRepository dailyGameRecordRepository;

	@Autowired
	private DailyChallengeService dailyChallengeService;

	@GetMapping("/today")
	public ResponseEntity<DailySeedsDTO> getTodaySeeds() {
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
		Map<String, Long> seeds = dailyChallengeService.getDailySeeds();
		return ResponseEntity.ok(new DailySeedsDTO(today.toString(), seeds));
	}

	@GetMapping("/records")
	public ResponseEntity<ApiResponse<List<DailyChallengeRecordDTO>>> getRecords(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Integer groupIndex) {

		if (date == null) {
			date = LocalDate.now(ZoneId.of("Asia/Shanghai"));
		}

		List<DailyGameRecord> records;
		if (groupIndex != null) {
			records = dailyGameRecordRepository.findByRecordDateAndGroupIndexAndStatusOrderByScoreDesc(
					date, groupIndex, DailyGameRecord.DailyRecordStatus.COMPLETED);
		} else {
			records = dailyGameRecordRepository.findByRecordDateOrderByGroupIndexAscScoreDesc(date);
		}

		List<DailyChallengeRecordDTO> dtoList = records.stream()
				.filter(r -> r.getStatus() == DailyGameRecord.DailyRecordStatus.COMPLETED)
				.map(this::convertToDTO)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success("获取成功", dtoList));
	}

	@GetMapping("/records/{playerName}")
	public ResponseEntity<ApiResponse<List<DailyChallengeRecordDTO>>> getPlayerRecords(
			@PathVariable String playerName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		final LocalDate queryDate = date != null ? date : LocalDate.now(ZoneId.of("Asia/Shanghai"));

		List<DailyGameRecord> allRecords = dailyGameRecordRepository.findAll();

		List<DailyChallengeRecordDTO> dtoList = allRecords.stream()
				.filter(r -> r.getPlayer() != null && playerName.equals(r.getPlayer().getName()))
				.filter(r -> r.getStatus() == DailyGameRecord.DailyRecordStatus.COMPLETED)
				.filter(r -> queryDate.equals(r.getRecordDate()))
				.map(this::convertToDTO)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success("获取成功", dtoList));
	}

	@GetMapping("/seeds/{date}")
	public ResponseEntity<DailySeedsDTO> getSeedsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		Map<String, Long> seeds = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			seeds.put("dailyGroup" + i, dailyChallengeService.generateSeed(date, i));
		}
		return ResponseEntity.ok(new DailySeedsDTO(date.toString(), seeds));
	}

	private DailyChallengeRecordDTO convertToDTO(DailyGameRecord record) {
		return DailyChallengeRecordDTO.fromEntity(record);
	}
}
