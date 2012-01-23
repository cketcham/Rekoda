
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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RekodaActivity extends Activity {
	private static final String TAG = "Rekoda";

	/**
	 * No problems during recording
	 */
	private static final int STATUS_OK = 0;

	/**
	 * A problem occurred when trying to create the output file
	 */
	private static final int STATUS_FILE_ERROR = 1;

	/**
	 * A problem occurred when trying to start recording
	 */
	private static final int STATUS_START_ERROR = 2;

	/**
	 * The status of recording. Currently it could be {@link #STATUS_OK},
	 * {@link #STATUS_FILE_ERROR} or {@link #STATUS_START_ERROR}
	 */
	private int mStatus = STATUS_OK;

	MediaRecorder recorder;
	SurfaceHolder holder;
	private WakeLock wl;

	private final BroadcastReceiver stop = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String response = "unknown error";
			if(mStatus == STATUS_FILE_ERROR)
				response = "file error";
			else if(mStatus == STATUS_START_ERROR)
				response = "start error";
			else if (stopRecorder())
				response = "stopped";

			setStatus(STATUS_OK);
			Toast.makeText(RekodaActivity.this, response, Toast.LENGTH_SHORT).show();
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

		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wl.acquire();

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

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);

		Log.d(TAG, "unregister");
		unregisterReceiver(start);
		unregisterReceiver(stop);

		if(recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}

		wl.release();
	}

	private void startRecorder() {
		if (recorder == null) {

			File file = getOutputMediaFile();
			if (file == null) {
				setStatus(STATUS_FILE_ERROR);
				recorder = null;
				return;
			}

			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

			CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			recorder.setProfile(cpHigh);

			recorder.setPreviewDisplay(holder.getSurface());
			recorder.setOutputFile(file.getAbsolutePath());

			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				setStatus(STATUS_START_ERROR);
				finish();
			} catch (IOException e) {
				e.printStackTrace();
				setStatus(STATUS_START_ERROR);
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

	/** Create a File for saving video */
	private static File getOutputMediaFile(){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MOVIES), "Rekoda");

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("Rekoda", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return new File(mediaStorageDir.getPath() + File.separator +
				"VID_"+ timeStamp + ".mp4");
	}

	/**
	 * Sets the status for recording the video. Will vibrate for a short duration if status
	 * is an error
	 * @param status
	 */
	private void setStatus(int status) {
		if(mStatus == STATUS_OK && status != STATUS_OK) {
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(100);
		}
		mStatus = status;
	}
}
