package com.aican.aicanapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.PhDevice;

import java.util.ArrayList;
import java.util.Locale;

public class PhAdapter extends RecyclerView.Adapter<PhAdapter.PhAdapterViewHolder> {

//    private String[] phs;     // Store data here in list or array from backend
//    private String deviceId;
//    Context context;// unique device id

//    public PhAdapter(String[] phs,Context context) {
//        this.phs = phs;
//        this.context = context;
//    }

    ArrayList<PhDevice> phDevices;

    public PhAdapter(ArrayList<PhDevice> phDevices) {
        this.phDevices = phDevices;
    }

    @Override
    public PhAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.ph_item, parent, false);
        return new PhAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhAdapter.PhAdapterViewHolder holder, int position) {
//        holder.ph.setText(phs[position]);
//        deviceId = Integer.toString(position);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                context.startActivity(new Intent(context,PhActivity.class));
//            }
//        });
        holder.bind(phDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return phDevices.size();
    }

    public class PhAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView ph, ec, temp, tds,tvName;

        //Viewholder-----------------------------------------------------------------------------------------
        public PhAdapterViewHolder(View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.ph);
            ec = itemView.findViewById(R.id.ec);
            temp = itemView.findViewById(R.id.temp);
            tds = itemView.findViewById(R.id.tds);
            tvName = itemView.findViewById(R.id.custom_device_name);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String deviceIdStr;
//                    Context context = itemView.getContext();
//                    deviceIdStr = "Ph " + deviceId;
//                    Intent toPh = new Intent(context, TemperatureActivity.class);
//                    toPh.putExtra("deviceId",deviceIdStr);
//                    context.startActivity(toPh);
//                }
//            });
        }

        public void bind(PhDevice device) {
            String phString = String.format(Locale.UK, "pH: %.2f", device.getPh());
            String ecString = String.format(Locale.UK, "EC: %.2f mS/cm", device.getEc());
            String tempString = String.format(Locale.UK, "Temp: %d°C", device.getTemp());
            String tdsString = String.format(Locale.UK, "TDS: %d ppm", device.getTds());
            ph.setText(phString);
            ec.setText(ecString);
            temp.setText(tempString);
            tds.setText(tdsString);
            tvName.setText(device.getName());
        }

        //Viewholder-----------------------------------------------------------------------------------------
    }
}
