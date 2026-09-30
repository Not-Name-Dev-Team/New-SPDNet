package me.catand.spdnetserver;

import me.catand.spdnetserver.entitys.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * SPDNet: 在线会话注册表——在线会话状态的唯一所有者与唯一写入口。
 *
 * <p>内部两张表是同一份会话状态的两个索引（两种查找需求，单表必有一边退化为 O(N) 遍历）：
 * <ul>
 *   <li>{@code bySession}: sessionId → Player，按连接找玩家（事件派发）</li>
 *   <li>{@code byName}: 玩家名 → sessionId，按名字找连接（查看/赠送/留言目标）</li>
 * </ul>
 *
 * <p>两者必须在同一临界区内成对更新。历史缺陷：增删分散在 connect 分支、disconnect 监听器、
 * 幽灵清理分支三处各自维护，任一处漏更新即产生矛盾状态——按 sessionId 取不到人（事件派发 NPE，
 * 且会跳过该事件的副作用）、或按名字指向已失效连接（重复登录误判、幽灵在线）。
 * 本类把这些操作收敛为语义方法，调用方无法再绕过配对更新。
 *
 * <p>锁只保护写路径（连接建立/断开，低频）；读路径直接查 {@link ConcurrentHashMap}，不加锁。
 */
public class SessionRegistry {

	private final Map<UUID, Player> bySession = new ConcurrentHashMap<>();
	private final Map<String, UUID> byName = new ConcurrentHashMap<>();
	private final Object lock = new Object();

	/**
	 * 登记结果。
	 *
	 * @param accepted 是否登记成功；false 表示同名会话仍存活，判定为重复登录
	 * @param ghost    被本次登记顶替掉的失效会话玩家，无则 null（供调用方补发 EXIT）
	 */
	public record RegisterResult(boolean accepted, Player ghost) {
		static RegisterResult duplicate() {
			return new RegisterResult(false, null);
		}

		static RegisterResult registered(Player ghost) {
			return new RegisterResult(true, ghost);
		}
	}

	/**
	 * 注销结果。
	 *
	 * @param player             被移除的玩家
	 * @param wasCurrentSession  本次会话是否仍是该玩家的当前会话。
	 *                           false 表示已被新会话接管，调用方不应广播退出（否则会把新会话一并退出）。
	 */
	public record UnregisterResult(Player player, boolean wasCurrentSession) {
	}

	/**
	 * 尝试登记新会话。重复登录判定与"清理失效会话后放行"在同一临界区内完成，
	 * 避免两个并发连接都通过存活检查后互相覆盖索引、留下孤儿会话。
	 *
	 * @param existingAlive 判断同名旧会话的连接是否仍存活。存活 → 拒绝登记（重复登录）；
	 *                      已失效 → 清理后放行。由调用方提供，使本类无需感知 SocketIOClient。
	 */
	public RegisterResult tryRegister(UUID sessionId, Player player, Predicate<UUID> existingAlive) {
		synchronized (lock) {
			UUID existing = byName.get(player.getName());
			Player ghost = null;
			if (existing != null) {
				if (existingAlive.test(existing)) {
					return RegisterResult.duplicate();
				}
				// 旧连接已失效（网络抖动产生的幽灵连接），清理占位后放行新连接
				ghost = bySession.remove(existing);
				byName.remove(player.getName());
			}
			bySession.put(sessionId, player);
			byName.put(player.getName(), sessionId);
			return RegisterResult.registered(ghost);
		}
	}

	/**
	 * 注销会话（幂等）。会话不存在时返回 null。
	 * 名字索引采用条件移除：仅当仍指向本会话时才删，避免旧连接的回调误删新会话的索引。
	 */
	public UnregisterResult unregister(UUID sessionId) {
		synchronized (lock) {
			Player player = bySession.remove(sessionId);
			if (player == null) {
				return null;
			}
			boolean wasCurrent = byName.remove(player.getName(), sessionId);
			return new UnregisterResult(player, wasCurrent);
		}
	}

	/** 按连接取玩家；会话已注销时返回 null。 */
	public Player playerOf(UUID sessionId) {
		return sessionId == null ? null : bySession.get(sessionId);
	}

	/** 按玩家名取当前会话 id；该玩家不在线时返回 null。 */
	public UUID sessionOf(String name) {
		return name == null ? null : byName.get(name);
	}

	/** 该玩家名当前是否已有登记会话（不判断连接是否存活）。 */
	public boolean isRegistered(String name) {
		return name != null && byName.containsKey(name);
	}

	/**
	 * 在线玩家快照（只读）。返回不可修改视图，防止调用方绕过本类的写入口直接改表。
	 */
	public Map<UUID, Player> onlinePlayers() {
		return Collections.unmodifiableMap(bySession);
	}

	/** 当前已登记的会话 id 快照，供巡检等遍历使用（避免遍历时并发修改）。 */
	public List<UUID> sessionIds() {
		return new ArrayList<>(bySession.keySet());
	}

	public int onlineCount() {
		return bySession.size();
	}
}
