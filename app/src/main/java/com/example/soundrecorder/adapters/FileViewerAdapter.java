package com.example.soundrecorder.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soundrecorder.DBHelper;
import com.example.soundrecorder.R;
import com.example.soundrecorder.RecordingItem;
import com.example.soundrecorder.fragments.PlaybackFragment;
import com.example.soundrecorder.listeners.OnDatabaseChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.soundrecorder.fragments.RecordFragment.LOG_TAG;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener{

    private static final String LOG_TAG = "FileViewAdapter";

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

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                ArrayList<String> entry = new ArrayList<>();
                entry.add("Share file");
                entry.add("Rename file");
                entry.add("Delete file");

                final CharSequence[] items = entry.toArray(new CharSequence[entry.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Options");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int item) {
                        if  (item == 0){
                            shareFileDialog(holder.getPosition());
                        } if (item == 1){
                            renameFileDialog(holder.getPosition());
                        } else if (item == 2){
                            deleteFileDialog(holder.getPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return false;
            }
        });

    }

    public void remove(int position) {
        //remove item from database, recycleView and storage

        //delete file from storage

        File file = new File(getItem(position).getmFilePath());
        file.delete();

        Toast.makeText(
                mContext,
                String.format("%1$s successfully deleted",
                        getItem(position).getmName()
                ),
                Toast.LENGTH_SHORT
        ).show();

        dbHelper.removeItemWithID(getItem(position).getmId());
        notifyItemRemoved(position);
    }

    private void renameFileDialog(final int position) {
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle("Rename file");
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                try {
                    String value = input.getText().toString().trim() + ".mp4";
                    rename(position, value);
                } catch (Exception e){
                    Log.e(LOG_TAG, "exception", e);
                }

                dialogInterface.cancel();
            }
        });
        renameFileBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    private void rename(int position, String name) {
        // rename a file

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) { // не понял 2 часть условия
            // file name is not unique, cannot rename file
            Toast.makeText(mContext,
                    String.format("The fie %1&s already exists. Please choose a different...", name),
                    Toast.LENGTH_SHORT).show();
        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getmFilePath());
            oldFilePath.renameTo(f);
            dbHelper.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    private void shareFileDialog(int position) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getmFilePath())));
        intent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(intent, "Send to"));
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

    private void deleteFileDialog(final int position) {
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle("Confirm Delete...");
        confirmDelete.setMessage("Are you sure you would like to delete this file?");
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                try {
                    //remove item from database, recycleView and storage
                    remove(position);
                } catch (Exception e){
                    Log.e(LOG_TAG, "exception", e);
                }

                dialogInterface.cancel();
            }
        });

        confirmDelete.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }
}
