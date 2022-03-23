package com.aican.aicanapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.dataClasses.phData;

import java.util.ArrayList;

public class BufferAdapter extends RecyclerView.Adapter<BufferAdapter.ViewHolder> {

    private Context context;
    ArrayList<BufferData> list ;

    public BufferAdapter(ArrayList<BufferData> mlist, Context context) {
        this.list = mlist;
        this.context = context;
    }

    @NonNull
    @Override
    public BufferAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.buffer_items, parent, false);
        return new BufferAdapter.ViewHolder(listItem);

    }

    @Override
    public void onBindViewHolder(@NonNull BufferAdapter.ViewHolder holder, int position) {
        holder.ph.setText(list.get(position).getPh());
        holder.mv.setText(list.get(position).getMv());
        holder.time.setText(list.get(position).getTime());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        public TextView ph, mv, time, editPh;
        LinearLayout bftItem;
        View mView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.ph1);
            mv = itemView.findViewById(R.id.mv1);
            editPh = itemView.findViewById(R.id.phEdit1);
            time = itemView.findViewById(R.id.time);
            bftItem = itemView.findViewById(R.id.bft_item);
            mView = itemView;
        }
    }
}
