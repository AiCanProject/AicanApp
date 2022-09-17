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
import com.aican.aicanapp.dataClasses.tempData;

import java.util.List;

public class TempLogAdapter extends RecyclerView.Adapter<TempLogAdapter.ViewHolder> {

    Context context;
    List<tempData> logs_list;

    public TempLogAdapter(Context context, List<tempData> logs_list) {
        this.context = context;
        this.logs_list = logs_list;
    }

    @NonNull
    @Override
    public TempLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_log, parent, false);
        return new TempLogAdapter.ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TempLogAdapter.ViewHolder holder, int position) {
        if(logs_list != null && logs_list.size() > 0){
            holder.settemp.setText(logs_list.get(position).getSet_temp());
            holder.temp1.setText(logs_list.get(position).getTemp1());
            holder.dt.setText(logs_list.get(position).getDate());
            holder.time.setText(logs_list.get(position).getTime());
            holder.productnum.setText(logs_list.get(position).getProduct_name());
            holder.batchnum.setText(logs_list.get(position).getBatchnum());
            holder.temp2.setText(logs_list.get(position).getTemp2());
        }
    }

    @Override
    public int getItemCount() {
        return logs_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView settemp, temp1, dt, time, batchnum, arnum, temp2,productnum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            settemp = itemView.findViewById(R.id.phLog);
            temp1 = itemView.findViewById(R.id.tempLog);
            time = itemView.findViewById(R.id.time);
            dt = itemView.findViewById(R.id.date);
            productnum = itemView.findViewById(R.id.batchnum);
            batchnum = itemView.findViewById(R.id.arnum);
            temp2 = itemView.findViewById(R.id.compound);
        }
    }
}