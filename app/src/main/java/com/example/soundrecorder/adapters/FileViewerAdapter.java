package com.example.soundrecorder.adapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.soundrecorder.DBHelper;
import com.example.soundrecorder.R;
import com.example.soundrecorder.RecordingItem;
import com.example.soundrecorder.fragments.PlaybackFragment;
import com.example.soundrecorder.listeners.OnDatabaseChangedListener;

import java.util.concurrent.TimeUnit;

import static com.example.soundrecorder.fragments.RecordFragment.LOG_TAG;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener{

    //private static final String LOG_TAG = "FileViewAdapter";

    private DBHelper dbHelper;

    private LinearLayoutManager llm;
    private Context mContext;

    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        dbHelper = new DBHelper(mContext);
        DBHelper.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // это просто скопипастил
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {
        RecordingItem item = getItem(position);
        long itemDuration = item.getmLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(item.getmName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
                DateUtils.formatDateTime(
                        mContext,
                        item.getmTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE |
                                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    PlaybackFragment playbackFragment =
                            new PlaybackFragment().newInstance(getItem(holder.getPosition()));

                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playbackFragment.show(transaction, "dialog_playback");

                } catch (Exception e){
                    Log.e(LOG_TAG, "exception", e);
                }
            }
        });
    }


    static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        TextView vName;
        TextView vLength;
        TextView vDateAdded;
        View cardView;

        RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    @Override
    public int getItemCount() {
        return dbHelper.getCount();
    }

    private RecordingItem getItem(int position){
        return dbHelper.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        // item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    public void onDatabaseEntryRenamed() {

    }
}
