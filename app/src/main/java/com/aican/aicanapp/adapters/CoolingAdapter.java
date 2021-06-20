package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.CoolingDevice;
import com.aican.aicanapp.dataClasses.TempDevice;
import com.aican.aicanapp.specificactivities.*;

import java.util.ArrayList;
import java.util.Locale;

public class CoolingAdapter extends RecyclerView.Adapter<CoolingAdapter.CoolingAdapterViewHolder> {

//    private String[] coolings; // Store data here in list or array from backend
//    private String deviceId;   // unique device id


//    public CoolingAdapter(String[] coolings) {
//        this.coolings = coolings;
//    }

    ArrayList<CoolingDevice> coolingDevices;

    public CoolingAdapter(ArrayList<CoolingDevice> coolingDevices) {
        this.coolingDevices = coolingDevices;
    }

    @Override
    public CoolingAdapterViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cooling_item,parent,false);
        return new CoolingAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CoolingAdapter.CoolingAdapterViewHolder holder, int position) {
//        holder.cooling.setText(coolings[position]);
//        deviceId = Integer.toString(position);
        holder.bind(coolingDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return coolingDevices.size();
    }

    //viewHolder---------------------------------------------------------------------------

    public class CoolingAdapterViewHolder extends RecyclerView.ViewHolder{

        private TextView cooling;
        private TextView tvName;

        public CoolingAdapterViewHolder(View itemView) {
            super(itemView);
            cooling = itemView.findViewById(R.id.cooling);
            tvName = itemView.findViewById(R.id.custom_device_name);


//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String deviceIdStr;
//                    Context context = itemView.getContext();
//                    deviceIdStr = "cooling " + deviceId;
//                    Intent toCool = new Intent(context, TemperatureActivity.class);
//                    toCool.putExtra("deviceId",deviceIdStr);
//                    context.startActivity(toCool);
//                }
//            });
        }

        public void bind(CoolingDevice device){
            String tempString = String.format(Locale.UK, "%d°C", device.getTemp());
            tvName.setText(device.getName());
            cooling.setText(tempString);
        }
    }
    //viewHolder---------------------------------------------------------------------------

}
