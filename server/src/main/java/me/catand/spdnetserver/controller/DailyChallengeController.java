package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.controller.dto.DailyChallengeInfoDTO;
import me.catand.spdnetserver.controller.dto.DailyChallengeRecordDTO;
import me.catand.spdnetserver.controller.dto.DailySeedsDTO;
import me.catand.spdnetserver.controller.dto.PlayerPrefixDTO;
import me.catand.spdnetserver.entitys.DailyGameRecord;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.repositories.DailyGameRecordRepository;
import me.catand.spdnetserver.service.DailyChallengeService;
import me.catand.spdnetserver.service.PlayerPrefixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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

	@Autowired
	private PlayerPrefixService playerPrefixService;

	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

	@GetMapping("/today")
	public ResponseEntity<DailySeedsDTO> getTodaySeeds() {
		LocalDate today = LocalDate.now(ZONE_ID);
		Map<String, Long> seeds = dailyChallengeService.getDailySeeds();
		return ResponseEntity.ok(new DailySeedsDTO(today.toString(), seeds));
	}

	@GetMapping("/info")
	public ResponseEntity<ApiResponse<List<DailyChallengeInfoDTO>>> getDailyChallengeInfo(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		if (date == null) {
			date = LocalDate.now(ZONE_ID);
		}

		List<DailyChallengeInfoDTO> infoList = new ArrayList<>();
		String dateStr = date.toString();

		for (int groupIndex = 0; groupIndex < 3; groupIndex++) {
			DailyChallengeInfoDTO info = buildDailyChallengeInfo(date, groupIndex, dateStr);
			infoList.add(info);
		}

		return ResponseEntity.ok(ApiResponse.success("获取成功", infoList));
	}

	@GetMapping("/info/{groupIndex}")
	public ResponseEntity<ApiResponse<DailyChallengeInfoDTO>> getDailyChallengeInfoByGroup(
			@PathVariable Integer groupIndex,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		if (date == null) {
			date = LocalDate.now(ZONE_ID);
		}

		if (groupIndex < 0 || groupIndex > 2) {
			return ResponseEntity.badRequest().body(ApiResponse.error("无效的组别索引"));
		}

		DailyChallengeInfoDTO info = buildDailyChallengeInfo(date, groupIndex, date.toString());
		return ResponseEntity.ok(ApiResponse.success("获取成功", info));
	}

	private DailyChallengeInfoDTO buildDailyChallengeInfo(LocalDate date, int groupIndex, String dateStr) {
		long seed = dailyChallengeService.generateSeed(date, groupIndex);
		int challenges = dailyChallengeService.getChallengesForGroup(seed, groupIndex);
		List<String> challengeNames = dailyChallengeService.getChallengeNames(challenges);

		List<DailyGameRecord> allRecords = dailyGameRecordRepository.findByRecordDateAndGroupIndex(date, groupIndex);

		// SPDNet: 排除被ban玩家的记录
		allRecords = allRecords.stream()
				.filter(r -> r.getPlayer() == null || r.getPlayer().getRole() != me.catand.spdnetserver.entitys.UserRole.BANNED)
				.collect(Collectors.toList());

		int totalParticipants = allRecords.size();
		int completedCount = (int) allRecords.stream()
				.filter(r -> r.getStatus() == DailyGameRecord.DailyRecordStatus.COMPLETED)
				.count();
		int winCount = (int) allRecords.stream()
				.filter(r -> r.getStatus() == DailyGameRecord.DailyRecordStatus.COMPLETED)
				.filter(r -> r.getGameRecord() != null && r.getGameRecord().isWin())
				.count();

		double winRate = completedCount > 0 ? (winCount * 100.0 / completedCount) : 0;
		double completionRate = totalParticipants > 0 ? (completedCount * 100.0 / totalParticipants) : 0;

		DailyChallengeInfoDTO info = new DailyChallengeInfoDTO();
		info.setGroupIndex(groupIndex);
		info.setGroupName(dailyChallengeService.getGroupDisplayName(groupIndex));
		info.setDate(dateStr);
		info.setChallengeCount(dailyChallengeService.getChallengeCount(challenges));
		info.setChallengeNames(challengeNames);
		info.setTotalParticipants(totalParticipants);
		info.setCompletedCount(completedCount);
		info.setWinCount(winCount);
		info.setWinRate(Math.round(winRate * 100.0) / 100.0);
		info.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);

		return info;
	}

	@GetMapping("/records")
	public ResponseEntity<ApiResponse<List<DailyChallengeRecordDTO>>> getRecords(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Integer groupIndex) {

		if (date == null) {
			date = LocalDate.now(ZONE_ID);
		}

		List<DailyGameRecord> records;
		if (groupIndex != null) {
			records = dailyGameRecordRepository.findByRecordDateAndGroupIndexAndStatusOrderByScoreDesc(
					date, groupIndex, DailyGameRecord.DailyRecordStatus.COMPLETED);
		} else {
			records = dailyGameRecordRepository.findByRecordDateOrderByGroupIndexAscScoreDesc(date);
		}

		// SPDNet: 排除被ban玩家的记录
		List<DailyChallengeRecordDTO> dtoList = records.stream()
				.filter(r -> r.getStatus() == DailyGameRecord.DailyRecordStatus.COMPLETED)
				.filter(r -> r.getPlayer() == null || r.getPlayer().getRole() != me.catand.spdnetserver.entitys.UserRole.BANNED)
				.map(this::convertToDTO)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success("获取成功", dtoList));
	}

	@GetMapping("/records/{playerName}")
	public ResponseEntity<ApiResponse<List<DailyChallengeRecordDTO>>> getPlayerRecords(
			@PathVariable String playerName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		final LocalDate queryDate = date != null ? date : LocalDate.now(ZONE_ID);

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
		DailyChallengeRecordDTO dto = DailyChallengeRecordDTO.fromEntity(record);

		if (record.getPlayer() != null) {
			PlayerPrefixDTO prefixDTO = playerPrefixService.getActivePrefixDTO(record.getPlayer().getName());
			dto.setPrefix(prefixDTO);
		}

		return dto;
	}
}
