package com.aican.aicanapp.fragments.ec;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.CalibFileAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.EcActivity;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aspose.cells.FileFormatType;
import com.aspose.cells.LoadOptions;
import com.aspose.cells.PdfCompliance;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EcCalibFragment extends Fragment {
    private static final String FILE_NAME = "user_info.txt";
    private static final String FILE_NAMEE = "user_calibrate.txt";

    public static final String THREE_POINT_CALIBRATION = "three";
    public static final String FIVE_POINT_CALIBRATION = "five";
    public static final String CALIBRATION_TYPE = "calibration_type";

    LinearLayout log3, log5;

    int ec;
    Integer fault;
    String nullEntry;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, tvTimer, lastCalib;
    TextView ph1, mv1, phEdit1, ph2, mv2, phEdit2, ph3, mv3, phEdit3, ph4, mv4, phEdit4, ph5, mv5, phEdit5, dt1, dt2, dt3, dt4, dt5;
    TextView ec_bufferD1, ec_bufferD2, ec_bufferD3, ec_bufferD4, ec_bufferD5;
    TextView ec_qr1, ec_qr2, ec_qr3, ec_qr4, ec_qr5;
    DatabaseReference deviceRef;
    String MV1, MV2, MV3, MV4, MV5, PH1, PH2, PH3, PH4, PH5, DT1, DT2, DT3, DT4, DT5;
    Button calibrateBtn, btnNext, printCalib;
    Spinner spin;
    String mode;
    String test1, test2, test3;
    String mV1, mV2, mV3, mV4, mV5;
    String tm1, tm2, tm3, tm4, tm5;
    RecyclerView calibRecyclerView;

    PhView phView;
    TextView title;
    DatabaseHelper databaseHelper;
    CalibFileAdapter calibFileAdapter;
    ArrayList<BufferData> bufferList = new ArrayList<>();
    ArrayList<BufferData> bufferListThree = new ArrayList<>();

    String[] lines, liness;
    LinearLayout ll1;

    float[] buffers = new float[]{1.0F, 4.0F, 7.0F};
    String[] bufferLabels = new String[]{"B_1", "B_2", "B_3"};
    String[] coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3"};
    String[] coeffLabelsThree = new String[]{"VAL_2", "VAL_3", "VAL_4"};
    int[] calValues = new int[]{10, 20, 30, 40, 50};
    int[] calValuesThree = new int[]{20, 30, 40};
    int currentBuf = 0;
    int currentBufThree = 0;

    DatabaseReference coeffRef = null;

    float coeff = 0;
    float coeffThree = 0;
    boolean isCalibrating = false;

    LinearLayout l1, l2, l3, l4, l5;


    String currentTime;
    String strDate;


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ph2 && v.getId() == R.id.mv2) {
                ph1.setBackgroundColor(Color.LTGRAY);
                mv1.setBackgroundColor(Color.LTGRAY);
            }
        }
    };

    private void setupCoeffListener() {
        if (coeffRef != null) {
            coeffRef.removeEventListener(coeffListener);
        }
        coeffRef = deviceRef.child("UI").child("EC").child("EC_CAL").child(coeffLabels[currentBuf]);
        coeffRef.addValueEventListener(coeffListener);
    }


    private void setupListeners() {

        if (spin.getSelectedItemPosition() == 0) {

//            l4.setVisibility(View.VISIBLE);
//            l5.setVisibility(View.VISIBLE);

            l1.setVisibility(View.VISIBLE);
            l2.setVisibility(View.GONE);
            l3.setVisibility(View.GONE);
            l4.setVisibility(View.GONE);
            l5.setVisibility(View.GONE);

            deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ph = snapshot.getValue(Float.class);
                    if (ph == null) return;
                    String phForm = String.format(Locale.UK, "%.2f", ph);
                    tvPhCurr.setText(phForm);
                    //                   phView.moveTo(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float temp = snapshot.getValue(Float.class);
                    String tempForm = String.format(Locale.UK, "%.1f", temp);
                    tvTempCurr.setText(tempForm + "°C");
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("Data").child("VOLT").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ec = snapshot.getValue(Float.class);
                    String ecForm = String.format(Locale.UK, "%.1f", ec);
                    tvEcCurr.setText(ecForm);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("Data").child("VOLT").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ec = snapshot.getValue(Float.class);
                    String ecForm = String.format(Locale.UK, "%.1f", ec);
                    tvEcCurr.setText(ecForm);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });


            deviceRef.child("UI").child("EC").child("EC_CAL").child("MV_1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ec = snapshot.getValue(Float.class);
                    String ecForm = String.format(Locale.UK, "%.2f", ec);
                    mv1.setText(ecForm);
                    mV1 = mv1.getText().toString();
                    Log.d("test1", mV1);

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("MV1", mV1);
                    myEdit.commit();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("MV_2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ec = snapshot.getValue(Float.class);
                    String ecForm = String.format(Locale.UK, "%.2f", ec);
                    mv2.setText(ecForm);
                    mV2 = mv2.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("MV2", mV2);
                    myEdit.commit();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });


            deviceRef.child("UI").child("EC").child("EC_CAL").child("MV_3").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ec = snapshot.getValue(Float.class);
                    String ecForm = String.format(Locale.UK, "%.2f", ec);
                    mv3.setText(ecForm);
                    mV3 = mv3.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("MV3", mV3);
                    myEdit.commit();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("DT_1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String time = snapshot.getValue(String.class);
                    dt1.setText(time);
                    tm1 = dt1.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT1", tm1);
                    myEdit.commit();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("DT_2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String time = snapshot.getValue(String.class);
                    dt2.setText(time);
                    tm2 = dt2.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT2", tm2);
                    myEdit.commit();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("DT_3").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String time = snapshot.getValue(String.class);
                    dt3.setText(time);
                    tm3 = dt3.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT3", tm3);
                    myEdit.commit();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("B_1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String ph = snapshot.getValue(String.class);
                    ph1.setText(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
            deviceRef.child("UI").child("EC").child("EC_CAL").child("B_2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String ph = snapshot.getValue(String.class);
                    ph2.setText(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("B_3").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String ph = snapshot.getValue(String.class);
                    ph3.setText(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }


        deviceRef.child("Data").child("FAULT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                fault = snapshot.getValue(Integer.class);
                if (fault == null) return;
                if (fault == 1) {
                    showAlertDialogButtonClicked();
                } else {

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        if (spin.getSelectedItemPosition() == 1) {
            l2.setVisibility(View.VISIBLE);
            l1.setVisibility(View.GONE);
            l3.setVisibility(View.GONE);
            l4.setVisibility(View.GONE);
            l5.setVisibility(View.GONE);


            deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    ec = snapshot.getValue(Integer.class);

                    if (ec == 10 || ec == 0) {
                        l1.setBackgroundColor(Color.LTGRAY);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.WHITE);
                        l4.setBackgroundColor(Color.WHITE);
                        l5.setBackgroundColor(Color.WHITE);
                    } else if (ec == 20 || ec == 11) {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.LTGRAY);
                        l3.setBackgroundColor(Color.WHITE);
                        l4.setBackgroundColor(Color.WHITE);
                        l5.setBackgroundColor(Color.WHITE);

                    } else if (ec == 30 || ec == 21) {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.LTGRAY);
                        l4.setBackgroundColor(Color.WHITE);
                        l5.setBackgroundColor(Color.WHITE);

                    } else if (ec == 40 || ec == 31) {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.WHITE);
                        l4.setBackgroundColor(Color.LTGRAY);
                        l5.setBackgroundColor(Color.WHITE);

                    } else if (ec == 50 || ec == 41) {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.WHITE);
                        l4.setBackgroundColor(Color.WHITE);
                        l5.setBackgroundColor(Color.LTGRAY);
                    } else {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.WHITE);
                        l4.setBackgroundColor(Color.WHITE);
                        l5.setBackgroundColor(Color.WHITE);
                    }
                    if (ec == 11) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("DT_1").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 21) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("DT_2").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 31) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("DT_3").setValue(strDate);
                        calibrateBtn.setEnabled(true);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                calibrateBtn.setText("START");
                                calibrateBtn.setEnabled(true);
                                deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(0);
                            }
                        }, 10000);   //5 seconds


                    } else if (ec == 0) {
                        calibrateBtn.setText("START");
                        calibrateBtn.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }


        if (spin.getSelectedItemPosition() == 2) {
            l3.setVisibility(View.VISIBLE);
            l1.setVisibility(View.GONE);
            l2.setVisibility(View.GONE);
            l4.setVisibility(View.GONE);
            l5.setVisibility(View.GONE);
        }
    }


    public void showAlertDialogButtonClicked() {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(requireContext());
        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.fault_dialog,
                        null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                    }
                });

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ec_calib, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), EcActivity.DEVICE_ID, Toast.LENGTH_SHORT).show();

        btnNext = view.findViewById(R.id.nextBtn);
        printCalib = view.findViewById(R.id.printCalibData);
        phView = view.findViewById(R.id.phView);
        ph1 = view.findViewById(R.id.ph1);
        mv1 = view.findViewById(R.id.mv1);
        ph2 = view.findViewById(R.id.ph2);
        mv2 = view.findViewById(R.id.mv2);
        ph3 = view.findViewById(R.id.ph3);
        mv3 = view.findViewById(R.id.mv3);
        ph4 = view.findViewById(R.id.ph4);
        mv4 = view.findViewById(R.id.mv4);
        ph5 = view.findViewById(R.id.ph5);
        mv5 = view.findViewById(R.id.mv5);

        log3 = view.findViewById(R.id.log3point);
        log5 = view.findViewById(R.id.log5Point);

        phEdit2 = view.findViewById(R.id.phEdit2);
        phEdit3 = view.findViewById(R.id.phEdit3);
        phEdit4 = view.findViewById(R.id.phEdit4);
        phEdit5 = view.findViewById(R.id.phEdit5);

        dt1 = view.findViewById(R.id.dt1);
        dt2 = view.findViewById(R.id.dt2);
        dt3 = view.findViewById(R.id.dt3);
        dt4 = view.findViewById(R.id.dt4);
        dt5 = view.findViewById(R.id.dt5);

        ec_bufferD1 = view.findViewById(R.id.ec_bufferD1);
        ec_bufferD2 = view.findViewById(R.id.ec_bufferD2);
        ec_bufferD3 = view.findViewById(R.id.ec_bufferD3);
        ec_bufferD4 = view.findViewById(R.id.ec_bufferD4);
        ec_bufferD5 = view.findViewById(R.id.ec_bufferD5);


        ec_qr1 = view.findViewById(R.id.ec_qr1);
        ec_qr2 = view.findViewById(R.id.ec_qr2);
        ec_qr3 = view.findViewById(R.id.ec_qr3);
        ec_qr4 = view.findViewById(R.id.ec_qr4);
        ec_qr5 = view.findViewById(R.id.ec_qr5);

        title = view.findViewById(R.id.tvTitle);
        l1 = view.findViewById(R.id.log1);
        l2 = view.findViewById(R.id.log2);
        l3 = view.findViewById(R.id.log3);
        l4 = view.findViewById(R.id.log4);
        l5 = view.findViewById(R.id.log5);

        ll1 = view.findViewById(R.id.ll1);
        phEdit1 = view.findViewById(R.id.phEdit1);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);
        tvEcCurr = view.findViewById(R.id.tvEcCurr);
        calibrateBtn = view.findViewById(R.id.startBtn);
        spin = view.findViewById(R.id.calibMode);
        calibRecyclerView = view.findViewById(R.id.rvCalibFileView);
        nullEntry = "";


        databaseHelper = new DatabaseHelper(requireContext());


        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput(FILE_NAMEE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            liness = sb.toString().split("\\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        title.setText("Do not exit/change fragments \nwhile calibrating");


        String[] spinselect = {"0.1K", "1K", "10K"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinselect);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        SharedPreferences spinPref = getContext().getSharedPreferences("spinnerPref", MODE_PRIVATE);
        int spinnerValue = spinPref.getInt("userChoiceSpinner", -1);
        if (spinnerValue != -1) {
            spin.setSelection(spinnerValue);
        }

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:

                        log3.setVisibility(View.VISIBLE);
                        log5.setVisibility(View.GONE);
                        mode = "0.1K";

                        buffers = new float[]{2.0F};
                        bufferLabels = new String[]{"B_1"};
                        coeffLabels = new String[]{"VAL_1"};
                        calValues = new int[]{10};

                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);
                        setupCoeffListener();
                        setupListeners();
                        //loadBuffers();

//                        deleteAllCalibData();
                        calibData();

//                        databaseHelper.insertCalibration(PH1, MV1, DT1, "");
//                        databaseHelper.insertCalibration(PH2, MV2, DT2, "");
//                        databaseHelper.insertCalibration(PH3, MV3, DT3, "");
//                        databaseHelper.insertCalibration(PH4, MV4, DT4, "");
//                        databaseHelper.insertCalibration(PH5, MV5, DT5, "");


                        deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL_MODE").setValue(2);

                        int userChoice = spin.getSelectedItemPosition();
                        SharedPreferences sharedPrefs = getContext().getSharedPreferences("spinnerPref", 0);
                        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
                        prefEditor.putInt("userChoiceSpinner", userChoice);
                        prefEditor.commit();

                        break;

                    case 1:
                        mode = "1K";

                        log3.setVisibility(View.VISIBLE);
                        log5.setVisibility(View.GONE);

                        buffers = new float[]{4.0F};
                        bufferLabels = new String[]{"B_2"};
                        coeffLabels = new String[]{"VAL_2"};
                        calValues = new int[]{20};

                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);
                        setupCoeffListener();
                        setupListeners();
                        //loadBuffers();

