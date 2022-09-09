package com.aican.aicanapp.fragments.ec;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.aican.aicanapp.specificactivities.EcActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
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


public class EcGraphFragment extends Fragment {

    DatabaseHelper databaseHelper;
    LineChart lineChart;
    DatabaseReference deviceRef;
    String ph;
    Button refresh, btnGraphCancel;
    LineDataSet lineDataSet = new LineDataSet(null, null);
    ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    LineData lineData;
    TextView tvGraphTemp, tvGraphConductivity;
    Spinner graphInterval;
    CountDownTimer countDownTimer;
    public static String intervalSelected = "5 sec";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public EcGraphFragment() {
        // Required empty public constructor
    }

    public static EcGraphFragment newInstance(String param1, String param2) {
        EcGraphFragment fragment = new EcGraphFragment();
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
        return inflater.inflate(R.layout.fragment_ec_graph, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.graph);
        refresh = view.findViewById(R.id.btnGraphRefresh);
        btnGraphCancel = view.findViewById(R.id.btnGraphCancel);
        tvGraphTemp = view.findViewById(R.id.tvGraphTemp);
        tvGraphConductivity = view.findViewById(R.id.tvGraphConductivity);
        graphInterval = view.findViewById(R.id.graphInterval);
        btnGraphCancel.setEnabled(false);
        spinnerAction();


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);

        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if (ph == null) return;
                //phView.moveTo(ph);
                String phForm = String.format(Locale.UK, "%.2f", ph);
                tvGraphConductivity.setText(phForm);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String tempp = snapshot.getValue(Integer.class).toString();
                tvGraphTemp.setText(tempp + "°C");
                Float temp = snapshot.getValue(Float.class);
                String tempForm = String.format(Locale.UK, "%.1f", temp);
                tvGraphTemp.setText(tempForm + "°C");

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        String valURL1 = "https://labdevices-8c9a5-default-rtdb.asia-southeast1.firebasedatabase.app/ECMETER/" + EcActivity.DEVICE_ID + "/Data/.json";

        float fe = fetchData(valURL1);
        Toast.makeText(getContext(), "" + fe, Toast.LENGTH_SHORT).show();

        lineDataSet.setLabel("Data");
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
//                if (adapterView.getPositionForView(view) == 0) {
//                    intervalSelected = "5 sec";
//                }
//                if (adapterView.getPositionForView(view) == 1) {
//                    intervalSelected = "10 sec";
//                }
//                if (adapterView.getPositionForView(view) == 2) {
//                    intervalSelected = "30 sec";
//                }
//                if (adapterView.getPositionForView(view) == 3) {
//                    intervalSelected = "1 min";
//                }
//                if (adapterView.getPositionForView(view) == 4) {
//                    intervalSelected = "3 min";
//                }
//                if (adapterView.getPositionForView(view) == 5) {
//                    intervalSelected = "5 min";
//                }
//                if (adapterView.getPositionForView(view) == 6) {
//                    intervalSelected = "10 min";
//                }
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

    public static float ec_val;

    public static boolean start = false;

    public void graphShow(long totalMin, long interval) {

        lineChart.clear();
        lineChart.invalidate();

        ArrayList<Entry> information = new ArrayList<>();
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
        String valURL = "https://labdevices-8c9a5-default-rtdb.asia-southeast1.firebasedatabase.app/ECMETER/" + EcActivity.DEVICE_ID + "/Data/.json";

        countDownTimer = new CountDownTimer(totalMin * 60 * 1000, interval * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;

                start = true;

                refresh.setEnabled(false);
                btnGraphCancel.setEnabled(true);
                ec_val = fetchData(valURL);
//                Toast.makeText(getContext(), "" + getConductivity, Toast.LENGTH_SHORT).show();
                if (ec_val != -100) {
                    information.add(new Entry(times[0], ec_val));
                    times[0] = (int) (times[0] + interval);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showChart(information);

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

    public float fetchData(String myUrl) {


        RequestQueue requestQueue = Volley.newRequestQueue(getContext());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, myUrl, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("EC_VAL", response.get("EC_VAL").toString());
                    String strEC = response.get("EC_VAL").toString();
                    ec_val = Float.parseFloat(strEC);


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

        return ec_val;

    }


    public void showChart(ArrayList<Entry> dataVal) {
        lineDataSet.setValues(dataVal);
        iLineDataSets.add(lineDataSet);
        lineData = new LineData(iLineDataSets);
        lineChart.clear();
        lineChart.setData(lineData);
        lineChart.invalidate();

    }

}