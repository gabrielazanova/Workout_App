package com.example.workoutapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    public ActivityAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class ActivityViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText;
        public TextView durationText;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.name_activity);
            durationText = itemView.findViewById(R.id.duration_activity);
        }
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.activities_table_view, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String name = mCursor.getString(mCursor.getColumnIndex(Activity.ActivityEntry.COLUMN_NAME));
        long duration = mCursor.getLong(mCursor.getColumnIndex(Activity.ActivityEntry.COLUMN_AMOUNT));
        long id = mCursor.getLong(mCursor.getColumnIndex(Activity.ActivityEntry._ID));

        holder.nameText.setText(name);
        holder.durationText.setText(formatDuration(duration));
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    private String formatDuration (long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;

        String formatted = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);

        return formatted;
    }
}
