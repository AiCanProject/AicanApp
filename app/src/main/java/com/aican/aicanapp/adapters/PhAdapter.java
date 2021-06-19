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

public class PhAdapter extends RecyclerView.Adapter<PhAdapter.PhAdapterViewHolder> {

    private String[] phs;     // Store data here in list or array from backend
    private String deviceId;
    Context context;// unique device id

    public PhAdapter(String[] phs,Context context) {
        this.phs = phs;
        this.context = context;
    }

    @Override
    public PhAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.ph_item,parent,false);
        return new PhAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhAdapter.PhAdapterViewHolder holder, int position) {
//        holder.ph.setText(phs[position]);
        deviceId = Integer.toString(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context,PhActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return phs.length;
    }

    public class PhAdapterViewHolder extends RecyclerView.ViewHolder{
        private TextView ph, ec, temp, tds;
        //Viewholder-----------------------------------------------------------------------------------------
        public PhAdapterViewHolder(View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.ph);
            ec = itemView.findViewById(R.id.ec);
            temp = itemView.findViewById(R.id.temp);
            tds = itemView.findViewById(R.id.tds);

            ph.setText("pH: 14");
            ec.setText("EC: 10.00 mS/cm");
            temp.setText("Temp: 99°C");
            tds.setText("TDS: 9999 ppm");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String deviceIdStr;
                    Context context = itemView.getContext();
                    deviceIdStr = "Ph " + deviceId;
                    Intent toPh = new Intent(context, TemperatureActivity.class);
                    toPh.putExtra("deviceId",deviceIdStr);
                    context.startActivity(toPh);
                }
            });
        }
        //Viewholder-----------------------------------------------------------------------------------------
    }
}
