package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.TempDevice;
import com.aican.aicanapp.specificactivities.*;

import java.util.ArrayList;
import java.util.Locale;

public class TempAdapter extends RecyclerView.Adapter<TempAdapter.TempAdapterViewHolder> {

//    private String[] temps;  // Store data here in list or array from backend
//    private String deviceId; // unique device id
//    Context context;

//    public TempAdapter(Context context,String[] temps) {
//        this.temps = temps;
//        this.context = context;
//    }

    ArrayList<TempDevice> tempDevices;

    public TempAdapter(ArrayList<TempDevice> tempDevices) {
        this.tempDevices = tempDevices;
    }

    @Override
    public TempAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.temp_item,parent,false);
        return new TempAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TempAdapter.TempAdapterViewHolder holder, int position) {

//        holder.temperature.setText(temps[position]);
//        deviceId = Integer.toString(position);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String deviceIdStr;
//                deviceIdStr = "temp " + deviceId;
//                Intent toTemp = new Intent(context, TemperatureActivity.class);
//                toTemp.putExtra("deviceId",deviceIdStr);
//                toTemp.putExtra("deviceTemp",temps[position]);
//                context.startActivity(toTemp);
//            }
//        });
        holder.bind(tempDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return tempDevices.size();
    }

    public class TempAdapterViewHolder extends RecyclerView.ViewHolder{

        private TextView temperature;
        private TextView tvName;

        //Viewholder-----------------------------------------------------------------------------------------
        public TempAdapterViewHolder(View itemView) {
            super(itemView);
            temperature = itemView.findViewById(R.id.ph);
            tvName = itemView.findViewById(R.id.custom_device_name);
        }

        public void bind(TempDevice device){
            String tempString = String.format(Locale.UK, "%d°C", device.getTemp());
            tvName.setText(device.getName());
            temperature.setText(tempString);

            itemView.setOnClickListener(v->{
                Intent intent = new Intent(itemView.getContext(), TemperatureActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
                itemView.getContext().startActivity(intent);
            });
        }

        //Viewholder-----------------------------------------------------------------------------------------
    }
}
