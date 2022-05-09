package com.aican.aicanappnoncfr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.dataClasses.Step;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepViewHolder> {

    ArrayList<Step> list;

    public StepsAdapter(ArrayList<Step> list) {
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new StepViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.step_item_view, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull StepViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class StepViewHolder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tv;

        public StepViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            iv = itemView.findViewById(R.id.iv);
            tv = itemView.findViewById(R.id.tv);
        }

        void bind(Step step) {
            iv.setBackgroundColor(step.getBg());
            tv.setText("step "+(getAdapterPosition()+1));
        }
    }
}