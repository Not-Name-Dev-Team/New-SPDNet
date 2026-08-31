package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import static com.watabou.utils.DeviceCompat.isDebug;
import static com.watabou.utils.DeviceCompat.isDesktop;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.BlueButton;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.NetIcons;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.utils.DeviceCompat;

public class NetWndServerInfo extends NetWindow {
	private static final int WIDTH_P = 122;
	private static final int WIDTH_L = 223;
	private static final int BTN_HEIGHT = 18;

	private static final float GAP = 2;

	IconTitle title;
	RenderedTextBlock host;
	RenderedTextBlock status;
	BlueButton keyBtn;
	BlueButton connectBtn;

	NetWndServerInfo self = this;

	public NetWndServerInfo() {
		super();

		// 初始化socket客户端以正确显示服务器地址
		Net.getSocket();

		int height, y = 0;

		int maxWidth = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		title = new IconTitle(NetIcons.get(NetIcons.GLOBE), Messages.get(this, "server_connection"));
		title.setRect(0, 0, maxWidth, 20);
		add(title);

		float bottom = y;
		bottom = title.bottom();


		if (isDesktop() && isDebug()) {
			host = PixelScene.renderTextBlock("服务器地址(调试)" + "\n" + Net.getServerUrl(), 7);
		} else {
			host = PixelScene.renderTextBlock(Messages.get(this, "server_address")+"\n" + Net.getServerUrl(), 9);
		}
		host.maxWidth(maxWidth);
		host.setPos(0, bottom + GAP);
		add(host);

		bottom = host.bottom() + GAP;

		// SPDNet: 复现 renderTextBlock(9) 的归一化(含 realPixelX 背缓冲修正 + 取整)，
		// 替代原手写的 9*zoom/zoom(1/zoom)，保证状态文字与原版各档缩放完全一致、不再偏大。
		// 因需要匿名 update() 重写 state，不能直接用 renderTextBlock(方法调用无法加匿名子类)，
		// 故手动构造 RenderedTextBlock 并按同样规则缩放。
		float realScale = DeviceCompat.getRealPixelScaleX();
		// 纹理像素尺寸 = 9 * K，但缩放分母是 K(不是 9*K)：纹理按 K 放大后需用 1/K 缩放回逻辑 9，
		// 与 renderTextBlock() 的归一化规则完全一致
		int scaleMult = Math.round(PixelScene.defaultZoom * realScale);
		int statusPx = 9 * scaleMult;
		status = new RenderedTextBlock(Net.isConnected() ? Messages.get(this, "connected") : Messages.get(this, "disconnected"), statusPx) {
			@Override
			public synchronized void update() {
				super.update();
				text(Net.isConnected() ? Messages.get(NetWndServerInfo.class, "connected") : Messages.get(NetWndServerInfo.class, "disconnected"));
				hardlight(Net.isConnected() ? 0x00FF00 : 0xFF0000);
			}
		};

		status.zoom(1 / (float) scaleMult);
		status.setRect(0, bottom + GAP, maxWidth, 20);
		add(status);

		bottom = status.bottom() + (GAP * 3);

		keyBtn = new BlueButton(Messages.get(this, "set_credentials")) {
			@Override
			protected void onClick() {
				NetWindow.showLogin();
			}
		};
		add(keyBtn);
		keyBtn.setSize(maxWidth / 2, BTN_HEIGHT);
		keyBtn.setPos(0, bottom);

		float finalBottom = bottom;
		connectBtn = new BlueButton(Messages.get(NetWndServerInfo.class, "connect")) {
			@Override
			public synchronized void update() {
				super.update();
				text.text(Net.isConnected() ? Messages.get(NetWndServerInfo.class, "disconnect") : Messages.get(NetWndServerInfo.class, "connect"));
				connectBtn.setRect(keyBtn.right(), finalBottom, maxWidth / 2, BTN_HEIGHT);
			}

			@Override
			protected void onClick() {
				super.onClick();
				Net.toggleSocket();
			}
		};
		add(connectBtn);
		connectBtn.setSize(maxWidth / 2, BTN_HEIGHT);
		connectBtn.setPos(keyBtn.right(), bottom);

		height = (int) (connectBtn.bottom() + GAP / 2);

		resize(maxWidth, height);
	}
}