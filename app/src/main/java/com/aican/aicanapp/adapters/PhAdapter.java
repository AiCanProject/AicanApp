package com.aican.aicanapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.PhDevice;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.DashboardListsOptionsClickListener;

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
    DashboardListsOptionsClickListener optionsClickListener;

    public PhAdapter(ArrayList<PhDevice> phDevices, DashboardListsOptionsClickListener optionsClickListener) {
        this.phDevices = phDevices;
        this.optionsClickListener = optionsClickListener;
    }

    @Override
    public PhAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.ph_item, parent, false);
        return new PhAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhAdapter.PhAdapterViewHolder holder, int position) {
        holder.bind(phDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return phDevices.size();
    }

    public class PhAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView ph, ec, temp, tds, tvName;
        private ImageView ivOptions;

        //Viewholder-----------------------------------------------------------------------------------------
        public PhAdapterViewHolder(View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.ph);
            ec = itemView.findViewById(R.id.ec);
            temp = itemView.findViewById(R.id.temp);
            tds = itemView.findViewById(R.id.tds);
            tvName = itemView.findViewById(R.id.custom_device_name);
            ivOptions = itemView.findViewById(R.id.ivOptions);

        }

        public void bind(PhDevice device) {
            String phString;
            if (device.getPh() < 0 || device.getPh() > 14) {
                phString = "pH: -";
            } else {
                phString = String.format(Locale.UK, "pH: %.2f", device.getPh());
            }
            String ecString = String.format(Locale.UK, "EC: %.2f mS/cm", device.getEc());
            String tempString;
            if (device.getTemp() < -50 || device.getTemp() > 125) {
                tempString = "Temp: -";
            } else {
                tempString = String.format(Locale.UK, "Temp: %d°C", device.getTemp());
            }
            String tdsString;
            if (device.getTds() < 0 || device.getTds() > 9999) {
                tdsString = "TDS: -";
            } else {
                tdsString = String.format(Locale.UK, "TDS: %d ppm", device.getTds());
            }
            ph.setText(phString);
            ec.setText(ecString);
            temp.setText(tempString);
            tds.setText(tdsString);
            tvName.setText(device.getName());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PhActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
                itemView.getContext().startActivity(intent);
            });

            ivOptions.setOnClickListener(v -> {
                optionsClickListener.onOptionsIconClicked(v, device.getId());
            });
        }

        //Viewholder-----------------------------------------------------------------------------------------
    }

}
