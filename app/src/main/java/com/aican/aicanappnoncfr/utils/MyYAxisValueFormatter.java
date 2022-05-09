package com.aican.aicanappnoncfr.utils;

import androidx.annotation.NonNull;

import com.aican.aicanappnoncfr.specificactivities.PhActivity;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

public class MyYAxisValueFormatter extends IndexAxisValueFormatter {

    DatabaseReference deviceRef;
    @Override
    public String getFormattedValue(float value) {
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Entry> dataVal = new ArrayList<Entry>();
                if (snapshot.hasChildren()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        DataPoint dataPoint = dataSnapshot.getValue(DataPoint.class);
                        dataVal.add(new Entry((float)dataPoint.getX(),(float)dataPoint.getY()));
                    }
                    //showChart(dataVal);
                }else{
                    //lineChart.clear();
                    //lineChart.invalidate();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return super.getFormattedValue(value);
    }
}
