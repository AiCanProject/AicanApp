package com.aican.aicanapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dataClasses.EcDevice;
import com.aican.aicanapp.specificactivities.EcActivity;
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

public class EcAdapter extends RecyclerView.Adapter<EcAdapter.EcAdapterViewHolder> {
    ArrayList<EcDevice> ecDevices;
    DashboardListsOptionsClickListener optionsClickListener;

    public EcAdapter(ArrayList<EcDevice> ecDevices, DashboardListsOptionsClickListener optionsClickListener) {
        this.ecDevices = ecDevices;
        this.optionsClickListener = optionsClickListener;
    }

    @NonNull
    @Override
    public EcAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.ec_item,parent,false);
        return new EcAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EcAdapterViewHolder holder, int position) {


        holder.bind(ecDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return ecDevices.size();
    }

    public class EcAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView ec;
        private TextView tvName;
        private ImageView ivOptions;


        //Viewholder-----------------------------------------------------------------------------------------
        public EcAdapterViewHolder(View itemView) {
            super(itemView);
            ec = itemView.findViewById(R.id.ph);
            tvName = itemView.findViewById(R.id.custom_device_name);
            ivOptions = itemView.findViewById(R.id.ivOptions);
        }

        public void bind(EcDevice device) {
            String ecString = String.format(Locale.UK, "%d", device.getEc());
            tvName.setText(device.getName());
            ec.setText(ecString);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), EcActivity.class);
                intent.putExtra(Dashboard.KEY_DEVICE_ID, device.getId());
              //  intent.putExtra(EcActivity.DEVICE_TYPE_KEY, Dashboard.DEVICE_TYPE_EC);
                itemView.getContext().startActivity(intent);
            });

            ivOptions.setOnClickListener(v -> {
                optionsClickListener.onOptionsIconClicked(v, device.getId());
            });

            setFirebaseListeners(device);
        }

        private void setFirebaseListeners(EcDevice device) {

            DatabaseReference deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(device.getId())).getReference().child(Dashboard.DEVICE_TYPE_EC).child(device.getId());

            deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Integer val = snapshot.getValue(Integer.class);
                    if (val == null) return;
                    int prev = device.getEc();
                    device.setEc(val);
                    if (prev != device.getEc())
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
