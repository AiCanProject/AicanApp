package com.aican.aicanapp.fragments.ph;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.BatteryDialog;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class PhFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "PhFragment";
    PhView phView;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, slopeCurr, offsetCurr, batteryCurr;

    DatabaseHelper databaseHelper;
    DatabaseReference deviceRef;
    SwitchCompat switchAtc;

    BatteryDialog batteryDialog;

    float ph = 0;
    int skipPoints = 0;
    String[] probe = {"Unbreakable", "Glass", "Others"};

    ArrayList<Entry> entriesOriginal;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_main, container, false);
    }

    boolean isTimeOptionsVisible = false;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcCurr = view.findViewById(R.id.tvEcCurr);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);

       switchAtc = view.findViewById(R.id.switchAtc);
        Spinner probesVal = view.findViewById(R.id.probesVal);

        offsetCurr = view.findViewById(R.id.offsetVal);
        batteryCurr = view.findViewById(R.id.batteryPercent);
        slopeCurr = view.findViewById(R.id.slopeVal);

        phView = view.findViewById(R.id.phView);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);

        entriesOriginal = new ArrayList<>();

        ArrayAdapter ad = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, probe);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        probesVal.setAdapter(ad);

        databaseHelper = new DatabaseHelper(requireContext());

        Cursor res = databaseHelper.get_data();
        while (res.moveToNext()) {
            Source.userName = res.getString(0);
            Source.userRole = res.getString(1);
            Source.userId = res.getString(2);
            Source.userPasscode = res.getString(3);
        }

        batteryDialog = new BatteryDialog();
        DialogMain dialogMain = new DialogMain();
        dialogMain.setCancelable(false);
        Source.userTrack = "PhFragment logged in by ";
        dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        setupListeners();
    }

    private void rescaleGraph() {
        ArrayList<Entry> entries = new ArrayList<>();
        int count = 0;
        for (Entry entry : entriesOriginal) {
            if (count == 0) {
                entries.add(entry);
            }
            ++count;
            if (count >= skipPoints) {
                count = 0;
            }
        }
    }

    private void setupListeners() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if (ph == null) return;
                phView.moveTo(ph);
                String phForm = String.format(Locale.UK, "%.2f", ph);
                tvPhCurr.setText(phForm);
                PhFragment.this.ph = ph;
                phView.moveTo(ph);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("HOLD").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float hold = snapshot.getValue(Float.class);

                if (hold == 1) {
                    tvPhCurr.setTextColor(Color.GREEN);
                } else if (hold != 1) {
                    tvPhCurr.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String tempp = snapshot.getValue(Integer.class).toString();
                tvTempCurr.setText(tempp + "°C");
                Float temp = snapshot.getValue(Float.class);
                String tempForm = String.format(Locale.UK, "%.1f", temp);
                tvTempCurr.setText(tempForm + "°C");

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Extras", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("temp", tvTempCurr.getText().toString());
                edit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String ecc = snapshot.getValue(Integer.class).toString();
                tvEcCurr.setText(ecc);
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.1f", ec);
                tvEcCurr.setText(ecForm);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("OFFSET").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String offset = snapshot.getValue(Integer.class).toString();
                offsetCurr.setText(offset);
                Float offSet = snapshot.getValue(Float.class);
                String offsetForm = String.format(Locale.UK, "%.2f", offSet);
                offsetCurr.setText(offsetForm);

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Extras", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("offset", offsetCurr.getText().toString());
                edit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("BATTERY").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String battery = snapshot.getValue(Integer.class).toString();
                batteryCurr.setText(battery);
                batteryCurr.setText(battery + " %");

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Extras", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("battery", batteryCurr.getText().toString() + "%");
                edit.commit();
                Log.d("6516516", battery);

                if (battery.equals("25") || battery.equals("20") || battery.equals("15") || battery.equals("10") || battery.equals("5")) {
                    batteryDialog.show(getActivity().getSupportFragmentManager(), "example dialog");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("SLOPE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String slope = snapshot.getValue(Integer.class).toString();
                slopeCurr.setText(slope + " %");

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Extras", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("slope", slopeCurr.getText().toString());
                edit.commit();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        switchAtc.setOnCheckedChangeListener((v, isChecked) -> {

            deviceRef.child("Data").child("ATC").setValue(isChecked ? 1  : 0);
            if (isChecked){
                switchAtc.setText("On  ");
            }else {
                switchAtc.setText("Off  ");
            }
        });


//        deviceRef.child("Data").child("ATC").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Integer atc = snapshot.getValue(Integer.class);
//                if (atc == null) return;
//
//                if (switchAtc.setChecked(true))
//                switchAtc.setChecked(atc == 1);
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
