package com.example.soundrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Дмитрий on 18.07.2017.
 */

public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";

    private String mFileName = null;
    private String mFilePath = null;

    private MediaRecorder mRecorder = null;

    //TODO:
    //private DBHelper mDatabase;

    private long mStartingTimeMillis = 0;
    private long mElapsedMilliis = 0;
    private int mElapsedSeconds = 0;
    //private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimeTask = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        //mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }


    private void startRecording() {
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(192000);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void setFileNameAndPath() {
        int count = 0;
        File f;

        do{
            count++;

            //mFileName = "My Recording" + "_" + (mDatabase.getCount() + count) + ".mp4";
            mFileName = "My Recording" + "_" + count + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }

    private void stopRecording() {
        mRecorder.stop();
        mElapsedMilliis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        Toast.makeText(this, "Recording saved to" + " " + mFilePath, Toast.LENGTH_SHORT).show();

        // remove notification
        // add here if block

        mRecorder = null;

        //TODO: add try block mDatabase.add ...

    }
}
