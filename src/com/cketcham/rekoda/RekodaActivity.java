
package com.cketcham.rekoda;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class RekodaActivity extends Activity {
	private static final String TAG = "Rekoda";

	MediaRecorder recorder;
	SurfaceHolder holder;
	// private WakeLock wl;

	private final BroadcastReceiver stop = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (stopRecorder())
				Toast.makeText(RekodaActivity.this, "stopped", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "stop");
		}
	};

	private final BroadcastReceiver start = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			startRecorder();
			Log.d(TAG, "start");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).mkdirs();
		//
		// PowerManager pm =
		// (PowerManager)getSystemService(Context.POWER_SERVICE);
		// wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		// wl.acquire();
		//
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.camera_layout);

		SurfaceView cameraView = (SurfaceView) findViewById(R.id.camera_view);
		holder = cameraView.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		registerReceiver(start, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(stop, new IntentFilter(Intent.ACTION_SCREEN_ON));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// wl.release();

		Log.d(TAG, "unregister");
		unregisterReceiver(start);
		unregisterReceiver(stop);
	}

	private void startRecorder() {
		if (recorder == null) {

			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

			CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			recorder.setProfile(cpHigh);

			recorder.setPreviewDisplay(holder.getSurface());
			recorder.setOutputFile(new File(Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "vid_"
					+ new Date().getTime() + ".3gp").getAbsolutePath());

			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				finish();
			} catch (IOException e) {
				e.printStackTrace();
				finish();
			}
		}
	}

	private boolean stopRecorder() {
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
			return true;
		}
		return false;
	}
}
