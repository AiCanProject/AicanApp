package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.*;

public class CoolingAdapter extends RecyclerView.Adapter<CoolingAdapter.CoolingAdapterViewHolder> {

    private String[] coolings; // Store data here in list or array from backend
    private String deviceId;   // unique device id


    public CoolingAdapter(String[] coolings) {
        this.coolings = coolings;
    }

    @Override
    public CoolingAdapterViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cooling_item,parent,false);
        return new CoolingAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CoolingAdapter.CoolingAdapterViewHolder holder, int position) {
        holder.cooling.setText(coolings[position]);
        deviceId = Integer.toString(position);
    }

    @Override
    public int getItemCount() {
        return coolings.length;
    }

    //viewHolder---------------------------------------------------------------------------

    public class CoolingAdapterViewHolder extends RecyclerView.ViewHolder{

        private TextView cooling;
        public CoolingAdapterViewHolder(View itemView) {
            super(itemView);
            cooling = itemView.findViewById(R.id.cooling);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String deviceIdStr;
                    Context context = itemView.getContext();
                    deviceIdStr = "cooling " + deviceId;
                    Intent toCool = new Intent(context, TemperatureActivity.class);
                    toCool.putExtra("deviceId",deviceIdStr);
                    context.startActivity(toCool);
                }
            });
        }
    }
    //viewHolder---------------------------------------------------------------------------

}
