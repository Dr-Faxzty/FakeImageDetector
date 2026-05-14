package com.example.fakeimagedetector.ui;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fakeimagedetector.R;

public class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.ViewHolder> {
    private final Cursor cursor;

    public AnalysisAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_analysis, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String verdict = cursor.getString(cursor.getColumnIndexOrThrow("result_text"));
            double prob = cursor.getDouble(cursor.getColumnIndexOrThrow("probability"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

            holder.tvVerdict.setText(verdict);
            holder.tvProb.setText(String.format("%.1f%%", prob));
            holder.tvDate.setText(date);

            int color = holder.itemView.getContext().getColor(R.color.primary);
            if (verdict.contains("FAKE")) {
                color = holder.itemView.getContext().getColor(R.color.fake_red);
            } else if (verdict.contains("REALE")) {
                color = holder.itemView.getContext().getColor(R.color.real_green);
            }
            holder.tvVerdict.setTextColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVerdict, tvDate, tvProb;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVerdict = itemView.findViewById(R.id.tvHistoryVerdict);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvProb = itemView.findViewById(R.id.tvHistoryProb);
        }
    }
}