package com.aican.aicanappnoncfr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.dataClasses.BufferData;

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
