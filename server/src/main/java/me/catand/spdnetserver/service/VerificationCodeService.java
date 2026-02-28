package me.catand.spdnetserver.service;

import lombok.Getter;
import me.catand.spdnetserver.config.MailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

	@Autowired
	private MailProperties mailProperties;

	private final Map<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();

	@Getter
	public static class VerificationCode {
		private final String code;
		private final LocalDateTime expireTime;
		private boolean verified;

		public VerificationCode(String code, int expireMinutes) {
			this.code = code;
			this.expireTime = LocalDateTime.now().plusMinutes(expireMinutes);
			this.verified = false;
		}

		public boolean isExpired() {
			return LocalDateTime.now().isAfter(expireTime);
		}

		public void markVerified() {
			this.verified = true;
		}
	}

	public String generateCode() {
		int length = mailProperties.getCodeLength();
		Random random = new Random();
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < length; i++) {
			code.append(random.nextInt(10));
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
