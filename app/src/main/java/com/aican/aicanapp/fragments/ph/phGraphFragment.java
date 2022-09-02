package com.aican.aicanapp.fragments.ph;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aican.aicanapp.R;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.aican.aicanapp.utils.MyXAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;


public class phGraphFragment extends Fragment {

    String TABLE_NAME = "LogUserdetails";
    DatabaseHelper databaseHelper;
    LineChart lineChart;
    DatabaseReference deviceRef;
    String ph;
    Button refresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ph_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        lineChart = view.findViewById(R.id.activity_main_linechart);
        refresh = view.findViewById(R.id.btnGraphRefresh);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        fetchLogs();
        new graphBackgroundService().execute();


//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showChart();
//            }
//        }, 5000);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLogs();
                showChart();
            }
        });

    }

    public int columns() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.databaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void showChart() {
        int countColumns = columns();

        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 0; i < countColumns; i++) {
            yValues.add(new Entry(Float.parseFloat(String.valueOf(i)), Float.parseFloat(ph)));
            LineDataSet set = new LineDataSet(yValues, "pH");
            set.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);
            LineData data = new LineData(dataSets);
            lineChart.setData(data);
            lineChart.setPinchZoom(true);
            lineChart.setTouchEnabled(true);
            lineChart.invalidate();
        }

        lineChart.getDescription().setText("Tap on graph to Plot!");
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(250);
        xAxis.setLabelCount(3);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        LineDataSet set = new LineDataSet(yValues, "pH");
        set.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        lineChart.setPinchZoom(true);
        lineChart.setTouchEnabled(true);
        lineChart.callOnClick();
        lineChart.refreshDrawableState();
    }

    private void fetchLogs(){
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                ph = String.format(Locale.UK, "%.2f", p);
                showChart();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class graphBackgroundService extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            fetchLogs();
//            lineChart.callOnClick();
            return null;
        }
    }
} 