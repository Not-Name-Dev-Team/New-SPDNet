package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetSettings;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.BlueButton;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.ui.SPDNetTextInput;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.PointerArea;

/**
 * SPDNet登录凭据设置窗口
 * 
 * 使用SPDNetTextInput替代原有TextInput，解决多输入框焦点冲突问题
 */
public class NetWndLogin extends NetWindow {

    private static final int WIDTH = 135;
    private static final int MARGIN = 2;
    private static final int BUTTON_HEIGHT = 16;
    private static final int INPUT_HEIGHT = 16;

    private SPDNetTextInput nameInput;
    private SPDNetTextInput passwordInput;

    public NetWndLogin() {
        super();

        if (PixelScene.landscape()) {
            offset(0, -45);
        } else {
            offset(0, -45);
        }

        float pos = MARGIN;

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.maxWidth(WIDTH);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH - title.width()) / 2, pos);
        add(title);
        pos = title.bottom() + MARGIN * 2;

        RenderedTextBlock nameLabel = PixelScene.renderTextBlock(Messages.get(this, "username"), 7);
        nameLabel.maxWidth(WIDTH);
        nameLabel.setPos(MARGIN, pos);
        add(nameLabel);
        pos = nameLabel.bottom() + MARGIN;

        int textSize = (int) PixelScene.uiCamera.zoom * 9;
        float nameInputTop = pos;

        nameInput = new SPDNetTextInput(Chrome.get(Chrome.Type.TOAST_WHITE), false, textSize) {
            @Override
            public void enterPressed() {
                passwordInput.setActive(true);
            }

            @Override
            public void onActivated() {
                if (passwordInput != null) {
                    passwordInput.setActive(false);
                }
            }
        };
        nameInput.setText(NetSettings.getName());
        nameInput.setMaxLength(30);
        add(nameInput);

        PointerArea nameClickArea = new PointerArea(0, 0, 0, 0) {
            @Override
            protected void onClick(PointerEvent event) {
                nameInput.setActive(true);
            }
        };
        add(nameClickArea);

        pos += INPUT_HEIGHT + MARGIN * 2;

        RenderedTextBlock passwordLabel = PixelScene.renderTextBlock(Messages.get(this, "password"), 7);
        passwordLabel.maxWidth(WIDTH);
        passwordLabel.setPos(MARGIN, pos);
        add(passwordLabel);
        pos = passwordLabel.bottom() + MARGIN;

        float passwordInputTop = pos;

        passwordInput = new SPDNetTextInput(Chrome.get(Chrome.Type.TOAST_WHITE), false, textSize) {
            @Override
            public void enterPressed() {
                saveCredentials();
            }

            @Override
            public void onActivated() {
                if (nameInput != null) {
                    nameInput.setActive(false);
                }
            }
        };
        passwordInput.setText(NetSettings.getPassword());
        passwordInput.setMaxLength(100);
        add(passwordInput);

        PointerArea passwordClickArea = new PointerArea(0, 0, 0, 0) {
            @Override
            protected void onClick(PointerEvent event) {
                passwordInput.setActive(true);
            }
        };
        add(passwordClickArea);

        pos += INPUT_HEIGHT + MARGIN * 2;

        int btnWidth = (WIDTH - MARGIN * 4) / 3;

        BlueButton confirmBtn = new BlueButton(Messages.get(this, "confirm")) {
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
                saveCredentials();
            }
        };
        add(confirmBtn);
        confirmBtn.setRect(MARGIN, pos, btnWidth, BUTTON_HEIGHT);

        BlueButton cancelBtn = new BlueButton(Messages.get(this, "cancel")) {
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
        add(cancelBtn);
        cancelBtn.setRect(confirmBtn.right() + MARGIN, pos, btnWidth, BUTTON_HEIGHT);

        BlueButton registerBtn = new BlueButton(Messages.get(this, "register")) {
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
                Game.platform.openURI(Net.webUrl);
            }
        };
        add(registerBtn);
        registerBtn.setRect(cancelBtn.right() + MARGIN, pos, btnWidth, BUTTON_HEIGHT);

        pos += BUTTON_HEIGHT + MARGIN;

        resize(WIDTH, (int) pos);

        float inputWidth = WIDTH - MARGIN * 2;
        nameInput.setRect(MARGIN, nameInputTop, inputWidth, INPUT_HEIGHT);
        nameClickArea.x = MARGIN;
        nameClickArea.y = nameInputTop;
        nameClickArea.width = inputWidth;
        nameClickArea.height = INPUT_HEIGHT;
        
        passwordInput.setRect(MARGIN, passwordInputTop, inputWidth, INPUT_HEIGHT);
        passwordClickArea.x = MARGIN;
        passwordClickArea.y = passwordInputTop;
        passwordClickArea.width = inputWidth;
        passwordClickArea.height = INPUT_HEIGHT;

        nameInput.setActive(true);

        PointerEvent.clearKeyboardThisPress = false;
    }

    private void saveCredentials() {
        String name = nameInput.getText().trim();
        String password = passwordInput.getText();

        if (name.isEmpty()) {
            NetWindow.error(Messages.get(this, "error"), Messages.get(this, "username_empty"));
            return;
        }
        if (password.isEmpty()) {
            NetWindow.error(Messages.get(this, "error"), Messages.get(this, "password_empty"));
            return;
        }

        NetSettings.setName(name);
        NetSettings.setPassword(password);
        Net.destroySocket();
        hide();
        NetWindow.info(Messages.get(this, "credentials_saved"));
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void offset(int xOffset, int yOffset) {
        super.offset(xOffset, yOffset);
        if (nameInput != null) {
            nameInput.setRect(nameInput.left(), nameInput.top(), nameInput.width(), nameInput.height());
        }
        if (passwordInput != null) {
            passwordInput.setRect(passwordInput.left(), passwordInput.top(), passwordInput.width(), passwordInput.height());
        }
    }
}
