package com.aican.aicanappnoncfr.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.dataClasses.TempDevice;
import com.aican.aicanappnoncfr.specificactivities.TemperatureActivity;
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

public class TempAdapter extends RecyclerView.Adapter<TempAdapter.TempAdapterViewHolder> {

//    private String[] temps;  // Store data here in list or array from backend
//    private String deviceId; // unique device id
//    Context context;

//    public TempAdapter(Context context,String[] temps) {
//        this.temps = temps;
//        this.context = context;
//    }

    ArrayList<TempDevice> tempDevices;
    DashboardListsOptionsClickListener optionsClickListener;

    public TempAdapter(ArrayList<TempDevice> tempDevices, DashboardListsOptionsClickListener optionsClickListener) {
        this.tempDevices = tempDevices;
        this.optionsClickListener = optionsClickListener;
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

    public class TempAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView temperature;
        private TextView tvName;
        private ImageView ivOptions;


        //Viewholder-----------------------------------------------------------------------------------------
        public TempAdapterViewHolder(View itemView) {
            super(itemView);
            temperature = itemView.findViewById(R.id.ph);
            tvName = itemView.findViewById(R.id.custom_device_name);
            ivOptions = itemView.findViewById(R.id.ivOptions);
        }

        public void bind(TempDevice device) {
            String tempString = String.format(Locale.UK, "%d°C", device.getTemp());
            tvName.setText(device.getName());
            temperature.setText(tempString);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), TemperatureActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
                intent.putExtra(TemperatureActivity.DEVICE_TYPE_KEY, Dashboard.DEVICE_TYPE_TEMP);
                itemView.getContext().startActivity(intent);
            });

            ivOptions.setOnClickListener(v -> {
                optionsClickListener.onOptionsIconClicked(v, device.getId());
            });

            setFirebaseListeners(device);
        }

        private void setFirebaseListeners(TempDevice device) {

            DatabaseReference deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(device.getId())).getReference().child(Dashboard.DEVICE_TYPE_TEMP).child(device.getId());

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

        //Viewholder-----------------------------------------------------------------------------------------
    }
}
