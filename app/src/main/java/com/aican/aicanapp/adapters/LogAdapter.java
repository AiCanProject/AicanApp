package com.aican.aicanapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.phData;

import java.util.ArrayList;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    ArrayList<phData> list ;

    public LogAdapter(ArrayList<phData> mlist) {
        this.list = mlist;
    }

    @NonNull
    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.item_log, parent, false);
        return new ViewHolder(listItem);

    }

    @Override
    public void onBindViewHolder(@NonNull LogAdapter.ViewHolder holder, int position) {

        holder.ph.setText(list.get(position).getpH());
        holder.mv.setText(list.get(position).getmV());
        holder.dt.setText(list.get(position).getDate());
        holder.tim.setText(list.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView ph, mv, dt, tim;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.phLog);
            mv = itemView.findViewById(R.id.mVLog);
            dt = itemView.findViewById(R.id.date);
            tim = itemView.findViewById(R.id.time);
        }
    }
}

