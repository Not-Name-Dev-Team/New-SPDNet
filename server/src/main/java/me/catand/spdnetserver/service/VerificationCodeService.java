package me.catand.spdnetserver.service;

import lombok.Getter;
import me.catand.spdnetserver.config.MailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

	@Autowired
	private MailProperties mailProperties;

	// SPDNet: 单次验证码允许的最大错误猜测次数，超过后验证码作废，须重新发送（防暴力破解）
	private static final int MAX_ATTEMPTS = 5;

	private final Map<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();

	@Getter
	public static class VerificationCode {
		private final String code;
		private final LocalDateTime expireTime;
		private LocalDateTime lastSendTime;
		private boolean verified;
		private int attempts;

		public VerificationCode(String code, int expireMinutes) {
			this.code = code;
			this.expireTime = LocalDateTime.now().plusMinutes(expireMinutes);
			this.lastSendTime = LocalDateTime.now();
			this.verified = false;
			this.attempts = 0;
		}

		public boolean isExpired() {
			return LocalDateTime.now().isAfter(expireTime);
		}

		/**
		 * 检查是否在冷却时间内（10秒）
		 */
		public boolean isInCooldown(int cooldownSeconds) {
			return LocalDateTime.now().isBefore(lastSendTime.plusSeconds(cooldownSeconds));
		}

		/**
		 * 获取剩余冷却时间（秒）
		 */
		public long getRemainingCooldown(int cooldownSeconds) {
			LocalDateTime cooldownEnd = lastSendTime.plusSeconds(cooldownSeconds);
			if (LocalDateTime.now().isAfter(cooldownEnd)) {
				return 0;
			}
			return java.time.Duration.between(LocalDateTime.now(), cooldownEnd).getSeconds();
		}

		/**
		 * 更新最后发送时间
		 */
		public void updateLastSendTime() {
			this.lastSendTime = LocalDateTime.now();
		}

		public void markVerified() {
			this.verified = true;
		}
	}

	// SPDNet: 使用加密安全的随机数生成验证码，避免 java.util.Random 可预测导致的验证码被猜到
	private final SecureRandom secureRandom = new SecureRandom();

	public String generateCode() {
		int length = mailProperties.getCodeLength();
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < length; i++) {
			code.append(secureRandom.nextInt(10));
		}
		return code.toString();
	}

	/**
	 * 获取或生成验证码
	 * 如果该邮箱已有未过期的验证码，则返回相同的验证码
	 * 否则生成新的验证码
	 */
	public String getOrGenerateCode(String email) {
		VerificationCode existing = codeStorage.get(email);
		if (existing != null && !existing.isExpired()) {
			// 如果验证码未过期，返回相同的验证码
			return existing.getCode();
		}
		// 生成新验证码
		return generateCode();
	}

	/**
	 * 检查是否可以发送验证码（是否在冷却时间外）
	 * @param email 邮箱
	 * @param cooldownSeconds 冷却时间（秒）
	 * @return 如果可以发送返回true，否则返回false
	 */
	public boolean canSendCode(String email, int cooldownSeconds) {
		VerificationCode existing = codeStorage.get(email);
		if (existing == null || existing.isExpired()) {
			return true;
		}
		return !existing.isInCooldown(cooldownSeconds);
	}

	/**
	 * 获取剩余冷却时间（秒）
	 */
	public long getRemainingCooldown(String email, int cooldownSeconds) {
		VerificationCode existing = codeStorage.get(email);
		if (existing == null || existing.isExpired()) {
			return 0;
		}
		return existing.getRemainingCooldown(cooldownSeconds);
	}

	/**
	 * 更新验证码的最后发送时间
	 */
	public void updateSendTime(String email) {
		VerificationCode existing = codeStorage.get(email);
		if (existing != null) {
			existing.updateLastSendTime();
		}
	}

	public void storeCode(String email, String code) {
		codeStorage.put(email, new VerificationCode(code, mailProperties.getCodeExpireMinutes()));
	}

	public boolean verifyCode(String email, String code) {
		VerificationCode stored = codeStorage.get(email);
		if (stored == null) {
			return false;
		}
		if (stored.isExpired()) {
			codeStorage.remove(email);
			return false;
		}
		if (!stored.getCode().equals(code)) {
			// SPDNet: 验证码错误次数+1，达到上限即作废并移除，须重新发送（防暴力破解）
			stored.attempts++;
			if (stored.attempts >= MAX_ATTEMPTS) {
				codeStorage.remove(email);
			}
			return false;
		}
		stored.markVerified();
		return true;
	}

	public boolean isCodeVerified(String email) {
		VerificationCode stored = codeStorage.get(email);
		if (stored == null || stored.isExpired()) {
			return false;
		}
		return stored.isVerified();
	}

	public void removeCode(String email) {
		codeStorage.remove(email);
	}

	public void cleanupExpiredCodes() {
		codeStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
	}
}
