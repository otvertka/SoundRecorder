package com.example.soundrecorder.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.soundrecorder.RecordingItem;


public class PlaybackFragment extends DialogFragment {


    private static final String ARG_ITEM = "recording_item";

    public PlaybackFragment newInstanse(RecordingItem item){

        PlaybackFragment f = new PlaybackFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ITEM, item);
        f.setArguments(b);

        return f;
    }
}
