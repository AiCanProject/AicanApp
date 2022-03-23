package com.aican.aicanapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.phData;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    Context context;
    List<phData> logs_list;

    public LogAdapter(Context context, List<phData> logs_list) {
        this.context = context;
        this.logs_list = logs_list;
    }

    @NonNull
    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_log, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull LogAdapter.ViewHolder holder, int position) {
        if(logs_list != null && logs_list.size() > 0){
            holder.ph.setText(logs_list.get(position).getpH());
            holder.mv.setText(logs_list.get(position).getmV());
            holder.dt.setText(logs_list.get(position).getDate());
        }
        else{
            return;
        }
    }

    @Override
    public int getItemCount() {
        return logs_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ph, mv, dt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.phLog);
            mv = itemView.findViewById(R.id.mVLog);
            dt = itemView.findViewById(R.id.date);
        }
    }
}

