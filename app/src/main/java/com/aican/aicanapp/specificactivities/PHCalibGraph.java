package com.aican.aicanapp.specificactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.aican.aicanapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class PHCalibGraph extends AppCompatActivity {

    LineDataSet lineDataSet = new LineDataSet(null, null);
    LineData lineData;
    ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    LineChart lineChart;
    DatabaseReference deviceRef;
    float ph1, ph2, ph3, ph4, ph5;
    float mv1, mv2, mv3, mv4, mv5;
    GraphView graphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phcalib_graph);
//        lineChart = findViewById(R.id.graph);
        graphView = findViewById(R.id.graph);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

//        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_1").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                String phVal = snapshot.getValue(String.class);
//                if (phVal != null) {
//                    ph1 = Float.parseFloat(phVal);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                String phVal = snapshot.getValue(String.class);
//                if (phVal != null) {
//                    ph2 = Float.parseFloat(phVal);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                String phVal = snapshot.getValue(String.class);
//                if (phVal != null) {
//                    ph3 = Float.parseFloat(phVal);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                String phVal = snapshot.getValue(String.class);
//                if (phVal != null) {
//                    ph4 = Float.parseFloat(phVal);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_5").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                String phVal = snapshot.getValue(String.class);
//                if (phVal != null) {
//                    ph5 = Float.parseFloat(phVal);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_1").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Float mv = snapshot.getValue(Float.class);
//                if (mv != null) {
//                    mv1 = mv;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_2").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Float mv = snapshot.getValue(Float.class);
//                if (mv != null) {
//                    mv2 = mv;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_3").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Float mv = snapshot.getValue(Float.class);
//                if (mv != null) {
//                    mv3 = mv;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_4").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Float mv = snapshot.getValue(Float.class);
//                if (mv != null) {
//                    mv4 = mv;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_5").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Float mv = snapshot.getValue(Float.class);
//                if (mv != null) {
//                    mv5 = mv;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });

        Intent i = getIntent();
        ph1 = Float.parseFloat(i.getStringExtra("PH1"));
        ph2 = Float.parseFloat(i.getStringExtra("PH2"));
        ph3 = Float.parseFloat(i.getStringExtra("PH3"));
        ph4 = Float.parseFloat(i.getStringExtra("PH4"));
        ph5 = Float.parseFloat(i.getStringExtra("PH5"));

        mv1 = Float.parseFloat(i.getStringExtra("MV1"));
        mv2 = Float.parseFloat(i.getStringExtra("MV2"));
        mv3 = Float.parseFloat(i.getStringExtra("MV3"));
        mv4 = Float.parseFloat(i.getStringExtra("MV4"));
        mv5 = Float.parseFloat(i.getStringExtra("MV5"));

        // activate horizontal zooming and scrolling
        graphView.getViewport().setScalable(true);

// activate horizontal scrolling
        graphView.getViewport().setScrollable(true);

// activate horizontal and vertical zooming and scrolling
        graphView.getViewport().setScalableY(true);

// activate vertical scrolling
        graphView.getViewport().setScrollableY(true);

        // set manual X bounds
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(-2);
        graphView.getViewport().setMaxX(20);

// set manual Y bounds
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-800);
        graphView.getViewport().setMaxY(800);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(ph1, mv1),
                new DataPoint(ph2, mv2),
                new DataPoint(ph3, mv3),
                new DataPoint(ph4, mv4),
                new DataPoint(ph5, mv5)
        });
        graphView.addSeries(series);
        series.setDrawDataPoints(true);
        series.setAnimated(true);

        ArrayList<Entry> information = new ArrayList<>();

        Log.e("values021",ph1 + " " + ph2 + " " + ph3 + " " + ph4 + " " + ph5);
        Log.e("values021",mv1 + " " + mv2 + " " + mv3 + " " + mv4 + " " + mv5);

//        information.add(new Entry(ph1, mv1));
//        information.add(new Entry(ph2, mv2));
//        information.add(new Entry(ph3, mv3));
//        information.add(new Entry(ph4, mv4));
//        information.add(new Entry(ph5, mv5));
//
//        YAxis y = new YAxis();
//        y.mAxisMaximum= 800;
//        y.mAxisMinimum = -800;
//
//        XAxis x = new XAxis();
//        x.mAxisMaximum = 20;
//        x.mAxisMinimum = -2;
//        showChart(information);

    }


//    public void showChart(ArrayList<Entry> dataVal) {
//        lineDataSet.setValues(dataVal);
//        iLineDataSets.add(lineDataSet);
//        lineData = new LineData(iLineDataSets);
//        YAxis yAxis = lineChart.get();
//        XAxis xAxis = lineChart.getXAxis();
//        lineChart.clear();
//        lineChart.setData(lineData);
//        lineChart.invalidate();
//
//    }
}