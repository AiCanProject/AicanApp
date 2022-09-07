package com.aican.aicanapp.fragments.temp;


import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.specificactivities.TemperatureActivity;
import com.aican.aicanapp.utils.MyXAxisValueFormatter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TempFragment extends Fragment {

    DatabaseHelper databaseHelper;
    LineChart lineChart;
    DatabaseReference deviceRef;
    String ph;
    float temp = 0;
    Button refresh, btnGraphCancel;
    LineDataSet lineDataSet = new LineDataSet(null, "TEMP1");
    LineDataSet lineDataSet2 = new LineDataSet(null, "TEMP2");
    ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    LineData lineData;
    TextView tvTempCurr, tvTempNext;
    Spinner graphInterval;
    CountDownTimer countDownTimer;
    public static String intervalSelected = "5 sec";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public TempFragment() {
        // Required empty public constructor
    }

    public static TempFragment newInstance(String param1, String param2) {
        TempFragment fragment = new TempFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temp_main, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.graph);
        refresh = view.findViewById(R.id.btnGraphRefresh);
        btnGraphCancel = view.findViewById(R.id.btnGraphCancel);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);
        graphInterval = view.findViewById(R.id.graphInterval);
        btnGraphCancel.setEnabled(false);
        spinnerAction();


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(TemperatureActivity.DEVICE_ID)).getReference().child(TemperatureActivity.deviceType).child(TemperatureActivity.DEVICE_ID);


        deviceRef.child("Data").child("TEMP2_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;
                updateTemp(temp);
                TempFragment.this.temp = temp;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        String valURL1 = "https://labdevices-8c9a5-default-rtdb.asia-southeast1.firebasedatabase.app/" + TemperatureActivity.deviceType + "/" + TemperatureActivity.DEVICE_ID + "/Data/.json";

        fetchData(valURL1);
        float fe = temp2_val;
        Toast.makeText(getContext(), "" + fe, Toast.LENGTH_SHORT).show();

        lineDataSet.setCircleColor(getResources().getColor(R.color.temp1));
        lineDataSet2.setCircleColor(getResources().getColor(R.color.temp2));

        lineDataSet.setHighLightColor(getResources().getColor(R.color.temp1));
        lineDataSet2.setHighLightColor(getResources().getColor(R.color.temp2));

//        lineDataSet2.setLabel("TEMP2");
//        lineDataSet.setLabel("TEMP1");

        spinnerSelected();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fetchLogs();
                if (intervalSelected.equals("5 sec")) {
                    graphShow(5, 5);
                }
                if (intervalSelected.equals("10 sec")) {
                    graphShow(10, 10);
                }
                if (intervalSelected.equals("30 sec")) {
                    graphShow(30, 30);
                }
                if (intervalSelected.equals("1 min")) {
                    graphShow(30, 60);
                }
                if (intervalSelected.equals("3 min")) {
                    graphShow(40, 180);
                }
                if (intervalSelected.equals("5 min")) {
                    graphShow(50, 300);
                }
                if (intervalSelected.equals("10 min")) {
                    graphShow(60, 600);
                }
            }
        });

        btnGraphCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                btnGraphCancel.setEnabled(false);
                refresh.setEnabled(true);
            }
        });

    }

    public void spinnerSelected() {
        graphInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    intervalSelected = "5 sec";
                }
                if (position == 1) {
                    intervalSelected = "10 sec";
                }
                if (position == 2) {
                    intervalSelected = "30 sec";
                }
                if (position == 3) {
                    intervalSelected = "1 min";
                }
                if (position == 4) {
                    intervalSelected = "3 min";
                }
                if (position == 5) {
                    intervalSelected = "5 min";
                }
                if (position == 6) {
                    intervalSelected = "10 min";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                intervalSelected = "5 sec";

            }
        });
    }

    private void spinnerAction() {
        // 30, 1 min , 3 min , 5 , 10

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("5 sec");
        categories.add("10 sec");
        categories.add("30 sec");
        categories.add("1 min");
        categories.add("3 min");
        categories.add("5 min");
        categories.add("10 min");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        graphInterval.setAdapter(dataAdapter);
    }

    public static float temp1_val;
    public static float temp2_val;

    public static boolean start = false;

    public void graphShow(long totalMin, long interval) {

        lineChart.clear();
        lineChart.invalidate();

        ArrayList<Entry> information = new ArrayList<>();
        ArrayList<Entry> information2 = new ArrayList<>();
        float ecValue;
        int[] times = {0};
        btnGraphCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                btnGraphCancel.setEnabled(false);
                refresh.setEnabled(true);
            }
        });
        String valURL = "https://labdevices-8c9a5-default-rtdb.asia-southeast1.firebasedatabase.app/" + TemperatureActivity.deviceType + "/" + TemperatureActivity.DEVICE_ID + "/Data/.json";

        countDownTimer = new CountDownTimer(totalMin * 60 * 1000, interval * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {


                start = true;

                refresh.setEnabled(false);
                btnGraphCancel.setEnabled(true);
                fetchData(valURL);
//                Toast.makeText(getContext(), "" + getConductivity, Toast.LENGTH_SHORT).show();
                if (temp1_val != -10000) {
                    information.add(new Entry(times[0], temp1_val));
                    information2.add(new Entry(times[0], temp2_val));
                    times[0] = (int) (times[0] + interval);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showChart(information, information2);

                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lineChart.clear();
                            lineChart.invalidate();
                            countDownTimer.cancel();
                        }
                    });


                }

            }

            @Override
            public void onFinish() {
                refresh.setEnabled(true);
                btnGraphCancel.setEnabled(false);
            }
        };

        countDownTimer.start();

    }

    @Override
    public void onPause() {
        super.onPause();
        lineChart.clear();
        lineChart.invalidate();
    }

    public void fetchData(String myUrl) {


        RequestQueue requestQueue = Volley.newRequestQueue(getContext());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, myUrl, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("TEMP2_VAL", response.get("TEMP2_VAL").toString());
                    String temp1_val1 = response.get("TEMP1_VAL").toString();
                    String temp2_val2 = response.get("TEMP2_VAL").toString();
                    temp1_val = Float.parseFloat(temp1_val1);
                    temp2_val = Float.parseFloat(temp2_val2);


//                    Toast.makeText(getContext(), "" + response.getLong("EC_VAL"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error" + e, Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        requestQueue.add(jsonObjectRequest);


    }


    public void showChart(ArrayList<Entry> dataVal, ArrayList<Entry> dataVal2) {
        lineDataSet.setValues(dataVal);
        lineDataSet2.setValues(dataVal2);
        iLineDataSets.add(lineDataSet);
        iLineDataSets.add(lineDataSet2);
        lineData = new LineData(iLineDataSets);
        lineChart.clear();
        lineChart.setData(lineData);
        lineChart.invalidate();

    }

    private void updateTemp(int temp) {
        String newText;
        if (temp < -50 || temp > 125) {
            newText = "--";
        } else {
            newText = String.format(Locale.UK, "%d°C", temp);
        }
        tvTempNext.setText(newText);

        if (getContext() != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
            Animation slideInBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvTempCurr.setVisibility(View.INVISIBLE);
                    TextView t = tvTempCurr;
                    tvTempCurr = tvTempNext;
                    tvTempNext = t;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            tvTempCurr.startAnimation(fadeOut);
            tvTempNext.setVisibility(View.VISIBLE);
            tvTempNext.startAnimation(slideInBottom);
        } else {
            tvTempCurr.setText(newText);
        }
    }
}