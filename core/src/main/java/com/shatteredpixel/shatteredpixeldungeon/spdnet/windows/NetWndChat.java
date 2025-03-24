package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.BlueButton;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.NLog;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CChatMessage;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.Camera;
import com.watabou.noosa.TextInput;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

public class NetWndChat extends NetWindow {

	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 16;

	public static final float WIDTH_P = 120;
	public static final float WIDTH_L = 300;

	public static final float MSGPADDING = 2;

	private TextInput textInput;
	private BlueButton sendBtn;

	private ScrollPane list;
	private Component content;

	private Chat chat;
	private Camera cam = camera();
	private String lastMessage;
	private int currentMessages = 0;

	public NetWndChat() {
		super();
		int width, height;
		boolean isMobile = DeviceCompat.isAndroid() || DeviceCompat.isiOS();

		if (PixelScene.landscape()) width = (int) WIDTH_L;
		else width = (int) WIDTH_P;

		height = 0;

		int yOffset = isMobile ? -BUTTON_HEIGHT : 0;
		camera.y -= this.yOffset * camera.zoom;
		this.yOffset = yOffset;
		camera.y += yOffset * camera.zoom;

		shadow.boxRect(camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height);

		chat = new Chat();
		chat.setSize(width, height);
		chat.setPos(0, 1);
		add(chat);

		height += chat.bottom();

		resize(width, height);

		textInput.setRect(0, sendBtn.top(), width - sendBtn.width(), sendBtn.height());
	}

	public class Chat extends Component {
		public static final float BOXHEIGHT = 100;

		private float lastMessagePos;

		public Chat() {
			super();
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			int textSize = (int) PixelScene.uiCamera.zoom * 6;
			textInput = new TextInput(Chrome.get(Chrome.Type.TOAST), false, textSize);
			textInput.setMaxLength(200);
			textInput.setTextAlignment(Align.left);
			textInput.setTextColor(Color.WHITE);
			add(textInput);

			textInput.addlistener(new InputListener() {
				@Override
				public boolean keyDown(InputEvent event, int keycode) {
					if (keycode == Input.Keys.ENTER) {
						Sender.sendChatMessage(new CChatMessage(textInput.getText()));
						textInput.setText("");
					}
					return super.keyDown(event, keycode);
				}
			});

			list = new ScrollPane(new Component());
			list.camera = cam;
			add(list);

			content = list.content();
			content.clear();

			sendBtn = new BlueButton("发送") {
				@Override
				protected void onClick() {
					String msg = textInput.getText();
					if (!msg.equals(lastMessage)
							&& !msg.isEmpty()) {
						Sender.sendChatMessage(new CChatMessage(msg));
						lastMessage = msg;
						textInput.setText("");
					}
					Gdx.input.setOnscreenKeyboardVisible(true);
				}
			};

			add(sendBtn);
		}

		@Override
		protected void layout() {
			super.layout();

			content.setRect(0, y, width, BOXHEIGHT);
			list.setRect(0, y, width, BOXHEIGHT);

			list.scrollTo(0, 0);

			height = list.bottom() + (MARGIN * 3);

			sendBtn.setPos(width - sendBtn.width() - 1, height);
			sendBtn.setSize(20, BUTTON_HEIGHT);

			height = sendBtn.bottom();

			resize((int) width, (int) height);
		}
	}

	@Override
	public synchronized void update() {
		super.update();
		if (currentMessages < Net.chatMessages.size()) {
			for (int i = currentMessages; i < Net.chatMessages.size(); i++) {
				addChatMessage(Net.chatMessages.get(i));
			}
		}
	}

	private void addChatMessage(String message) {
		int color = CharSprite.DEFAULT;
		if (message.startsWith(NLog.POSITIVE)) {
			message = message.substring(NLog.POSITIVE.length());
			color = CharSprite.POSITIVE;
		} else if (message.startsWith(NLog.NEGATIVE)) {
			message = message.substring(NLog.NEGATIVE.length());
			color = CharSprite.NEGATIVE;
		} else if (message.startsWith(NLog.WARNING)) {
			message = message.substring(NLog.WARNING.length());
			color = CharSprite.WARNING;
		} else if (message.startsWith(NLog.HIGHLIGHT)) {
			message = message.substring(NLog.HIGHLIGHT.length());
			color = CharSprite.NEUTRAL;
		}

		RenderedTextBlock renderedTextBlock = PixelScene.renderTextBlock(6);
		renderedTextBlock.text(message, width);
		renderedTextBlock.hardlight(color);

		if (chat.lastMessagePos == 2) renderedTextBlock.setRect(0, 0, width, 7);
		else renderedTextBlock.setRect(0, chat.lastMessagePos + MSGPADDING, width, 7);

		chat.lastMessagePos = renderedTextBlock.bottom() + 2;

		if (renderedTextBlock.bottom() >= content.bottom())
			content.setSize(content.width(), content.height() + renderedTextBlock.height() + MSGPADDING);

		content.add(renderedTextBlock);

		if (content.bottom() > list.height())
			list.scrollTo(0, renderedTextBlock.bottom() - list.bottom());

		currentMessages++;
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
