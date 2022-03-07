package com.aican.aicanapp.fragments.ph;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class phLogFragment extends Fragment {


    DatabaseReference deviceRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button logBtn = view.findViewById(R.id.logBtn);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        phData phData = new phData();
        ArrayList<phData> list = new ArrayList<>();


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);



        logBtn.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
            phData.setDate(currentTime);

            deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float p = snapshot.getValue(Float.class);
                    String ph = String.format(Locale.UK, "%.2f", p );
                    phData.setpH(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });


            deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float mv = snapshot.getValue(Float.class);
                    String m = String.format(Locale.UK, "%.2f", mv );
                    phData.setmV(m);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
            LogAdapter adapter = new LogAdapter(list);
            list.add(phData);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


    }
}