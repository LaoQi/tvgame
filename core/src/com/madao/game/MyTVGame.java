package com.madao.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Pixmap;

import com.badlogic.gdx.audio.AudioDevice;
import com.madao.gnes.Gnes;
import com.madao.gnes.IAudioDevice;
import com.madao.gnes.IRender;

public class MyTVGame extends ApplicationAdapter {
	SpriteBatch batch;
	OrthographicCamera mainCamera;
	Texture img;

	private AudioDevice audioDevice1;
	private AudioDevice audioDevice2;

	private Pixmap frameBuffer;

	private Gnes gnes;

	@Override
	public void create () {
		batch = new SpriteBatch();
		FileHandle fileHandle = Gdx.files.internal("mario.nes");

		mainCamera = new OrthographicCamera(256, 240);
		mainCamera.position.set(256/2, 240/2, 0);   // Set camera position to the corner
		mainCamera.update();
		batch.setProjectionMatrix(mainCamera.combined);

		frameBuffer = new Pixmap(256, 240, Pixmap.Format.RGBA8888);
		img = new Texture(frameBuffer);
		audioDevice1 = Gdx.audio.newAudioDevice(44100, true);
		audioDevice2 = Gdx.audio.newAudioDevice(44100, true);
		gnes = new Gnes(fileHandle.readBytes(), new IAudioDevice() {
			@Override
			public void writeSamples1(float[] samples, int offset, int numSamples) {
				audioDevice1.writeSamples(samples, offset, numSamples);
			}

			@Override
			public void writeSamples2(float[] samples, int offset, int numSamples) {
				audioDevice2.writeSamples(samples, offset, numSamples);
			}
		}, new IRender() {
			@Override
			public void setPixel(int x, int y, int rgba8888color) {
				frameBuffer.drawPixel(x, y, rgba8888color);
			}
		});
	}

	@Override
	public void render () {
		gnes.tick();
		img.draw(frameBuffer, 0, 0);
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
