package com.aican.aicanapp.specificactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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
    Button takeScreenshot, viewScreenshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phcalib_graph);
        graphView = findViewById(R.id.graph);
        takeScreenshot = findViewById(R.id.takeScreenshot);
        viewScreenshot = findViewById(R.id.viewScreenshot);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

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

        graphView.getViewport().setScalable(true);

        graphView.getViewport().setScrollable(true);

        graphView.getViewport().setScalableY(true);

        graphView.getViewport().setScrollableY(true);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(-2);
        graphView.getViewport().setMaxX(20);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-800);
        graphView.getViewport().setMaxY(800);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(ph1, mv1),
                new DataPoint(ph2, mv2),
                new DataPoint(ph3, mv3),
                new DataPoint(ph4, mv4),
                new DataPoint(ph5, mv5)
        });
        graphView.addSeries(series);
        series.setDrawDataPoints(true);
        series.setAnimated(true);

        Log.e("values021", ph1 + " " + ph2 + " " + ph3 + " " + ph4 + " " + ph5);
        Log.e("values021", mv1 + " " + mv2 + " " + mv3 + " " + mv4 + " " + mv5);

        takeScreenshot.setOnClickListener(v -> {

        });

        viewScreenshot.setOnClickListener(v -> {

        });

    }

}