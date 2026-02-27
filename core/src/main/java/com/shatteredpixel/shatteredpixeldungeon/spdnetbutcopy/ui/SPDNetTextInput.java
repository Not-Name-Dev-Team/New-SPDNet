package com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.ui;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Point;

/**
 * SPDNet自定义TextInput组件（复制自com.watabou.noosa.TextInput）
 * 
 * 修改原因：原TextInput类在构造时自动获取焦点，导致多个输入框同时存在时产生焦点冲突
 * 
 * 修改内容：
 * 1. 移除构造函数中的自动获取焦点逻辑（原代码第151行：stage.setKeyboardFocus(textField)）
 * 2. 移除构造函数中的自动显示键盘逻辑（原代码第152行：Game.platform.setOnscreenKeyboardVisible(true, multiline)）
 * 3. 添加active成员变量用于跟踪激活状态
 * 4. 添加setActive(boolean active)方法用于手动控制焦点
 * 5. 添加isActive()方法用于查询当前激活状态
 * 6. 添加onActivated()和onDeactivated()回调方法
 * 7. 添加点击监听器，点击输入框时自动激活
 * 8. 修改setOnscreenKeyboard回调，仅在active时显示键盘
 * 
 * 影响：允许多个输入框共存，仅当前激活的输入框显示光标并接收输入
 * 
 * 原始文件：SPD-classes/src/main/java/com/watabou/noosa/TextInput.java
 */
public class SPDNetTextInput extends Component {

    private Stage stage;
    private Container container;
    private TextField textField;

    private Skin skin;

    private NinePatch bg;

    private boolean active = false;
    private boolean multiline;
    private int textSize;

