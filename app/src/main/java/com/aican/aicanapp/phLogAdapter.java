package com.aican.aicanapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class phLogAdapter extends RecyclerView.Adapter<phLogAdapter.MyViewHolder> {

    Context context;
    List<logTable> listt;

    public phLogAdapter(Context context, List<logTable> list) {
        this.context = context;
        this.listt = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.pH.setText(listt.get(position).getpH());
        holder.date.setText(listt.get(position).getDate());
        holder.mV.setText(listt.get(position).getmV());
    }

    @Override
    public int getItemCount() {
        return listt == null ? 0 : listt.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView pH, mV, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            pH = itemView.findViewById(R.id.phView);
            date = itemView.findViewById(R.id.date);
            mV = itemView.findViewById(R.id.mVLog);
        }
    }
}
