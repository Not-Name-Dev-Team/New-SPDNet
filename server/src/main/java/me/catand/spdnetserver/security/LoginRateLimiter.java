package me.catand.spdnetserver.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SPDNet: 登录失败频率限制器（宽松策略）。
 *
 * 用途: 为 /api/login 提供"多次连续失败后短暂冷却"的保护，缓解暴力破解，同时保持足够的宽容度，
 *       避免误伤正常玩家的登录体验。
 * 设计:
 *  - 以用户名为维度统计连续登录失败的次数；
 *  - 失败次数达到阈值后进入一段短冷却（默认连续5次失败后锁定30秒），期间拒绝登录并提示剩余等待秒数；
 *  - 登录成功后清零计数；
 *  - 仅使用内存计数，进程重启即失效，实现简单、无线程安全问题（ConcurrentHashMap + 原子计数）。
 * 说明: 属宽松限制——不封号、冷却短、仅针对连续失败，符合项目"登录频率限制不用太严格"的要求。
 */
@Component
public class LoginRateLimiter {

	// 连续失败多少次后进入冷却
	private final int maxFailures;
	// 冷却时长（秒）
	private final long lockSeconds;

	private final Map<String, AtomicInteger> failCounts = new ConcurrentHashMap<>();
	private final Map<String, Long> lockedUntil = new ConcurrentHashMap<>();

	public LoginRateLimiter() {
		// 宽松策略：5次失败、冷却30秒
		this.maxFailures = 5;
		this.lockSeconds = 30;
	}

	/**
	 * 查询该用户当前是否处于失败冷却中。
	 *
	 * @param username 用户名
	 * @return 若非 0 表示仍在冷却，返回剩余等待秒数；否则返回 0 表示允许尝试
	 */
	public long getRemainingLock(String username) {
		Long until = lockedUntil.get(username);
		if (until == null) {
			return 0;
		}
		long remain = until - System.currentTimeMillis();
		if (remain <= 0) {
			lockedUntil.remove(username);
			return 0;
		}
		return remain / 1000 + 1;
	}

	/**
	 * 记录一次登录失败；失败次数达到阈值时进入冷却。
	 */
	public void recordFailure(String username) {
		AtomicInteger counter = failCounts.computeIfAbsent(username, k -> new AtomicInteger());
		if (counter.incrementAndGet() >= maxFailures) {
			lockedUntil.put(username, System.currentTimeMillis() + lockSeconds * 1000);
			counter.set(0);
		}
	}

	/**
	 * 登录成功时调用，清零该用户的失败计数与冷却状态。
	 */
	public void reset(String username) {
		failCounts.remove(username);
		lockedUntil.remove(username);
	}
}