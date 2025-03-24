package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.AvailableUpdateData;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.shatteredpixel.shatteredpixeldungeon.ui.HealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.Game;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PlatformSupport;

import java.io.File;

public class SPDNetChangesButton extends StyledButton {
	private static String updateProgress = "";
	private static float updateProgressValue = 0f;
	private static boolean downloadSuccess = false;

	protected static File file;

	public SPDNetChangesButton(Chrome.Type type, String label) {
		super(type, label);
		if (SPDSettings.updates()) Updates.checkForUpdate();
	}

	boolean updateShown = false;

	@Override
	public void update() {
		super.update();

		if (!updateShown && Updates.updateAvailable()) {
			updateShown = true;
			text(Messages.get(TitleScene.class, "update"));
		}

		if (updateShown) {
			textColor(ColorMath.interpolate(0xFFFFFF, Window.SHPX_COLOR, 0.5f + (float) Math.sin(Game.timeTotal * 5) / 2f));
		}
	}

	@Override
	protected void onClick() {
		if (Updates.updateAvailable()) {
			AvailableUpdateData update = Updates.updateData();
			ShatteredPixelDungeon.scene().addToFront(new WndUpdate(update));
		} else {
			ChangesScene.changesSelected = 0;
			ShatteredPixelDungeon.switchNoFade(ChangesScene.class);
		}
	}

	public static class WndUpdate extends Window {

		protected static final int WIDTH_P = 120;
		protected static final int WIDTH_L = 144;

		protected static final int MARGIN = 2;
		protected static final int BUTTON_HEIGHT = 18;

		private final PlatformSupport.UpdateCallback listener = new PlatformSupport.UpdateCallback() {
			@Override
			public void onDownloading(boolean isDownloading) {
			}

			@Override
			public void onStart(String url) {
				updateProgress = "开始下载";
			}

			@Override
			public void onProgress(long progress, long total, boolean isChanged) {
				updateProgressValue = (float) progress / total;
				updateProgress = String.format("%.2f", updateProgressValue * 100) + "%";
			}

			@Override
			public void onFinish(File file) {
				downloadSuccess = true;
				updateProgress = "下载完成";
				SPDNetChangesButton.file = file;
				Game.platform.install(file);
			}

			@Override
			public void onError(Exception e) {
				updateProgress = "下载失败";
			}

			@Override
			public void onCancel() {
				updateProgress = "下载失败";
			}
		};

		public WndUpdate(AvailableUpdateData update) {
			super();

			int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

			float pos = 0;
			IconTitle tfTitle = new IconTitle(Icons.get(Icons.CHANGES), update.versionName == null ? Messages.get(TitleScene.ChangesButton.class, "title") : Messages.get(TitleScene.ChangesButton.class, "versioned_title", update.versionName));
			tfTitle.setRect(0, pos, width, 0);
			add(tfTitle);

			pos = tfTitle.bottom();

			HealthBar downloadProgress = new HealthBar() {
				@Override
				public synchronized void update() {
					super.update();
					this.visible = !updateProgress.isEmpty();
					this.level(updateProgressValue);
				}
			};
			downloadProgress.setSize(width, 2);
			downloadProgress.setPos(0, pos + 2);
			add(downloadProgress);

			pos = downloadProgress.bottom() + 2 * MARGIN;

			layoutBody(pos, update.desc == null ? Messages.get(TitleScene.ChangesButton.class, "desc") : update.desc, update);
		}

		protected void layoutBody(float pos, String message, AvailableUpdateData update) {
			int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

			RenderedTextBlock tfMesage = PixelScene.renderTextBlock(6);
			tfMesage.text(message, width);
			tfMesage.setPos(0, pos);
			add(tfMesage);

			pos = tfMesage.bottom() + 2 * MARGIN;

			RedButton btn = new RedButton("从Github下载") {
				@Override
				protected void onClick() {
					if (!downloadSuccess) {
						Game.platform.updateGame(update.URL, listener);
					} else {
						Game.platform.install(file);
					}
				}

				@Override
				public void update() {
					if (!updateProgress.isEmpty()) {
						text(updateProgress);
					}
				}
			};
			btn.setRect(0, pos, width, BUTTON_HEIGHT);
			add(btn);
			pos += BUTTON_HEIGHT + MARGIN;
			RedButton btn2 = new RedButton("从Gitee下载") {
				@Override
				protected void onClick() {
					if (!downloadSuccess) {
						Game.platform.updateGame(update.giteeURL, listener);
					} else {
						Game.platform.install(file);
					}
				}

				@Override
				public void update() {
					if (!updateProgress.isEmpty()) {
						text(updateProgress);
					}
				}
			};
			btn2.setRect(0, pos, width, BUTTON_HEIGHT);
			add(btn2);

			pos += BUTTON_HEIGHT + MARGIN;


			resize(width, (int) (pos - MARGIN));
		}
	}
}