//                        deleteAllCalibData();
                        calibData();

//                        databaseHelper.insertCalibration(PH2, MV2, DT2, "");
//                        databaseHelper.insertCalibration(PH3, MV3, DT3, "");
//                        databaseHelper.insertCalibration(PH4, MV4, DT4, "");

                        deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL_MODE").setValue(1);

                        int userChoice1 = spin.getSelectedItemPosition();
                        SharedPreferences sharedPrefs1 = getContext().getSharedPreferences("spinnerPref", 0);
                        SharedPreferences.Editor prefEditor1 = sharedPrefs1.edit();
                        prefEditor1.putInt("userChoiceSpinner", userChoice1);
                        prefEditor1.commit();

                        break;
                    case 2:
                        mode = "10K";
                        buffers = new float[]{7.0F};
                        bufferLabels = new String[]{"B_2"};
                        coeffLabels = new String[]{"VAL_2"};
                        calValues = new int[]{30};

                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);
                        setupCoeffListener();
                        setupListeners();
                        //loadBuffers();

//                        deleteAllCalibData();
                        calibData();
                        int userChoice2 = spin.getSelectedItemPosition();
                        SharedPreferences sharedPrefs2 = getContext().getSharedPreferences("spinnerPref", 0);
                        SharedPreferences.Editor prefEditor2 = sharedPrefs2.edit();
                        prefEditor2.putInt("userChoiceSpinner", userChoice2);
                        prefEditor2.commit();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(requireContext(), "Select a mode of callibration", Toast.LENGTH_SHORT).show();
            }
        });

        if (spin.getSelectedItemPosition() == 0) {
            phEdit1.setOnClickListener(this::onClick);
//            phEdit2.setOnClickListener(this::onClick);
//            phEdit3.setOnClickListener(this::onClick);
//            phEdit4.setOnClickListener(this::onClick);
//            phEdit5.setOnClickListener(this::onClick);
        } else if (spin.getSelectedItemPosition() == 1) {
//            phEdit1.setOnClickListener(this::onClick);
            phEdit2.setOnClickListener(this::onClick);
//            phEdit3.setOnClickListener(this::onClick);
        } else if (spin.getSelectedItemPosition() == 2) {
//            phEdit1.setOnClickListener(this::onClick);
//            phEdit2.setOnClickListener(this::onClick);
            phEdit3.setOnClickListener(this::onClick);
        }


        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FileOutputStream fos = null;

                try {
                    fos = getContext().openFileOutput(FILE_NAMEE, MODE_PRIVATE);
                    Source.userName = "default";
                    fos.write(Source.userName.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                FileInputStream fis = null;
                try {
                    fis = getActivity().openFileInput(FILE_NAME);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;

                    while ((text = br.readLine()) != null) {
                        sb.append(text).append("\n");
                    }
                    lines = sb.toString().split("\\n");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (mode.equals(3)) {
                    calibrate();
                } else {
                    calibrate();
                }
            }
        });


        test3 = mv2.getText().toString();

        printCalib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCalibData();

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData";
                File root = new File(path);
                File[] filesAndFolders = root.listFiles();

                if (filesAndFolders == null || filesAndFolders.length == 0) {

                    return;
                } else {
                    for (int i = 0; i < filesAndFolders.length; i++) {
                        filesAndFolders[i].getName().endsWith(".csv");
                    }
                }


                try {
                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData/CalibrationData.xlsx");

                    PdfSaveOptions options = new PdfSaveOptions();
                    options.setCompliance(PdfCompliance.PDF_A_1_B);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData/CalibrationData" + currentDateandTime + ".pdf", options);

                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData";
                    File root1 = new File(path1);
                    fileNotWrite(root1);
                    File[] filesAndFolders1 = root1.listFiles();

                    if (filesAndFolders1 == null || filesAndFolders1.length == 0) {

                        return;
                    } else {
                        for (int i = 0; i < filesAndFolders1.length; i++) {
                            if (filesAndFolders1[i].getName().endsWith(".csv") || filesAndFolders1[i].getName().endsWith(".xlsx")) {
                                filesAndFolders1[i].delete();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(root);
                File[] filesAndFoldersPDF = rootPDF.listFiles();


                calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), filesAndFoldersPDF);
                calibRecyclerView.setAdapter(calibFileAdapter);
                calibFileAdapter.notifyDataSetChanged();
                calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            Toast.makeText(requireContext(), "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                filesAndFolders[i].getName().endsWith(".csv");
            }
        }

        calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), filesAndFolders);
        calibRecyclerView.setAdapter(calibFileAdapter);
        calibFileAdapter.notifyDataSetChanged();
        calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

    }

    public void calibData() {
        SharedPreferences shp = getContext().getSharedPreferences("ECCalibPrefs", MODE_PRIVATE);

        if (spin.getSelectedItemPosition() == 0) {

            MV1 = shp.getString("MV1", "");


            DT1 = shp.getString("DT1", "");


            PH1 = "1.2";
            PH2 = "4.0";
            PH3 = "7.0";
            PH4 = "9.2";
            PH5 = "12.0";
        } else if (spin.getSelectedItemPosition() == 1) {


            MV2 = shp.getString("MV2", "");

            DT2 = shp.getString("DT2", "");


            PH2 = "4.0";
            PH3 = "7.0";
            PH4 = "9.2";

        }


    }

    public void fileNotWrite(File file) {
        file.setWritable(false);
        if (file.canWrite()) {
            Log.d("csv", "Nhi kaam kar rha");
        } else {
            Log.d("csvnw", "Party Bhaiiiii");
        }
    }


    public void deleteAllCalibData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM CalibData");
        db.close();
    }

    public void exportCalibData() {


        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {

            file = new File(exportDir, "CalibrationData.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);


            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);

            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Callibration Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("pH,mV,DATE");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));

                String record1 = ph + "," + mv + "," + date;

                printWriter.println(record1);
            }
            calibCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);

            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData/";
            Workbook workbook = new Workbook(inputFile + "CalibrationData.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 18.5);
            worksheet.getCells().setColumnWidth(2, 18.5);
            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECCalibrationData/CalibrationData.xlsx", SaveFormat.XLSX);


        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
        }

    }


    private void onClick(View v) {

        if (spin.getSelectedItemPosition() == 0) {
            switch (v.getId()) {
                case R.id.phEdit1:
                    EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
                        updateBufferValue(ph);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("B_1").setValue(String.valueOf(ph));
                    });
                    dialog.show(getParentFragmentManager(), null);
                    break;

                default:
                    break;
            }
        } else if (spin.getSelectedItemPosition() == 1) {
            switch (v.getId()) {

                case R.id.phEdit2:

                    EditPhBufferDialog dialog1 = new EditPhBufferDialog(ph -> {
                        updateBufferValue(ph);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("B_2").setValue(String.valueOf(ph));
                    });
                    dialog1.show(getParentFragmentManager(), null);
                    break;

                default:
                    break;
            }
        } else if (spin.getSelectedItemPosition() == 2) {
            switch (v.getId()) {

                case R.id.phEdit3:

                    EditPhBufferDialog dialog2 = new EditPhBufferDialog(ph -> {
                        updateBufferValue(ph);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("B_3").setValue(String.valueOf(ph));
                    });
                    dialog2.show(getParentFragmentManager(), null);
                    break;

                default:
                    break;
            }

        }

    }


    private void updateBufferValue(Float value) {
        String newValue = String.valueOf(value);
    }

    public void calibrate() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        databaseHelper.insert_action_data(date + " " + time, "Calibrated by " + Source.logUserName, "", "", "", "", EcActivity.DEVICE_ID);
        calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtn.setEnabled(false);
        tvTimer.setVisibility(View.VISIBLE);
        isCalibrating = true;
        setupCoeffListener();
        CountDownTimer timer = new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimer.setText(time);
            }

            final Handler handler = new Handler();
            Runnable runnable;

            @Override
            public void onFinish() {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvTimer.setVisibility(View.INVISIBLE);
                        currentTime = new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault()).format(new Date());
                        bufferList.add(new BufferData(null, null, currentTime));
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                        deviceRef.child("UI").child("EC").child("EC_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                            Float coeff = dataSnapshot.getValue(Float.class);
                            if (coeff == null) return;
                            currentBuf += 1;
                        });
                    }
                };
                runnable.run();
            }
        };

        deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
            timer.start();
        });
    }


    ValueEventListener coeffListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
            Float coeff = snapshot.getValue(Float.class);
            if (coeff == null) return;
            EcCalibFragment.this.coeff = coeff;
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {
        }
    };


//    @Override
//    public void onBackPressed() {
//        if (isCalibrating) {
//            ExitConfirmDialog dialogs = new ExitConfirmDialog((new ExitConfirmDialog.DialogCallbacks() {
//                @Override
//                public void onYesClicked(Dialog dialogs) {
//                    deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
//                    dialogs.dismiss();
//                    isCalibrating = false;
//                }
//
//                @Override
//                public void onNoClicked(Dialog dialogs) {
//                    dialogs.dismiss();
//                }
//            }));
//
//            dialogs.show(getParentFragmentManager(), null);
//        } else {
//            getActivity().getSupportFragmentManager().popBackStack();
//        }
//    }
}