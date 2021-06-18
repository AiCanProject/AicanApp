package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.PumpActivity;

public class PumpAdapter extends RecyclerView.Adapter<PumpAdapter.PumpAdapterViewHolder> {

    private String modes[]; // Store data here in list or array from backend
    private String deviceId;

    public PumpAdapter(String[] modes) {
        this.modes = modes;
    }

    @Override
    public PumpAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.pump_item,parent,false);
        return new PumpAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PumpAdapterViewHolder holder, int position) {
        holder.mode.setText(modes[position]);
        deviceId = Integer.toString(position);
    }

    @Override
    public int getItemCount() {
        return modes.length;
    }

    public class PumpAdapterViewHolder extends RecyclerView.ViewHolder{
        private TextView mode;
        //Viewholder-----------------------------------------------------------------------------------------
        public PumpAdapterViewHolder(View itemView) {
            super(itemView);
            mode = itemView.findViewById(R.id.mode);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    String deviceIdStr;
                    Context context = itemView.getContext();
//                    deviceIdStr = "Pump " + deviceId;
                    Intent tpPump = new Intent(context, PumpActivity.class);
//                    tpPump.putExtra("deviceId",deviceIdStr);
                    context.startActivity(tpPump);
                }
            });
        }
        //Viewholder-----------------------------------------------------------------------------------------
    }
}
