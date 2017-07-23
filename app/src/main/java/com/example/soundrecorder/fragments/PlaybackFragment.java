package com.example.soundrecorder.fragments;

import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.soundrecorder.R;
import com.example.soundrecorder.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


public class PlaybackFragment extends DialogFragment {

    private static final String LOG_TAG = "myLogs";

    private static final String ARG_ITEM = "recording_item";
    private RecordingItem item;

    private MediaPlayer mMediaPlayer = null;

    private Handler mHandler = new Handler();

    private SeekBar mSeekBar = null;
    private FloatingActionButton mPlayButton = null;
    private TextView mCurrentProgressTextView = null;
    private TextView mFileNameTextView = null;
    private TextView mFileLengthTextView = null;

    // stores whether or not the mediaPlayer is currently playing audio
    private boolean isPlaying = false;

    long minutes = 0;
    long seconds = 0;

    public PlaybackFragment newInstance(RecordingItem item){

        PlaybackFragment f = new PlaybackFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ITEM, item);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(ARG_ITEM);

        if (item != null) {
            long itemDuration = item.getmLength();
            minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
            seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                    - TimeUnit.MINUTES.toSeconds(minutes);
        } else Log.d(LOG_TAG, "item == null!");

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_media_playback, null);

        mFileNameTextView = (TextView) view.findViewById(R.id.file_name_text_view);
        mFileLengthTextView = (TextView) view.findViewById(R.id.file_length_text_view);
        mCurrentProgressTextView = (TextView) view.findViewById(R.id.current_progress_text_view);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        ColorFilter filter = new LightingColorFilter
                (getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent));
        mSeekBar.getProgressDrawable().setColorFilter(filter);
        mSeekBar.getThumb().setColorFilter(filter);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes, seconds));
                    Log.d(LOG_TAG, "onProgressChanged()..   " +  progress);
                    updateSeekBar();
                } else if (mMediaPlayer == null && fromUser){
                    prepareMediaPlayerFromPointer(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null){
                    // remove message Handler from updating progress bar
                    Log.d(LOG_TAG, "onStartTrackingTouch()..   " );
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(LOG_TAG, "onStopTrackingTouch()..   " );

                if (mMediaPlayer != null){
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));
                    updateSeekBar();
                }
            }
        });

        mPlayButton = (FloatingActionButton) view.findViewById(R.id.fab_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(isPlaying);
                isPlaying = !isPlaying;

            }
        });

        mFileNameTextView.setText(item.getmName());
        mFileLengthTextView.setText(String.format("%02d:%02d", minutes,seconds));

        builder.setView(view);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()..   " );

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(LOG_TAG, "onStop()..   " );

        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(LOG_TAG, "onDestroy()..   " );


        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    // Play start/stop
    private void onPlay(boolean isPlaying) {
        if (!isPlaying){
            // currently mediaPlayer is not playing audio
            if (mMediaPlayer == null){
                startPlaying(); // start for beginning
            } else {
                resumePlaying(); // resume  the currently paused MediaPlayer
            }
        } else {
            // pause the mediaPlayer
            pausePlaying();
        }

    }

    private void startPlaying() {
        mPlayButton.setImageResource(R.mipmap.ic_pause_white_36dp);
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(item.getmFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });
        
        updateSeekBar();
        
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void prepareMediaPlayerFromPointer(int progress){
        // set mediaPlayer to start from middle of the audio file
        Log.d(LOG_TAG, "prepare()..");

        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(item.getmFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlaying();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        // keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying() {
        mPlayButton.setImageResource(R.mipmap.ic_play_arrow_white_36dp);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
    }


    private void resumePlaying() {
        Log.e(LOG_TAG, "resumePlaying().. ");
        mPlayButton.setImageResource(R.mipmap.ic_pause_white_36dp);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
    }

    private void stopPlaying() {
        Log.e(LOG_TAG, "stopPlaying().. ");
        mPlayButton.setImageResource(R.mipmap.ic_play_arrow_white_36dp);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mSeekBar.setProgress(mSeekBar.getMax());
        isPlaying = !isPlaying;

        mCurrentProgressTextView.setText(mFileLengthTextView.getText());
        //mSeekBar.setProgress(mSeekBar.getMax()); надо 2 раз??

        //allow the screen to turn off again once audio is finished playing
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //updating mSeekBar
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null){
                int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(mCurrentPosition);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes, seconds));
                Log.d(LOG_TAG, "run()..   " +  mCurrentPosition);

                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        Log.d(LOG_TAG, "update()..");
        mHandler.postDelayed(mRunnable, 1000);
    }

}
