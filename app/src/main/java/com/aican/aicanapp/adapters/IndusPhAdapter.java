package com.aican.aicanapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.IndusPhDevices;
import com.aican.aicanapp.specificactivities.IndusPhActivity;

import com.aican.aicanapp.utils.DashboardListsOptionsClickListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class IndusPhAdapter extends RecyclerView.Adapter<IndusPhAdapter.IPhAdapterViewHolder> {

//    private String[] phs;     // Store data here in list or array from backend
//    private String deviceId;
//    Context context;// unique device id

//    public PhAdapter(String[] phs,Context context) {
//        this.phs = phs;
//        this.context = context;
//    }

    ArrayList<IndusPhDevices> indusPhDevices;
    DashboardListsOptionsClickListener optionsClickListener;

    public IndusPhAdapter(ArrayList<IndusPhDevices> indusPhDevices, DashboardListsOptionsClickListener optionsClickListener) {
        this.indusPhDevices = indusPhDevices;
        this.optionsClickListener = optionsClickListener;
    }

    @Override
    public IPhAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.indus_ph_item, parent, false);
        return new IPhAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IPhAdapterViewHolder holder, int position) {
        holder.bind(indusPhDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return indusPhDevices.size();
    }

    public class IPhAdapterViewHolder extends RecyclerView.ViewHolder {
         private TextView ph, ec, temp, tds, tvName;
        private ImageView ivOptions;

        //Viewholder-----------------------------------------------------------------------------------------
        public IPhAdapterViewHolder(View itemView) {
            super(itemView);
            ph = itemView.findViewById(R.id.ph);
            ec = itemView.findViewById(R.id.ec);
            //temp = itemView.findViewById(R.id.temp);
            //tds = itemView.findViewById(R.id.tds);
            tvName = itemView.findViewById(R.id.custom_device_name);
            ivOptions = itemView.findViewById(R.id.ivOptions);

        }

        public void bind(IndusPhDevices device) {
            String phString;
            if (device.getPh() < 0 || device.getPh() > 14) {
                phString = "pH: -";
            } else {
                phString = String.format(Locale.UK, "%.2f", device.getPh());
            }
            String ecString = String.format(Locale.UK, "mV: %.2f", device.getEc());
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
            //temp.setText(tempString);
            //tds.setText(tdsString);
            tvName.setText(device.getName()+"  "+device.getId());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), IndusPhActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
                itemView.getContext().startActivity(intent);
            });

            ivOptions.setOnClickListener(v -> {
                optionsClickListener.onOptionsIconClicked(v, device.getId());
            });

            setFirebaseListeners(device);
        }

        private void setFirebaseListeners(IndusPhDevices device) {

            DatabaseReference dataRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(device.getId())).getReference().child(Dashboard.DEVICE_TYPE_IPH).child(device.getId()).child("Data");

            dataRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                }

                @Override
                public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                    if (snapshot.getKey() == null || snapshot.getValue() == null) return;
                    switch (snapshot.getKey()) {
                        case "PH_VAL": {
                            Float ph = snapshot.getValue(Float.class);
                            if (ph == null) return;
                            device.setPh(ph);
                            break;
                        }
                       /* case "TEMP_VAL": {
                            Integer val = snapshot.getValue(Integer.class);
                            if (val == null) return;
                            device.setTemp(val);
                            break;
                        }*/
                        case "MV_VAL": {
                            Float val = snapshot.getValue(Float.class);
                            if (val == null) return;
                            device.setEc(val);
                            break;
                        }
                        //case "TDS_VAL": {
                        //Long val = snapshot.getValue(Long.class);
                        //if (val == null) return;
                        //device.setTds(val);
                        //  break;
                        //}
                    }
                    notifyItemChanged(getAdapterPosition());
                }

                @Override
                public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        //Viewholder-----------------------------------------------------------------------------------------
    }

}
