package com.shatteredpixel.shatteredpixeldungeon.spdnet.update;


import com.badlogic.gdx.Net;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.AvailableUpdateData;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateService;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetConfig;
import com.watabou.noosa.Game;

import java.util.regex.Pattern;

public class SPDNetUpdates extends UpdateService {

	private static Pattern descPattern = Pattern.compile("(.*?)(\r\n|\n|\r)(\r\n|\n|\r)---", Pattern.DOTALL + Pattern.MULTILINE);
	private static Pattern versionCodePattern = Pattern.compile("internal version number: ([0-9]*)", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean supportsUpdatePrompts() {
		return true;
	}

	@Override
	public boolean supportsBetaChannel() {
		return true;
	}

	@Override
	public void checkForUpdate(boolean useMetered, boolean includeBetas, UpdateResultCallback callback) {

		if (!useMetered && !Game.platform.connectedToUnmeteredNetwork()) {
			callback.onConnectionFailed();
			return;
		}

		NetConfig.refreshConfig(new Net.HttpResponseListener() {
			@Override
			public void handleHttpResponse(Net.HttpResponse httpResponse) {
				if (NetConfig.config == null) {
					callback.onConnectionFailed();
				} else {
					String latestSPDVersion = NetConfig.config.getString("SPDVersion");
					String latestNetVersion = NetConfig.config.getString("NetVersion");
					if (isVersionNewer(ShatteredPixelDungeon.version, latestSPDVersion)
							|| isNetVersionNewer(ShatteredPixelDungeon.netVersion.split("-")[0], latestNetVersion)) {
						AvailableUpdateData update = new AvailableUpdateData();
						update.versionName = latestSPDVersion + "-" + latestNetVersion;
						update.desc = NetConfig.config.getString("changeLog");
						update.URL = NetConfig.config.getString("GithubUpdateUrl");
						update.giteeURL = NetConfig.config.getString("GiteeUpdateUrl");
						// SPDNet: 记录 APK 的 MD5，供 downloadGame 校验缓存
						update.apkMd5 = NetConfig.config.getString("ApkMd5");
						callback.onUpdateAvailable(update);
					} else {
						callback.onNoUpdateFound();
					}
				}
			}

			@Override
			public void failed(Throwable t) {
				callback.onConnectionFailed();
			}

			@Override
			public void cancelled() {
				callback.onConnectionFailed();
			}
		});

	}

	@Override
	public void initializeUpdate(AvailableUpdateData update) {
		Game.platform.openURI(update.URL);
	}

	@Override
	public boolean supportsReviews() {
		return false;
	}

	@Override
	public void initializeReview(ReviewResultCallback callback) {
		//does nothing, no review functionality here
		callback.onComplete();
	}

	@Override
	public void openReviewURI() {
		//does nothing
	}

	/**
	 * 远程版本是否比当前版本更新。
	 * 逐位比较，缺失的位按 0 补齐（"4.0" 与 "4.0.0" 等价），任一位不同即出结果。
	 */
	private static boolean isVersionNewer(String currentVersion, String newVersion) {
		int[] current = splitVersion(currentVersion);
		int[] latest = splitVersion(newVersion);
		for (int i = 0; i < Math.max(current.length, latest.length); i++) {
			int c = i < current.length ? current[i] : 0;
			int l = i < latest.length ? latest[i] : 0;
			if (c != l) {
				return c < l;
			}
		}
		return false;
	}

	/**
	 * Net 版本是否比当前版本更新。
	 * Net 版本号独立于破碎版本号演进，主版本必须保持一致才有可比性：
	 * 主版本更高时当前构建更新（如 4.0.0 不需要降级到 3.3.8 的 Net 版本），
	 * 主版本更低时交给主版本比较负责，此处不再重复报告更新。
	 */
	private static boolean isNetVersionNewer(String currentVersion, String newVersion) {
		int[] current = splitVersion(currentVersion);
		int[] latest = splitVersion(newVersion);
		int currentMajor = current.length > 0 ? current[0] : 0;
		int latestMajor = latest.length > 0 ? latest[0] : 0;
		if (currentMajor != latestMajor) {
			return false;
		}
		return isVersionNewer(currentVersion, newVersion);
	}

	/**
	 * 把版本号解析为数字数组，容忍预发布后缀（-INDEV / -RC1 等）与空段。
	 */
	private static int[] splitVersion(String version) {
		String[] parts = version.split("[-+]")[0].split("\\.");
		int[] numbers = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			String digits = parts[i].replaceAll("\\D", "");
			numbers[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
		}
		return numbers;
	}
}
