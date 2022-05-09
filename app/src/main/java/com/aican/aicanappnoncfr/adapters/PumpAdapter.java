package com.aican.aicanappnoncfr.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.dataClasses.PumpDevice;
import com.aican.aicanappnoncfr.specificactivities.PumpActivity;
import com.aican.aicanappnoncfr.utils.DashboardListsOptionsClickListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class PumpAdapter extends RecyclerView.Adapter<PumpAdapter.PumpAdapterViewHolder> {

//    private String modes[]; // Store data here in list or array from backend
//    private String deviceId;

//    public PumpAdapter(String[] modes) {
//        this.modes = modes;
//    }

    ArrayList<PumpDevice> pumpDevices;
    DashboardListsOptionsClickListener optionsClickListener;

    public PumpAdapter(ArrayList<PumpDevice> pumpDevices, DashboardListsOptionsClickListener optionsClickListener) {
        this.pumpDevices = pumpDevices;
        this.optionsClickListener = optionsClickListener;
    }

    @Override
    public PumpAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.pump_item,parent,false);
        return new PumpAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PumpAdapterViewHolder holder, int position) {
//        holder.mode.setText(modes[position]);
//        deviceId = Integer.toString(position);
        holder.bind(pumpDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return pumpDevices.size();
    }

    public class PumpAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMode, tvVol, tvSpeed, tvDir, tvName;
        private ImageView ivOptions;
        private SwitchCompat onOffSwitch;

        //Viewholder-----------------------------------------------------------------------------------------
        public PumpAdapterViewHolder(View itemView) {
            super(itemView);
            tvMode = itemView.findViewById(R.id.mode);
            tvVol = itemView.findViewById(R.id.tvVol);
            tvSpeed = itemView.findViewById(R.id.tvSpeed);
            tvDir = itemView.findViewById(R.id.tvDir);
            tvName = itemView.findViewById(R.id.custom_device_name);
            ivOptions = itemView.findViewById(R.id.ivOptions);
            onOffSwitch = itemView.findViewById(R.id.switch1);
        }

        public void bind(PumpDevice device){
            String mode;
            if(device.getMode()==0){
                mode = "Mode: Dose";
                String vol = String.format(Locale.UK, "Vol: %d ml", device.getVol());
                tvVol.setVisibility(View.VISIBLE);
                tvVol.setText(vol);
            }else{
                mode = "Mode: Pump";
                tvVol.setVisibility(View.GONE);
            }
            String speed = String.format(Locale.UK, "%d mL/min", device.getSpeed());
            String dir;
            if (device.getDir() == 0) {
                dir = "Clockwise";
            } else {
                dir = "AntiClockwise";
            }
            if (device.getStatus() == PumpActivity.STATUS_PUMP || device.getStatus() == PumpActivity.STATUS_DOSE) {
                onOffSwitch.setChecked(true);
            } else {
                onOffSwitch.setChecked(false);
            }

            tvName.setText(device.getName());
            tvMode.setText(mode);
            tvSpeed.setText(speed);
            tvDir.setText(dir);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PumpActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
                itemView.getContext().startActivity(intent);
            });
            ivOptions.setOnClickListener(v -> {
                optionsClickListener.onOptionsIconClicked(v, device.getId());
            });

            setFirebaseListeners(device);
        }

        private void setFirebaseListeners(PumpDevice device) {
            DatabaseReference uiRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(device.getId())).getReference().child(Dashboard.DEVICE_TYPE_PUMP).child(device.getId())
                    .child("UI");

            uiRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    device.setMode(snapshot.child("Mode").getValue(Integer.class));
                    device.setDir(snapshot.child("Direction").getValue(Integer.class));
                    device.setSpeed(snapshot.child("Speed").getValue(Integer.class));
                    device.setVol(snapshot.child("Volume").getValue(Integer.class));
                    notifyItemChanged(getAdapterPosition());
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
            uiRef.child("Start").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    device.setStatus(snapshot.getValue(Integer.class));
                    notifyItemChanged(getAdapterPosition());
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        //Viewholder-----------------------------------------------------------------------------------------
    }
}
