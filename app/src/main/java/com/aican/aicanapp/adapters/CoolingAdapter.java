package com.aican.aicanapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.CoolingDevice;
import com.aican.aicanapp.specificactivities.TemperatureActivity;
import com.aican.aicanapp.utils.DashboardListsOptionsClickListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class CoolingAdapter extends RecyclerView.Adapter<CoolingAdapter.CoolingAdapterViewHolder> {

//    private String[] coolings; // Store data here in list or array from backend
//    private String deviceId;   // unique device id


//    public CoolingAdapter(String[] coolings) {
//        this.coolings = coolings;
//    }

    ArrayList<CoolingDevice> coolingDevices;
    DashboardListsOptionsClickListener optionsClickListener;

    public CoolingAdapter(ArrayList<CoolingDevice> coolingDevices, DashboardListsOptionsClickListener optionsClickListener) {
        this.coolingDevices = coolingDevices;
        this.optionsClickListener = optionsClickListener;
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

    public class CoolingAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView cooling;
        private TextView tvName;
        private ImageView ivOptions;
        private ProgressBar progressBar;

        public CoolingAdapterViewHolder(View itemView) {
            super(itemView);
            cooling = itemView.findViewById(R.id.cooling);
            tvName = itemView.findViewById(R.id.custom_device_name);
            ivOptions = itemView.findViewById(R.id.ivOptions);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(CoolingDevice device) {
            String tempString = String.format(Locale.UK, "%d°C", device.getTemp());
            tvName.setText(device.getName());
            cooling.setText(tempString);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), TemperatureActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
                intent.putExtra(TemperatureActivity.DEVICE_TYPE_KEY, Dashboard.DEVICE_TYPE_COOLING);
                itemView.getContext().startActivity(intent);
            });

            ivOptions.setOnClickListener(v -> {
                optionsClickListener.onOptionsIconClicked(v, device.getId());
            });

            setFirebaseListeners(device);
        }

        private void setFirebaseListeners(CoolingDevice device) {

            DatabaseReference deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(device.getId())).getReference().child(Dashboard.DEVICE_TYPE_COOLING).child(device.getId());

            deviceRef.child("UI").child("TEMP").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Integer val = snapshot.getValue(Integer.class);
                    if (val == null) return;
                    int prev = device.getTemp();
                    device.setTemp(val);
                    if (prev != device.getTemp())
                        notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }
    //viewHolder---------------------------------------------------------------------------

}
