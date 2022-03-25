package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.aican.aicanapp.UserDatabaseModel;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class BufferAdapter extends RecyclerView.Adapter<BufferAdapter.ViewHolder> {

    Context context;
    List<BufferData> list;

    public BufferAdapter(List<BufferData> mlist, Context context) {
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
        BufferData data = list.get(position);

        holder.ph.setText(data.getPh());
        holder.mv.setText(data.getMv());
        holder.time.setText(data.getTime());

/*
        holder.editPh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {

                });
                dialog.show(dialog.getParentFragmentManager(), null);

            }
        }); */
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
