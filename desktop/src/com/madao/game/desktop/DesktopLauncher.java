package com.madao.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.madao.game.MyTVGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		new LwjglApplication(new MyTVGame(), config);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//config.width = 256;
		//config.height = 240;
		config.width = 256*2;
		config.height = 240*2;

		// Start LibGDX Program
		new LwjglApplication(new MyTVGame(), config);
	}
}
