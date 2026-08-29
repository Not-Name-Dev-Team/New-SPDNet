/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AsynchronousAndroidAudio;
import com.badlogic.gdx.backends.android.DefaultAndroidInput;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.update.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

public class AndroidLauncher extends AndroidApplication {
	
	public static AndroidApplication instance;
	
	private static AndroidPlatformSupport support;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			GdxNativesLoader.load();
			FreeType.initFreeType();
		} catch (Exception e){
			GdxNativesLoader.disableNativesLoading = true;
			AndroidMissingNativesHandler.error = e;
			Intent intent = new Intent(this, AndroidMissingNativesHandler.class);
			startActivity(intent);
			finish();
			//let initialization continue for a moment so that we can set up things libGDX expects to be set up
		}

		//there are some things we only need to set up on first launch
		if (instance == null) {

			instance = this;

			// 设置Net版本号
			try {
				Game.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				Game.netVersion = Game.version.split("\\}")[1];
				Game.version = Game.version.split("\\}")[0];
			} catch (PackageManager.NameNotFoundException e) {
				Game.netVersion = "???";
				Game.version = "???";
			}
			try {
				Game.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				Game.versionCode = 0;
			}

			Gdx.app = this;
			if (UpdateImpl.supportsUpdates()) {
				Updates.service = UpdateImpl.getUpdateService();
			}
			if (NewsImpl.supportsNews()) {
				News.service = NewsImpl.getNewsService();
			}

			FileUtils.setDefaultFileProperties(Files.FileType.Local, "");

			// grab preferences directly using our instance first
			// so that we don't need to rely on Gdx.app, which isn't initialized yet.
			// Note that we use a different prefs name on android for legacy purposes,
			// this is the default prefs filename given to an android app (.xml is automatically added to it)
			SPDSettings.set(instance.getPreferences("ShatteredPixelDungeon"));

		} else {
			instance = this;
		}

		//Shattered still overrides the back gesture behaviour, but we need to do it in a new way
		// (API added in Android 13, functionality enforced in Android 16)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			//we post this to a runnable so that it's delayed and overrides
			// default GDX back handling, which only sends a key down event
			runnables.add(new Runnable() {
				@Override
				public void run() {
					getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, new OnBackInvokedCallback() {
						@Override
						public void onBackInvoked() {
							KeyEvent.addKeyEvent(new KeyEvent(Input.Keys.BACK, true));
							KeyEvent.addKeyEvent(new KeyEvent(Input.Keys.BACK, false));
						}
					});
				}
			});
		}

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.depth = 0;

		//we manage this ourselves
		config.useImmersiveMode = false;
		
		config.useCompass = false;
		config.useAccelerometer = false;
		
		if (support == null) support = new AndroidPlatformSupport();
		else                 support.reloadGenerators();
		
		support.updateSystemUI();

		Button.longClick = ViewConfiguration.getLongPressTimeout()/1000f;
		
		initialize(new ShatteredPixelDungeon(support), config);
		
	}

	@Override
	public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
		return new AsynchronousAndroidAudio(context, config);
	}

	// SPDNet: 修复 libGDX 1.13+ 安全键盘问题导致的录屏/截屏被系统拦截（部分机型）
	// 参考: https://github.com/libgdx/libgdx/issues/7754 （TheoTown 作者 LobbyDivinus 提供的临时方案）
	// 原理: GLSurfaceView20#onCreateInputConnection 中通过 DefaultAndroidInput.getAndroidInputType(onscreenKeyboardType, true)
	//       构造 inputType，其中包含 InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD 标志。
	//       该标志会被部分厂商（如 VIVO）的安全机制识别为"密码输入窗口"，从而禁止在该窗口上录屏/截屏。
	//       这里在默认软键盘类型下改用不含该标志的 inputType，规避系统拦截。
	@Override
	protected AndroidGraphics createGraphics(AndroidApplicationConfiguration config) {
		return new AndroidGraphics(this, config,
				config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy) {
			@Override
			protected GLSurfaceView20 createGLSurfaceView(AndroidApplicationBase application, ResolutionStrategy resolutionStrategy) {
				if (!checkGL20()) throw new GdxRuntimeException("libGDX requires OpenGL ES 2.0");

				GLSurfaceView.EGLConfigChooser configChooser = getEglConfigChooser();
				GLSurfaceView20 view = new GLSurfaceView20(application.getContext(), resolutionStrategy, config.useGL30 ? 3 : 2) {
					@Override
					public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
						if (outAttrs != null) {
							outAttrs.imeOptions = outAttrs.imeOptions | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
							if (onscreenKeyboardType == Input.OnscreenKeyboardType.Default) {
								// SPDNet: 关键——此处省略 InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD 标志，
								// 避免被系统判定为安全输入窗口而禁止录屏/截屏
								outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
							} else {
								outAttrs.inputType = DefaultAndroidInput.getAndroidInputType(onscreenKeyboardType, true);
							}
						}
						// 传入 null 以避免 outAttrs 被再次叠加修改、标志被写回
						return super.onCreateInputConnection(null);
					}
				};

				if (configChooser != null)
					view.setEGLConfigChooser(configChooser);
				else
					view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);

				view.setRenderer(this);
				return view;
			}
		};
	}

	@Override
	protected void onResume() {
		//prevents weird rare cases where the app is running twice
		if (instance != this){
			finishAndRemoveTask();
		}
		super.onResume();
	}

	@SuppressLint("GestureBackNavigation")
	@Override
	public void onBackPressed() {
		//do nothing, game should catch all back presses
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		support.updateSystemUI();
	}
	
	@Override
	public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
		super.onMultiWindowModeChanged(isInMultiWindowMode);
		support.updateSystemUI();
	}
}