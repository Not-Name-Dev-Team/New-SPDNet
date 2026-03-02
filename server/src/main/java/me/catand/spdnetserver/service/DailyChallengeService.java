package me.catand.spdnetserver.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DailyChallengeService {

	private static final String DAILY_SALT = "SPDNET_DAILY_SALT_2024";
	private static final long TOTAL_SEEDS = 1000000000L;
	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

	private final Map<String, Long> seedCache = new ConcurrentHashMap<>();

	@Getter
	private LocalDate currentDate;

	public DailyChallengeService() {
		this.currentDate = LocalDate.now(ZONE_ID);
		generateDailySeeds();
	}

	public long generateSeed(LocalDate date, int groupIndex) {
		String cacheKey = date.toString() + "_" + groupIndex;
		return seedCache.computeIfAbsent(cacheKey, k -> computeSeed(date, groupIndex));
	}

	private long computeSeed(LocalDate date, int groupIndex) {
		try {
			String input = date.toString() + "|" + groupIndex + "|" + DAILY_SALT;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

			long seed = 0;
			for (int i = 0; i < 8; i++) {
				seed = (seed << 8) | (hash[i] & 0xFF);
			}
			return Math.abs(seed % TOTAL_SEEDS);
		} catch (NoSuchAlgorithmException e) {
			log.error("SHA-256 algorithm not available", e);
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}

	public void generateDailySeeds() {
		this.currentDate = LocalDate.now(ZONE_ID);
		for (int groupIndex = 0; groupIndex < 3; groupIndex++) {
			long seed = generateSeed(currentDate, groupIndex);
			seedCache.put("dailyGroup" + groupIndex, seed);
			log.info("生成每日挑战种子: 日期={}, 组别={}, 种子={}", currentDate, groupIndex, seed);
		}
	}

	public void resetDailySeeds() {
		seedCache.clear();
		generateDailySeeds();
		log.info("每日挑战种子已重置");
	}

	public Long getCachedSeed(String key) {
		return seedCache.get(key);
	}

	public Map<String, Long> getDailySeeds() {
		Map<String, Long> seeds = new ConcurrentHashMap<>();
		for (int i = 0; i < 3; i++) {
			Long seed = seedCache.get("dailyGroup" + i);
			if (seed != null) {
				seeds.put("dailyGroup" + i, seed);
			}
		}
		return seeds;
	}

	public boolean validateSeed(LocalDate date, int groupIndex, long seed) {
		long expectedSeed = generateSeed(date, groupIndex);
		return expectedSeed == seed;
	}

	public int getGroupIndexFromKey(String key) {
		if (key == null || !key.startsWith("dailyGroup")) {
			return -1;
		}
		try {
			return Integer.parseInt(key.substring("dailyGroup".length()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public String getGroupDisplayName(int groupIndex) {
		return switch (groupIndex) {
			case 0 -> "新手(0-3挑)";
			case 1 -> "高手(4-6挑)";
			case 2 -> "大师(7-9挑)";
			default -> "未知组别";
		};
	}
}
