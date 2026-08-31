package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.BlueButton;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.NetNoteTarget;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CNoteCreate;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.ui.SPDNetTextInput;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

/**
 * SPDNet: 地牢留言(Ping)系统 - 发送留言窗。
 * 见 test/ping-design.md §2 / 阶段C.3：
 * 上部对象图标 + 名字预览，下部输入框(短) + 发送/取消；发送走 Sender.sendNote，
 * 取消直接关闭回到游戏（不还原聊天窗）。
 */
public class NetWndLeaveNote extends NetWindow {

	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 16;
	private static final int INPUT_MAX_LENGTH = 50;

	public static final float WIDTH_P = 120;
	public static final float WIDTH_L = 160;

	private final NetNoteTarget target;

	// SPDNet: 持有 Body 引用，供 send() 读取真正的输入框（Body.input）
	private Body body;

	public NetWndLeaveNote(NetNoteTarget target) {
		super();
		this.target = target;

		int width, height;
		if (PixelScene.landscape()) width = (int) WIDTH_L;
		else width = (int) WIDTH_P;

		height = 0;

		body = new Body(width);
		// SPDNet: 先 add 到窗口再排版，保证 layout() 时 input 能解析到窗口 camera，
		// 否则 SPDNetTextInput 因 camera()==null 跳过定位，导致光标不显示、点击无法聚焦
		add(body);
		body.setSize(width, height);
		body.setPos(0, 1);

		// SPDNet: 与登录窗一致，打开即让输入框取得焦点：显示光标并接收键盘输入（否则无法输入）
		body.input.setActive(true);

		height += body.bottom();

		resize(width, height);

		// SPDNet: resize() 才把窗口 camera 的 x/y/width/height 校准到位，而 SPDNetTextInput.layout()
		// 依赖 camera() 换算 scene2d 屏幕坐标。首次 Body.layout() 发生在 resize() 之前(相机未就绪)，
		// 文字会漂到窗口外右下；这里在 resize 后重新触发 Body 布局，重跑 input.setRect 使其按就绪的
		// 相机正确定位（与上游 WndTextInput / NetWndLogin 在 resize 之后再 setRect 的写法一致）。
		body.setRect(0, 1, body.width(), body.height());
	}

	private void send() {
		String msg = body.input.getText();
		// SPDNet: 客户端只负责发起，服务端权威校验；PLAYER 不留 snapshot，由服务端主动索取快照。
		Sender.sendNote(new CNoteCreate(target.pos, target.noteType, msg, target.targetName, target.snapshot));
		hide();
	}

	public class Body extends Component {
		IconTitle title;
		RenderedTextBlock hint;
		SPDNetTextInput input;
		BlueButton send;
		BlueButton cancel;
		// SPDNet: 覆盖在输入框上的原生点击区，用于可靠激活输入焦点（等于登录窗做法）
		PointerArea inputClickArea;

		Body(int width) {
			super();
			// width 由本 Body 的宽度决定
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			// 上部：对象图标 + 名字预览
			title = new IconTitle(target.icon, target.name);
			add(title);

			hint = PixelScene.renderTextBlock(6);
			hint.text("给" + target.name + " 留言");
			hint.hardlight(Color.WHITE.toIntBits());
			add(hint);

			// 下部：短输入框（字号需乘 uiCamera.zoom；注意先乘后取整，避免 zoom 小数被先行截断
			// 导致字号与输入框(按真实 zoom 缩放)尺寸不匹配、文字不对齐）
			int textSize = (int) (PixelScene.uiCamera.zoom * 6);
			input = new SPDNetTextInput(Chrome.get(Chrome.Type.TOAST_WHITE), false, textSize);
			input.setMaxLength(INPUT_MAX_LENGTH);
			input.setTextAlignment(Align.left);
			// SPDNet: 输入框白底(TOAST_WHITE)，文字用默认黑色，与登录框一致
			// 回车即发送
			input.addlistener(new InputListener() {
				@Override
				public boolean keyDown(InputEvent event, int keycode) {
					if (keycode == Input.Keys.ENTER) {
						send();
						return true;
					}
					return super.keyDown(event, keycode);
				}
			});
			add(input);

			// SPDNet: 在输入框上叠加原生 PointerArea，点击时激活焦点（与登录窗一致，
			// 不依赖 scene2d 自身的点击命中，后者在窗口坐标下不可靠）
			inputClickArea = new PointerArea(0, 0, 0, 0) {
				@Override
				protected void onClick(PointerEvent event) {
					input.setActive(true);
				}
			};
			add(inputClickArea);

			send = new BlueButton("发送") {
				@Override
				protected void onPointerDown() {
					super.onPointerDown();
					PointerEvent.clearKeyboardThisPress = false;
				}

				@Override
				protected void onPointerUp() {
					super.onPointerUp();
					PointerEvent.clearKeyboardThisPress = false;
				}

				@Override
				protected void onClick() {
					send();
				}
			};
			add(send);

			cancel = new BlueButton("取消") {
				@Override
				protected void onPointerDown() {
					super.onPointerDown();
					PointerEvent.clearKeyboardThisPress = false;
				}

				@Override
				protected void onPointerUp() {
					super.onPointerUp();
					PointerEvent.clearKeyboardThisPress = false;
				}

				@Override
				protected void onClick() {
					hide();
				}
			};
			add(cancel);
		}

		@Override
		protected void layout() {
			super.layout();

			title.setRect(0, 0, width, 0);
			float pos = title.bottom() + MARGIN;

			hint.setRect(0, pos, width, 0);
			hint.maxWidth((int) width);
			pos = hint.bottom() + MARGIN;

			input.setRect(0, pos, width, BUTTON_HEIGHT);
			// 让点击区与输入框同区域
			inputClickArea.x = 0;
			inputClickArea.y = pos;
			inputClickArea.width = width;
			inputClickArea.height = BUTTON_HEIGHT;
			pos = input.bottom() + MARGIN;

			send.setRect(0, pos, width / 2 - MARGIN, BUTTON_HEIGHT);
			cancel.setRect(send.right() + MARGIN * 2, pos, width / 2 - MARGIN, BUTTON_HEIGHT);
			pos = cancel.bottom();

			// 设置 Body 自身高度，供外层构造器 height += body.bottom() 计算正确窗口高度
			height = pos;
		}
	}
}