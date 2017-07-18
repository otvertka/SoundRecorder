package com.example.soundrecorder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soundrecorder.R;
import com.example.soundrecorder.RecordingService;

import java.io.File;

public class RecordFragment extends Fragment {

    public static final String LOG_TAG = "myLogs";

    private Chronometer mChronometer = null;
    private TextView mRecordingPrompt;
    private int mRecordingPromptCount = 0;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;

    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;

    long timeWhenPaused = 0; //stores time when user clicks pause button

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) recordView.findViewById(R.id.chronometer);

        //update recording prompt text
        mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setRippleColor(getResources().getColor(R.color.colorPrimary)); //харит эта хирня, мб что старая версия?..
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });

        mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPauseRecord(mPauseRecording);
                mPauseRecording = !mPauseRecording;
            }
        });

        return recordView;
    }

    private void onRecord(boolean start) {

        Intent intent = new Intent(getActivity(), RecordingService.class);

        if(start){
            //start recording
            mRecordButton.setImageResource(R.mipmap.ic_stop_white_36dp);
            mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Recording started", Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()){
                folder.mkdir();
                Log.d(LOG_TAG, " FOLDER CREATED!!! " + folder.getAbsolutePath());
            }

            //start Chronometer
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordingPromptCount == 0) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (mRecordingPromptCount == 1) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (mRecordingPromptCount == 2) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        mRecordingPromptCount = -1;
                    }

                    mRecordingPromptCount++;
                }
            });

            //start RecordingService
            getActivity().startService(intent);
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
            mRecordingPromptCount++;

        } else {
            //stop recording
            mRecordButton.setImageResource(R.mipmap.ic_mic_white_36dp);
            mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime()); //это не должно стоять перед стопом???
            timeWhenPaused = 0;
            mRecordingPrompt.setText(R.string.record_prompt);

            getActivity().stopService(intent);
            //allow the screen to turn off once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onPauseRecord(boolean pause) {
        if (pause) {
            //pause recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_play_arrow_white_36dp, 0, 0 ,0);
            mRecordingPrompt.setText(getString(R.string.pause_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime(); //по возможности разобраться с этой хренью
            //Log.d(LOG_TAG, "time pause: " + timeWhenPaused );
            //Log.d(LOG_TAG, "mChronometer.getBase(): " + mChronometer.getBase()  );
            //Log.d(LOG_TAG, "SystemClock.elapsedRealtime(): " + SystemClock.elapsedRealtime()  );
            mChronometer.stop();
        } else {
            //resume recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_pause_white_36dp, 0, 0, 0);
            mRecordingPrompt.setText(getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }
}
