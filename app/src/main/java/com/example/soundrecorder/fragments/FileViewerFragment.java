package com.example.soundrecorder.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;

import static com.example.soundrecorder.fragments.RecordFragment.LOG_TAG;

/**
 * Created by Дмитрий on 19.07.2017.
 */

public class FileViewerFragment extends Fragment {

    private static final String ARG_POSITION = "position";

   // private FileViewerAdapter mFileViewerAdapter;

    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
    }



    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/SoundRecorder") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if (event == FileObserver.DELETE){
                        // user deletes a recording file out of the app

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        // remove file from database and recyclerview
                        //mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
}