    public SPDNetTextInput(NinePatch bg, boolean multiline, int size) {
        super();
        this.bg = bg;
        this.multiline = multiline;
        this.textSize = size;
        add(bg);

        Viewport viewport = new Viewport() {};
        viewport.setWorldSize(Game.width, Game.height);
        viewport.setScreenBounds(0, 0, Game.width, Game.height);
        viewport.setCamera(new OrthographicCamera());
        SpriteBatch.overrideVertexType = Mesh.VertexDataType.VertexArray;
        stage = new Stage(viewport);
        Game.inputHandler.addInputProcessor(stage);

        container = new Container<TextField>();
        stage.addActor(container);
        container.setTransform(true);

        skin = new Skin(FileUtils.getFileHandle(Files.FileType.Internal, "gdx/textfield.json"));

        TextField.TextFieldStyle style = skin.get(TextField.TextFieldStyle.class);
        style.font = Game.platform.getFont(size, "", false, false);
        style.background = null;
        if (multiline) {
            textField = new TextField("", style) {
                @Override
                public void cut() {
                    super.cut();
                    onClipBoardUpdate();
                }

                @Override
                public void copy() {
                    super.copy();
                    onClipBoardUpdate();
                }
            };
        } else {
            textField = new TextField("", style) {
                @Override
                public void cut() {
                    super.cut();
                    onClipBoardUpdate();
                }

                @Override
                public void copy() {
                    super.copy();
                    onClipBoardUpdate();
                }
            };
        }
        textField.setProgrammaticChangeEvents(true);

        if (!multiline) textField.setAlignment(Align.center);

        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                BitmapFont f = Game.platform.getFont(size, textField.getText(), false, false);
                TextField.TextFieldStyle style = textField.getStyle();
                if (f != style.font) {
                    style.font = f;
                    textField.setStyle(style);
                }
                onChanged();
            }
        });

        if (!multiline) {
            textField.setTextFieldListener(new TextField.TextFieldListener() {
                public void keyTyped(TextField textField, char c) {
                    if (c == '\r' || c == '\n') {
                        enterPressed();
                    }
                }
            });
        }

        textField.setOnscreenKeyboard(new TextField.OnscreenKeyboard() {
            @Override
            public void show(boolean visible) {
                // 修改：仅在active时显示键盘
                if (active) {
                    Game.platform.setOnscreenKeyboardVisible(visible, multiline);
                }
            }
        });

        container.setActor(textField);
        
        // 修改：添加点击监听器，点击时激活此输入框
        textField.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActive(true);
            }
        });
        
        // 修改：移除原代码中的自动获取焦点逻辑
        // 原代码：stage.setKeyboardFocus(textField);
        // 原代码：Game.platform.setOnscreenKeyboardVisible(true, multiline);
    }

    /**
     * 设置此输入框的激活状态
     * @param active true表示激活，false表示停用
     */
    public void setActive(boolean active) {
        if (this.active == active) return;
        
        this.active = active;
        
        if (active) {
            stage.setKeyboardFocus(textField);
            Game.platform.setOnscreenKeyboardVisible(true, multiline);
            onActivated();
        } else {
            if (stage.getKeyboardFocus() == textField) {
                stage.setKeyboardFocus(null);
            }
            Game.platform.setOnscreenKeyboardVisible(false, false);
            onDeactivated();
        }
    }

    /**
     * 获取当前激活状态
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 当输入框被激活时调用
     */
    public void onActivated() {
        // 子类可重写
    }

    /**
     * 当输入框被停用时调用
     */
    public void onDeactivated() {
        // 子类可重写
    }

    public void enterPressed() {
        // 子类可重写
    }

    public void onChanged() {
        // 子类可重写
    }

    public void onClipBoardUpdate() {
        // 子类可重写
    }

    public void setText(String text) {
        textField.setText(text);
        textField.setCursorPosition(textField.getText().length());
    }

    public void setMaxLength(int maxLength) {
        textField.setMaxLength(maxLength);
    }

    public String getText() {
        return textField.getText();
    }

    public void copyToClipboard() {
        if (textField.getSelection().isEmpty()) {
            textField.selectAll();
        }
        textField.copy();
    }

    public void pasteFromClipboard() {
        String contents = Gdx.app.getClipboard().getContents();
        if (contents == null) return;

        if (!textField.getSelection().isEmpty()) {
            textField.cut();
            Gdx.app.getClipboard().setContents(contents);
        }

        String existing = textField.getText();
        int cursorIdx = textField.getCursorPosition();

        textField.setText(existing.substring(0, cursorIdx) + contents + existing.substring(cursorIdx));
        textField.setCursorPosition(cursorIdx + contents.length());
    }

    @Override
    protected void layout() {
        super.layout();

        float contX = x;
        float contY = y;
        float contW = width;
        float contH = height;

        if (bg != null) {
            bg.x = x;
            bg.y = y;
            bg.size(width, height);

            contX += bg.marginLeft();
            contY += bg.marginTop();
            contW -= bg.marginHor();
            contH -= bg.marginVer();
        }

        float zoom = Camera.main.zoom;
        Camera c = camera();
        if (c != null) {
            zoom = c.zoom;
            Point p = c.cameraToScreen(contX, contY);
            contX = p.x / zoom;
            contY = p.y / zoom;
        }

        container.align(Align.topLeft);
        container.setPosition(contX * zoom, (Game.height - (contY * zoom)));
        container.size(contW * zoom, contH * zoom);
    }

    @Override
    public void update() {
        super.update();
        stage.act(Game.elapsed);
    }

    @Override
    public void draw() {
        super.draw();
        Quad.releaseIndices();
        Script.unuse();
        Texture.clear();
        stage.draw();
        Quad.bindIndices();
        Blending.useDefault();
    }

    @Override
    public synchronized void destroy() {
        super.destroy();
        if (stage != null) {
            stage.dispose();
            skin.dispose();
            Game.inputHandler.removeInputProcessor(stage);
            if (active) {
                Game.platform.setOnscreenKeyboardVisible(false, false);
            }
            if (!DeviceCompat.isDesktop()) Game.platform.updateSystemUI();
        }
    }
}
