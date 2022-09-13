package com.aican.aicanapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.ecLogModel;

import java.util.List;

public class ECLogAdapter extends RecyclerView.Adapter<ECLogAdapter.ViewHolder> {

    Context context;
    List<ecLogModel> logs_list;

    public ECLogAdapter(Context context, List<ecLogModel> logs_list) {
        this.context = context;
        this.logs_list = logs_list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_log_ec, parent, false);
        return new ECLogAdapter.ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(logs_list != null && logs_list.size() > 0){
            holder.date.setText(logs_list.get(position).getDate());
            holder.time.setText(logs_list.get(position).getTime());
            holder.conductivity.setText(logs_list.get(position).getConductivity());
            holder.TDS.setText(logs_list.get(position).getTDS());
            holder.temp.setText(logs_list.get(position).getTemperature());
            holder.productName.setText(logs_list.get(position).getProductName());
            holder.batchNum.setText(logs_list.get(position).getBatchNum());
        }
    }

    @Override
    public int getItemCount() {
        return logs_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, time, conductivity, TDS, temp, productName, batchNum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            conductivity = itemView.findViewById(R.id.conductivity);
            TDS = itemView.findViewById(R.id.TDS);
            temp = itemView.findViewById(R.id.temp);
            productName = itemView.findViewById(R.id.product);
            batchNum = itemView.findViewById(R.id.batchNum);
        }
    }

}
