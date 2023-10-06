package com.aican.aicanapp.fragments.ph;

import static android.content.Context.MODE_PRIVATE;
import static com.aican.aicanapp.utils.Constants.SERVER_PATH;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.ProbeScan.ProbeScanner;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.CalibFileAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.dataClasses.CalibDatClass;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PHCalibGraph;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.PhMvTable;
import com.aican.aicanapp.utils.AlarmConstants;
import com.aican.aicanapp.utils.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class PhCalibFragmentNew extends Fragment {

    private static float LOG_INTERVAL = 0;
    private static float LOG_INTERVAL_3 = 0;
    Handler handler1;
    Runnable runnable1;
    Button syncOfflineData;

    Handler handler2;
    Runnable runnable2;
    Integer fault;

    WebSocket webSocket1;
    JSONObject jsonData;
    int ec;
    String strDate;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    boolean isCalibrating = false;
    String deviceID = "";
    String companyName;
    String nullEntry, reportDate, reportTime;
    String offset, battery, slope, temp;
    String calib_stat = "incomplete";
    ProgressDialog progressDialog;
    PhView phView;
    TextView tvEcCurr;

    String mode;
    DatabaseHelper databaseHelper;

    int[] calValues = new int[]{10, 20, 30, 40, 50};
    int[] calValuesThree = new int[]{20, 30, 40};
    int currentBuf = 0;
    int currentBufThree = 0;

    ArrayList<BufferData> bufferList = new ArrayList<>();
    ArrayList<BufferData> bufferListThree = new ArrayList<>();
    public static String PH_MODE = "both";

    LinearLayout fivePointCalibStart, threePointCalibStart;

    LinearLayout log1, log2, log3, log4, log5;
    LinearLayout log1_3, log2_3, log3_3;
    LinearLayout log3point, log5point;
    LinearLayout calibSpinner;
    Spinner spin;
    TextView tvTimer, tvTimerThree, tvTempCurr, tvPhCurr;
    TextView modeText;
    Button calibrateBtn, calibrateBtnThree, printCalibData, phMvTable, phGraph, printAllCalibData;
    TextView ph1, ph2, ph3, ph4, ph5;
    TextView phAfterCalib1, phAfterCalib2, phAfterCalib3, phAfterCalib4, phAfterCalib5;
    TextView slope1, slope2, slope3, slope4, slope5;
    TextView temp1, temp2, temp3, temp4, temp5;
    TextView mv1, mv2, mv3, mv4, mv5;
    TextView dt1, dt2, dt3, dt4, dt5;
    TextView bufferD1, bufferD2, bufferD3, bufferD4, bufferD5;
    TextView phEdit1, phEdit2, phEdit3, phEdit4, phEdit5;
    TextView qr1, qr2, qr3, qr4, qr5;

    TextView ph1_3, ph2_3, ph3_3;
    TextView phAfterCalib1_3, phAfterCalib2_3, phAfterCalib3_3;
    TextView slope1_3, slope2_3, slope3_3;
    TextView temp1_3, temp2_3, temp3_3;
    TextView mv1_3, mv2_3, mv3_3;
    TextView dt1_3, dt2_3, dt3_3;
    TextView bufferD1_3, bufferD2_3, bufferD3_3;
    TextView phEdit1_3, phEdit2_3, phEdit3_3;
    TextView qr1_3, qr2_3, qr3_3;

    RecyclerView calibRecyclerView;
    CalibFileAdapter calibFileAdapter;

    float minMV1, minMV2, minMV3, minMV4, minMV5;
    float maxMV1, maxMV2, maxMV3, maxMV4, maxMV5;

    float minMV1_3, minMV2_3, minMV3_3;
    float maxMV1_3, maxMV2_3, maxMV3_3;

    String MV1, MV2, MV3, MV4, MV5;
    String PH1, PH2, PH3, PH4, PH5;
    String DT1, DT2, DT3, DT4, DT5;
    String BFD1, BFD2, BFD3, BFD4, BFD5;
    String t1, t2, t3, t4, t5;
    String pHAC1, pHAC2, pHAC3, pHAC4, pHAC5;
    String mV1, mV2, mV3, mV4, mV5;
    String SLOPE1, SLOPE2, SLOPE3, SLOPE4, SLOPE5;

    String MV1_3, MV2_3, MV3_3;
    String PH1_3, PH2_3, PH3_3;
    String DT1_3, DT2_3, DT3_3;
    String BFD1_3, BFD2_3, BFD3_3;
    String t1_3, t2_3, t3_3;
    String pHAC1_3, pHAC2_3, pHAC3_3;
    String mV1_3, mV2_3, mV3_3;
    String SLOPE1_3, SLOPE2_3, SLOPE3_3;

    CardView fivePointCalib, threePointCalib;

    DatabaseReference deviceRef;
    String[] bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
    String[] bufferLabelsThree = new String[]{"B_2", "B_3", "B_4"};
    String[] coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
    String[] postCoeffLabels = new String[]{"POST_VAL_1", "POST_VAL_2", "POST_VAL_3", "POST_VAL_4", "POST_VAL_5"};
    String[] postCoeffLabelsThree = new String[]{"POST_VAL_2", "POST_VAL_3", "POST_VAL_4"};
    String[] coeffLabelsThree = new String[]{"VAL_2", "VAL_3", "VAL_4"};

    public PhCalibFragmentNew() {
        // Required empty public constructor
    }

    public static PhCalibFragmentNew newInstance(String param1, String param2) {
        PhCalibFragmentNew fragment = new PhCalibFragmentNew();
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
        return inflater.inflate(R.layout.fragment_ph_calib_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeAllViews(view);

        databaseHelper = new DatabaseHelper(requireContext());

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        jsonData = new JSONObject();
        if (!Constants.OFFLINE_MODE) {
            fetchAllDataFromFirebase();
            fetchAllData5Point();
            fetchAllData3Point();
            getAllMvData();
        } else {
//            tvPhCurr.setText("");
//            tvTempCurr.setText("");
//            tvEcCurr.setText("");
//            offsetCurr.setText("");
//            batteryCurr.setText("");
//            slopeCurr.setText("");

//            initiateSocketConnection();

        }


        deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    companyName = snapshot.getValue(String.class);
                } else {
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").setValue("NA");
                    companyName = "NA";
                }
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });
        databaseHelper = new DatabaseHelper(requireContext());

        Cursor res = databaseHelper.get_data();
        while (res.moveToNext()) {
            Source.userName = res.getString(0);
        }

        if (deviceRef.child("Data").child("CALIBRATION_STAT") != null)
            deviceRef.child("Data").child("CALIBRATION_STAT").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    calib_stat = snapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        String[] spinselect = {"5", "3"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinselect);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        fivePointCalib.setVisibility(View.VISIBLE);
        fivePointCalibStart.setVisibility(View.VISIBLE);
        threePointCalib.setVisibility(View.GONE);
        threePointCalibStart.setVisibility(View.GONE);

        switch (PH_MODE) {
            case "both":
                calibSpinner.setVisibility(View.VISIBLE);
                spin.setSelection(0);
                break;
            case "5":
                calibSpinner.setVisibility(View.INVISIBLE);
                modeText.setText("Mode : 5 Point");
                spin.setSelection(0);
                break;
            case "3":
                calibSpinner.setVisibility(View.INVISIBLE);
                modeText.setText("Mode : 3 Point");
                spin.setSelection(1);
                break;
        }

        phGraph.setEnabled(true);
        phMvTable.setEnabled(true);
        printCalibData.setEnabled(true);
        calibSpinner.setEnabled(true);
        spin.setEnabled(true);

        DialogMain dialogMain = new DialogMain();
        dialogMain.setCancelable(false);
        Source.userTrack = "PhCalibPage logged : ";
        if (Source.subscription.equals("cfr")) {


            dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
        }

//        if (Constants.OFFLINE_MODE){
//            calibrateBtn.setEnabled(false);
//            progressDialog = new ProgressDialog(getContext());
//            progressDialog.setMessage("Establizing socket connection");
//            progressDialog.setCancelable(false);
//            progressDialog.setMax(100);
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.show();
//
//        }else {
//            calibrateBtn.setEnabled(true);
//
//        }

        calibrateBtn.setOnClickListener(v -> {
            if (Constants.OFFLINE_MODE) {
                calibrateFivePointOffline(webSocket1);
            } else {
                calibrateFivePoint();
            }
        });

        calibrateBtnThree.setOnClickListener(v ->

        {
            if (Constants.OFFLINE_MODE) {
                calibrateThreePointOffline();
            } else {
                calibrateThreePoint();
            }
        });

        phMvTable.setOnClickListener(v ->

        {
            Source.status_phMvTable = true;

            SharedPreferences sh = getContext().getSharedPreferences("RolePref", MODE_PRIVATE);
            SharedPreferences.Editor roleE = sh.edit();
            String roleSuper = Source.logUserName;
            roleE.putString("roleSuper", roleSuper);
            roleE.commit();


            if (Source.subscription.equals("cfr")) {
//                    DialogMain dialogMain = new DialogMain();
//                    dialogMain.setCancelable(false);
//                    Source.userTrack = "PhLogFragment logged in by ";
                dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
            } else {
                Intent intent = new Intent(getContext(), PhMvTable.class);
                startActivity(intent);
            }
        });

        printCalibData.setOnClickListener(v ->

        {
            try {
                generatePDF();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
//                exportCalibData();

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
            File root = new File(path);
            File[] filesAndFolders = root.listFiles();

            if (filesAndFolders == null || filesAndFolders.length == 0) {

                return;
            } else {
                for (int i = 0; i < filesAndFolders.length; i++) {
                    filesAndFolders[i].getName().endsWith(".pdf");
                }
            }

            String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
            File rootPDF = new File(pathPDF);
            fileNotWrite(root);
            File[] filesAndFoldersPDF = rootPDF.listFiles();


            calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), reverseFileArray(filesAndFoldersPDF));
            calibRecyclerView.setAdapter(calibFileAdapter);
            calibFileAdapter.notifyDataSetChanged();
            calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));
        });

        printAllCalibData.setOnClickListener(v ->

        {
            try {
                generateAllPDF();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
//                exportCalibData();

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
            File root = new File(path);
            File[] filesAndFolders = root.listFiles();

            if (filesAndFolders == null || filesAndFolders.length == 0) {

                return;
            } else {
                for (int i = 0; i < filesAndFolders.length; i++) {
                    filesAndFolders[i].getName().endsWith(".pdf");
                }
            }

            String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
            File rootPDF = new File(pathPDF);
            fileNotWrite(root);
            File[] filesAndFoldersPDF = rootPDF.listFiles();


            calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), reverseFileArray(filesAndFoldersPDF));
            calibRecyclerView.setAdapter(calibFileAdapter);
            calibFileAdapter.notifyDataSetChanged();
            calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));
        });

        phGraph.setOnClickListener(v ->

        {
            if (!PH1.equals("") || !PH2.equals("") || !PH3.equals("") || !PH4.equals("") || !PH5.equals("")
                    || !MV1.equals("") || !MV2.equals("") || !MV3.equals("") || !MV4.equals("") || !MV5.equals("")
            ) {
                Intent i = new Intent(getContext(), PHCalibGraph.class);
                i.putExtra("PH1", PH1);
                i.putExtra("PH2", PH2);
                i.putExtra("PH3", PH3);
                i.putExtra("PH4", PH4);
                i.putExtra("PH5", PH5);

                i.putExtra("MV1", MV1);
                i.putExtra("MV2", MV2);
                i.putExtra("MV3", MV3);
                i.putExtra("MV4", MV4);
                i.putExtra("MV5", MV5);
                startActivity(i);
            } else {
                Toast.makeText(getContext(), "Not allow to move further because some values are null, and null values cannot plot the graph", Toast.LENGTH_LONG).show();
            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        calibFileAdapter = new

                CalibFileAdapter(requireContext().

                getApplicationContext(), reverseFileArray(filesAndFolders != null ? filesAndFolders : new File[0]));
        calibRecyclerView.setAdapter(calibFileAdapter);
        calibFileAdapter.notifyDataSetChanged();
        calibRecyclerView.setLayoutManager(new

                LinearLayoutManager(requireContext().

                getApplicationContext()));

        if (deviceRef.child("Data").child("CALIBRATION_STAT") != null)
            deviceRef.child("Data").child("CALIBRATION_STAT").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    calib_stat = snapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


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
                "Continue Calibration",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        Source.calibratingNow = false;
                        phMvTable.setEnabled(true);
                    }
                });

        builder.setNeutralButton(
                "Restart",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        Source.calibratingNow = false;
                        Intent i = new Intent(requireContext(), PhActivity.class);
                        i.putExtra("refreshCalib", "y");
                        i.putExtra(Dashboard.KEY_DEVICE_ID, PhActivity.DEVICE_ID);
                        startActivity(i);
                        getActivity().finish();

//                        Fragment frg = null;
//                        frg = requireActivity().getSupportFragmentManager().findFragmentById(R.layout.fragment_ph_calib_new);
//                        final FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
//                        if (frg != null) {
//                            ft.detach(frg);
//                        }
//                        ft.attach(frg);
//                        ft.commit();

                    }
                });

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }


    private void getAllMvData() {

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
//                String ecForm = String.format(Locale.UK, "%.1f", phVal);
                minMV1 = phVal;


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV1_3 = minMV2 = phVal;


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV2_3 = minMV3 = phVal;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV3_3 = minMV4 = phVal;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV5 = phVal;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV1 = phVal;


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV1_3 = maxMV2 = phVal;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV2_3 = maxMV3 = phVal;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV3_3 = maxMV4 = phVal;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV5 = phVal;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void fileNotWrite(File file) {
        file.setWritable(false);
        if (file.canWrite()) {
            Log.d("csv", "Nhi kaam kar rha");
        } else {
            Log.d("csvnw", "Party Bhaiiiii");
        }
    }

    private void generatePDF() throws FileNotFoundException {


        String company_name = "Company: " + companyName;
        String user_name = "Report generated by: " + Source.logUserName;
        String device_id = "DeviceID: " + deviceID;
        String calib_by = "Calibrated by: " + Source.calib_completed_by;

        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        SharedPreferences shp = getContext().getSharedPreferences("Extras", MODE_PRIVATE);
        offset = "Offset: " + shp.getString("offset", "");
        battery = "Battery: " + shp.getString("battery", "");
        slope = "Slope: " + shp.getString("slope", "");
        temp = "Temperature: " + shp.getString("temp", "");

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CD_" + currentDateandTime + "_" + ((tempFilesAndFolders != null ? tempFilesAndFolders.length : 0) - 1) + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);


//        Text text = new Text(company_name);
//        Text text1 = new Text(user_name);
//        Text text2 = new Text(device_id);
//
//
//
        if (Constants.OFFLINE_MODE) {
            document.add(new Paragraph("Offline Mode"));
        }
        document.add(new Paragraph(company_name + "\n" + calib_by + "\n" + user_name + "\n" + device_id));
        document.add(new Paragraph(""));
        document.add(new Paragraph(reportDate
                + "  |  " + reportTime + "\n" +
                offset + "  |  " + battery + "\n" + slope + "  |  " + temp
        ));

        document.add(new Paragraph(""));
        document.add(new Paragraph("Calibration Table"));

        float columnWidth[] = {200f, 210f, 190f, 170f, 340f, 170f};
        Table table = new Table(columnWidth);
        table.addCell("pH");
        table.addCell("pH Aft Calib");
        table.addCell("Slope");
        table.addCell("mV");
        table.addCell("Date & Time");
        table.addCell("Temperature");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor calibCSV;

        if (Constants.OFFLINE_MODE) {
            calibCSV = db.rawQuery("SELECT * FROM CalibOfflineData", null);

        } else {
            calibCSV = db.rawQuery("SELECT * FROM CalibData", null);
        }


        while (calibCSV.moveToNext()) {
            String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
            String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
            String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
            String slope = calibCSV.getString(calibCSV.getColumnIndex("SLOPE"));
            String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
            String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));

            table.addCell(ph);
            table.addCell(pHAC + "");
            table.addCell(slope + "");
            table.addCell(mv);
            table.addCell(date);
            table.addCell(temperature1);

        }
        document.add(table);

        if (spin.getSelectedItemPosition() == 0) {
            document.add(new Paragraph("Calibration : " + calib_stat));
        }

        document.add(new Paragraph("Operator Sign                                                                                      Supervisor Sign"));

        Bitmap imgBit1 = getSignImage();
        if (imgBit1 != null) {
            Uri uri1 = getImageUri(getContext(), imgBit1);

            try {
                String add = getPath(uri1);
                ImageData imageData1 = ImageDataFactory.create(add);
                Image image1 = new Image(imageData1).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        document.close();

        Toast.makeText(getContext(), "Pdf generated", Toast.LENGTH_SHORT).show();

    }

    private void generateAllPDF() throws FileNotFoundException {

        String company_name = "Company: " + companyName;
        String user_name = "Report generated by: " + Source.logUserName;
        String device_id = "DeviceID: " + deviceID;
        String calib_by = "Calibrated by: " + Source.calib_completed_by;

        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        SharedPreferences shp = getContext().getSharedPreferences("Extras", MODE_PRIVATE);
        offset = "Offset: " + shp.getString("offset", "");
        battery = "Battery: " + shp.getString("battery", "");
        slope = "Slope: " + shp.getString("slope", "");
        temp = "Temperature: " + shp.getString("temp", "");

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/AllCalibData_" + currentDateandTime + "_" + ((tempFilesAndFolders != null ? tempFilesAndFolders.length : 0) - 1) + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);


//        Text text = new Text(company_name);
//        Text text1 = new Text(user_name);
//        Text text2 = new Text(device_id);
//
//
//
//        document.add(new Paragraph(text).add(text1).add(text2));
        document.add(new Paragraph(company_name + "\n" + calib_by + "\n" + user_name + "\n" + device_id));
        document.add(new Paragraph(""));
        document.add(new Paragraph(reportDate
                + "  |  " + reportTime + "\n" +
                offset + "  |  " + battery + "\n" + slope + "  |  " + temp
        ));

        document.add(new Paragraph(""));
        document.add(new Paragraph("Calibration Table"));

        float columnWidth[] = {200f, 210f, 190f, 170f, 340f, 170f};
        Table table = new Table(columnWidth);
        table.addCell("pH");
        table.addCell("pH Aft Calib");
        table.addCell("Slope");
        table.addCell("mV");
        table.addCell("Date & Time");
        table.addCell("Temperature");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor calibCSV;

        if (Constants.OFFLINE_MODE) {
            calibCSV = db.rawQuery("SELECT * FROM CalibOfflineAllData", null);

        } else {
            calibCSV = db.rawQuery("SELECT * FROM CalibAllData", null);

        }


        while (calibCSV.moveToNext()) {
            String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
            String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
            String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
            String slope = calibCSV.getString(calibCSV.getColumnIndex("SLOPE"));
            String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
            String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));

            table.addCell(ph);
            table.addCell(pHAC + "");
            table.addCell(slope + "");
            table.addCell(mv);
            table.addCell(date);
            table.addCell(temperature1);

        }
        document.add(table);

        if (spin.getSelectedItemPosition() == 0) {
            document.add(new Paragraph("Calibration : " + calib_stat));
        }

        document.add(new Paragraph("Operator Sign                                                                                      Supervisor Sign"));

        Bitmap imgBit1 = getSignImage();
        if (imgBit1 != null) {
            Uri uri1 = getImageUri(getContext(), imgBit1);

            try {
                String add = getPath(uri1);
                ImageData imageData1 = ImageDataFactory.create(add);
                Image image1 = new Image(imageData1).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        document.close();

        Toast.makeText(getContext(), "Pdf generated", Toast.LENGTH_SHORT).show();

    }

    public void getFirebaseValue() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child(Dashboard.DEVICE_TYPE_PH).child(PhActivity.DEVICE_ID);
        dataRef.child("ID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                String p = snapshot.getValue(String.class);
                deviceID = p;
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap getSignImage() {
        SharedPreferences sh = getContext().getSharedPreferences("signature", Context.MODE_PRIVATE);
        String photo = sh.getString("signature_data", "");
        Bitmap bitmap = null;

        if (!photo.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }


    private static int line_3 = 0;
    public static boolean wrong_3 = false;
    CountDownTimer timer3;
    final Handler handler33 = new Handler();
    Runnable runnable33;

    private void calibrateThreePointOffline() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        calibrateBtnThree.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtnThree.setEnabled(false);

        tvTimerThree.setVisibility(View.VISIBLE);
        isCalibrating = true;

//        startTimer();


        timer3 = new CountDownTimer(45000, 1000) { //45000
            @Override
            public void onTick(long millisUntilFinished) {
                calibrateBtnThree.setEnabled(false);
                phGraph.setEnabled(false);
                phMvTable.setEnabled(false);
                printCalibData.setEnabled(false);
                calibSpinner.setEnabled(false);
                spin.setEnabled(false);

                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimerThree.setText(time);
                Log.e("lineNThree", line + "");
                Source.calibratingNow = true;
                if (line_3 == -1) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                    wrong_3 = false;
                }
                if (line_3 == 0) {
                    log1_3.setBackgroundColor(Color.GRAY);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
//                    if (Float.parseFloat(String.valueOf(mV1_3)) <= maxMV1_3 && Float.parseFloat(String.valueOf(mV1_3)) >= minMV1_3) {
//                        wrong_3 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_3 = true;
//                        timer3.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtnThree.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//                    }
                }
                if (line_3 == 1) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.GRAY);
                    log3_3.setBackgroundColor(Color.WHITE);
//                    if (Float.parseFloat(String.valueOf(mV2_3)) <= maxMV2_3 && Float.parseFloat(String.valueOf(mV2_3)) >= minMV2_3) {
//                        wrong_3 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_3 = true;
//                        timer3.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtnThree.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }
                }
                if (line_3 == 2) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.GRAY);
//                    if (Float.parseFloat(String.valueOf(mV3_3)) <= maxMV3_3 && Float.parseFloat(String.valueOf(mV3_3)) >= minMV3_3) {
//                        wrong_3 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_3 = true;
//                        timer3.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtnThree.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }
                }

                if (line_3 > 2) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                    wrong_3 = false;
                }
                wrong_3 = false;

            }


            @Override
            public void onFinish() {
                runnable33 = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Source.calibratingNow = false;

                            phGraph.setEnabled(true);
                            phMvTable.setEnabled(true);
                            printCalibData.setEnabled(true);
                            calibSpinner.setEnabled(true);
                            spin.setEnabled(true);

                            if (!wrong_3) {

                                wrong_3 = false;

                                line_3 = currentBufThree + 1;

                                if (currentBufThree == 2) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    dt3_3.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();

                                    JSONObject object = new JSONObject();
                                    jsonData.put("DT_4", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket1.send(jsonData.toString());
//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(date123 + " " + time123);
                                    calibrateBtnThree.setEnabled(false);
                                    Source.calib_completed_by = Source.logUserName;
                                    calibrateBtnThree.setText("DONE");
                                    startTimer3();

                                }

                                if (currentBufThree == 0) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    dt1_3.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();

                                    JSONObject object = new JSONObject();
                                    jsonData.put("DT_2", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket1.send(jsonData.toString());
//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(date123 + " " + time123);
                                    log1_3.setBackgroundColor(Color.WHITE);
                                    log2_3.setBackgroundColor(Color.GRAY);
                                    log3_3.setBackgroundColor(Color.WHITE);

                                }
                                if (currentBufThree == 1) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    dt2_3.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();
                                    jsonData.put("DT_3", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket1.send(jsonData.toString());
//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(date123 + " " + time123);
                                    log1_3.setBackgroundColor(Color.WHITE);
                                    log2_3.setBackgroundColor(Color.WHITE);
                                    log3_3.setBackgroundColor(Color.GRAY);

                                }

                                calibrateBtnThree.setEnabled(true);

                                tvTimerThree.setVisibility(View.INVISIBLE);
                                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                                bufferListThree.add(new BufferData(null, null, currentTime));
//                        bufferListThree.add(new BufferData(null, null, currentTime));

                                jsonData = new JSONObject();
                                jsonData.put("CAL", String.valueOf(calValuesThree[currentBufThree] + 1));
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket1.send(jsonData.toString());

//                                deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree] + 1);
                                Log.e("cValue", currentBufThree + "");

//                                int b = currentBuf < 0 ? 4 : currentBuf;
                                int b = currentBufThree;

                                Log.e("cValue2", currentBufThree + "");
                                Log.e("bValue", b + "");

                                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                if (b == 0) {


                                    myEdit.putString("tem1_3", tvTempCurr.getText().toString());
                                    myEdit.commit();
                                    jsonData = new JSONObject();
                                    jsonData.put("CALIBRATION_STAT", "incomplete");
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket1.send(jsonData.toString());
//                                    deviceRef.child("Data").child("CALIBRATION_STAT").setValue("incomplete");
//                                    calibData3();

//                                    CalibDatClass calibDatClass1 = new CalibDatClass(1, PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");

                                    CalibDatClass calibDatClass1 = new CalibDatClass(1, ph1_3.getText().toString(),
                                            mv1_3.getText().toString(), slope1_3.getText().toString(), dt1_3.getText().toString(),
                                            bufferD1_3.getText().toString(), phAfterCalib1_3.getText().toString(), tvTempCurr.getText().toString(),
                                            dt1_3.getText().toString().length() >= 15 ? dt1_3.getText().toString().substring(0, 10) : "--",
                                            dt1_3.getText().toString().length() >= 15 ? dt1_3.getText().toString().substring(11, 16) : "--");


                                    databaseHelper.updateClbOffDataThree(calibDatClass1);

                                    temp1_3.setText(tvTempCurr.getText());
                                } else if (b == 1) {


                                    myEdit.putString("tem2_3", tvTempCurr.getText().toString());
                                    myEdit.commit();
//                                    calibData3();

//                                    CalibDatClass calibDatClass2 = new CalibDatClass(2, PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");


                                    CalibDatClass calibDatClass2 = new CalibDatClass(2, ph2_3.getText().toString(),
                                            mv2_3.getText().toString(), slope2_3.getText().toString(), dt2_3.getText().toString(),
                                            bufferD2_3.getText().toString(), phAfterCalib2_3.getText().toString(), tvTempCurr.getText().toString(),
                                            dt2_3.getText().toString().length() >= 15 ? dt2_3.getText().toString().substring(0, 10) : "--",
                                            dt2_3.getText().toString().length() >= 15 ? dt2_3.getText().toString().substring(11, 16) : "--");


                                    databaseHelper.updateClbOffDataThree(calibDatClass2);

                                    temp2_3.setText(tvTempCurr.getText());
                                } else if (b == 2) {
                                    myEdit.putString("tem3_3", tvTempCurr.getText().toString());
                                    myEdit.commit();
                                    temp3_3.setText(tvTempCurr.getText());

                                    jsonData = new JSONObject();
                                    jsonData.put("CALIBRATION_STAT", "ok");
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket1.send(jsonData.toString());
//                                    deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");
                                    calibData3();
//                                    CalibDatClass calibDatClass3 = new CalibDatClass(3, PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");


                                    CalibDatClass calibDatClass3 = new CalibDatClass(3, ph3_3.getText().toString(),
                                            mv3_3.getText().toString(), slope3_3.getText().toString(), dt3_3.getText().toString(),
                                            bufferD3_3.getText().toString(), phAfterCalib3_3.getText().toString(), tvTempCurr.getText().toString(),
                                            dt3_3.getText().toString().length() >= 15 ? dt3_3.getText().toString().substring(0, 10) : "--",
                                            dt3_3.getText().toString().length() >= 15 ? dt3_3.getText().toString().substring(11, 16) : "--");

                                    databaseHelper.updateClbOffDataThree(calibDatClass3);

                                    databaseHelper.insertCalibrationOfflineAllData(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");
                                    databaseHelper.insertCalibrationOfflineAllData(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");
                                    databaseHelper.insertCalibrationOfflineAllData(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");


                                }
                                currentBufThree += 1;
                                calibData3();
                                deleteAllOfflineCalibData();


                                databaseHelper.insertCalibrationOfflineData(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");
                                databaseHelper.insertCalibrationOfflineData(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");
                                databaseHelper.insertCalibrationOfflineData(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");

                            } else {
//                            --line_3;
//                            --currentBufThree;
                                timer3.cancel();
                                handler33.removeCallbacks(this);
                                wrong_3 = false;
                                calibrateBtnThree.setEnabled(true);
                                showAlertDialogButtonClicked();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                };

                runnable33.run();
            }
        };

        try {
            jsonData = new JSONObject();
            jsonData.put("CAL", String.valueOf(calValuesThree[currentBufThree]));
            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
            webSocket1.send(jsonData.toString());
            timer3.start();

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if (!wrong_3) {
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree]).addOnSuccessListener(t -> {
//            timer3.start();
//        });
    }

    private void calibrateThreePoint() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        calibrateBtnThree.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtnThree.setEnabled(false);

        tvTimerThree.setVisibility(View.VISIBLE);
        isCalibrating = true;

//        startTimer();


        timer3 = new CountDownTimer(45000, 1000) { //45000
            @Override
            public void onTick(long millisUntilFinished) {
                calibrateBtnThree.setEnabled(false);
                phGraph.setEnabled(false);
                phMvTable.setEnabled(false);
                printCalibData.setEnabled(false);
                calibSpinner.setEnabled(false);
                spin.setEnabled(false);

                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimerThree.setText(time);
                Log.e("lineNThree", line + "");
                Source.calibratingNow = true;
                if (line_3 == -1) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                    wrong_3 = false;
                }
                if (line_3 == 0) {
                    log1_3.setBackgroundColor(Color.GRAY);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                    if (Float.parseFloat(String.valueOf(mV1_3)) <= maxMV1_3 && Float.parseFloat(String.valueOf(mV1_3)) >= minMV1_3) {
                        wrong_3 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_3 = true;
                        timer3.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtnThree.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
                    }
                }
                if (line_3 == 1) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.GRAY);
                    log3_3.setBackgroundColor(Color.WHITE);
                    if (Float.parseFloat(String.valueOf(mV2_3)) <= maxMV2_3 && Float.parseFloat(String.valueOf(mV2_3)) >= minMV2_3) {
                        wrong_3 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_3 = true;
                        timer3.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtnThree.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }
                }
                if (line_3 == 2) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.GRAY);
                    if (Float.parseFloat(String.valueOf(mV3_3)) <= maxMV3_3 && Float.parseFloat(String.valueOf(mV3_3)) >= minMV3_3) {
                        wrong_3 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_3 = true;
                        timer3.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtnThree.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }
                }

                if (line_3 > 2) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                    wrong_3 = false;
                }


            }


            @Override
            public void onFinish() {
                runnable33 = new Runnable() {
                    @Override
                    public void run() {
                        Source.calibratingNow = false;

                        phGraph.setEnabled(true);
                        phMvTable.setEnabled(true);
                        printCalibData.setEnabled(true);
                        calibSpinner.setEnabled(true);
                        spin.setEnabled(true);

                        if (!wrong_3) {

                            wrong_3 = false;

                            line_3 = currentBufThree + 1;

                            if (currentBufThree == 2) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(date123 + " " + time123);
                                calibrateBtnThree.setEnabled(false);
                                Source.calib_completed_by = Source.logUserName;
                                calibrateBtnThree.setText("DONE");
                                startTimer3();

                            }

                            if (currentBufThree == 0) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(date123 + " " + time123);
                                log1_3.setBackgroundColor(Color.WHITE);
                                log2_3.setBackgroundColor(Color.GRAY);
                                log3_3.setBackgroundColor(Color.WHITE);

                            }
                            if (currentBufThree == 1) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(date123 + " " + time123);
                                log1_3.setBackgroundColor(Color.WHITE);
                                log2_3.setBackgroundColor(Color.WHITE);
                                log3_3.setBackgroundColor(Color.GRAY);

                            }

                            calibrateBtnThree.setEnabled(true);

                            tvTimerThree.setVisibility(View.INVISIBLE);
                            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                            bufferListThree.add(new BufferData(null, null, currentTime));
//                        bufferListThree.add(new BufferData(null, null, currentTime));

                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree] + 1);
                            Log.e("cValue", currentBufThree + "");

                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabelsThree[currentBufThree]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
//                                int b = currentBuf < 0 ? 4 : currentBuf;
                                int b = currentBufThree;

                                Log.e("cValue2", currentBufThree + "");
                                Log.e("bValue", b + "");
                                if (coeff == null) return;

                                deviceRef.child("UI").child("PH").child("PH_CAL").child(postCoeffLabelsThree[b]).get().addOnSuccessListener(dataSnapshot2 -> {
                                    Float postCoeff = dataSnapshot2.getValue(Float.class);
                                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    if (b == 0) {
                                        phAfterCalib1_3.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem1_3", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC1_3", String.valueOf(postCoeff));
                                        myEdit.commit();
                                        deviceRef.child("Data").child("CALIBRATION_STAT").setValue("incomplete");

                                        temp1_3.setText(tvTempCurr.getText());
                                    } else if (b == 1) {
                                        phAfterCalib2_3.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem2_3", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC2_3", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp2_3.setText(tvTempCurr.getText());
                                    } else if (b == 2) {
                                        phAfterCalib3_3.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem3_3", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC3_3", String.valueOf(postCoeff));
                                        myEdit.commit();
                                        temp3_3.setText(tvTempCurr.getText());

                                        deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");
                                        calibData3();

                                        databaseHelper.insertCalibrationAllData(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");
                                        databaseHelper.insertCalibrationAllData(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");
                                        databaseHelper.insertCalibrationAllData(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");


                                    }
                                    currentBufThree += 1;
                                    calibData3();
                                    deleteAllCalibData();

                                    databaseHelper.insertCalibration(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");
                                    databaseHelper.insertCalibration(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");
                                    databaseHelper.insertCalibration(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");


                                });
                            });
                        } else {
//                            --line_3;
//                            --currentBufThree;
                            timer3.cancel();
                            handler33.removeCallbacks(this);
                            wrong_3 = false;
                            calibrateBtnThree.setEnabled(true);
                            showAlertDialogButtonClicked();

                        }
                    }

                };

                runnable33.run();
            }
        };

//        if (!wrong_3) {
        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree]).addOnSuccessListener(t -> {
            timer3.start();
        });
//        }
    }


    private static int line = 0;
    public static boolean wrong_5 = false;
    CountDownTimer timer5;
    final Handler handler55 = new Handler();
    Runnable runnable55;

    private void calibrateFivePointOffline(WebSocket webSocket2) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtn.setEnabled(false);

        tvTimer.setVisibility(View.VISIBLE);
        isCalibrating = true;

        timer5 = new CountDownTimer(45000, 1000) { //45000
            @Override
            public void onTick(long millisUntilFinished) {
                calibrateBtn.setEnabled(false);
                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimer.setText(time);
                Log.e("lineN", line + "");
                Source.calibratingNow = true;

                phGraph.setEnabled(false);
                phMvTable.setEnabled(false);
                printCalibData.setEnabled(false);
                calibSpinner.setEnabled(false);
                spin.setEnabled(false);


                if (line == -1) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                }
                if (line == 0) {
                    log1.setBackgroundColor(Color.GRAY);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
//                    if (Float.parseFloat(String.valueOf(mV1)) <= maxMV1 && Float.parseFloat(String.valueOf(mV1)) >= minMV1) {
//                        wrong_5 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_5 = true;
//                        timer5.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtn.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }
                }
                if (line == 1) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.GRAY);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
//                    if (Float.parseFloat(String.valueOf(mV2)) <= maxMV2 && Float.parseFloat(String.valueOf(mV2)) >= minMV2) {
//                        wrong_5 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_5 = true;
//                        timer5.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtn.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }
                }
                if (line == 2) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.GRAY);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
//                    if (Float.parseFloat(String.valueOf(mV3)) <= maxMV3 && Float.parseFloat(String.valueOf(mV3)) >= minMV3) {
//                        wrong_5 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_5 = true;
//                        timer5.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtn.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }
                }
                if (line == 3) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.GRAY);
                    log5.setBackgroundColor(Color.WHITE);

//                    if (Float.parseFloat(String.valueOf(mV4)) <= maxMV4 && Float.parseFloat(String.valueOf(mV4)) >= minMV4) {
//                        wrong_5 = false;
////                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wrong_5 = true;
//                        timer5.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtn.setEnabled(true);
//                        showAlertDialogButtonClicked();
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }

                }
                if (line == 4) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.GRAY);
//                    if (Float.parseFloat(String.valueOf(mV5)) <= maxMV5 && Float.parseFloat(String.valueOf(mV5)) >= minMV5) {
//                        wrong_5 = false;
//                    } else {
//                        wrong_5 = true;
//                        timer5.cancel();
////                        handler33.removeCallbacks(this);
////                        wrong_3 = false;
//                        calibrateBtn.setEnabled(true);
//                        showAlertDialogButtonClicked();
//
//                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();
//
//                    }
                }
                if (line > 4) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                    wrong_5 = false;
                }
                wrong_5 = false;

            }


            @Override
            public void onFinish() {
                runnable55 = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Source.calibratingNow = false;

                            phGraph.setEnabled(true);
                            phMvTable.setEnabled(true);
                            printCalibData.setEnabled(true);
                            calibSpinner.setEnabled(true);
                            spin.setEnabled(true);

                            if (!wrong_5) {
                                wrong_5 = false;
                                line = currentBuf + 1;

                                if (currentBuf == 4) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    dt5.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();
                                    JSONObject object1 = new JSONObject();
                                    jsonData.put("DT_5", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());

//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_5").setValue(date123 + " " + time123);
                                    calibrateBtn.setEnabled(false);
                                    Source.calib_completed_by = Source.logUserName;
                                    calibrateBtn.setText("DONE");
                                    startTimer();

                                }

                                if (currentBuf == 0) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                    dt1.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();
                                    JSONObject object1 = new JSONObject();
                                    jsonData.put("DT_1", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());
//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_1").setValue(date123 + " " + time123);
                                    log1.setBackgroundColor(Color.WHITE);
                                    log2.setBackgroundColor(Color.GRAY);
                                    log3.setBackgroundColor(Color.WHITE);
                                    log4.setBackgroundColor(Color.WHITE);
                                    log5.setBackgroundColor(Color.WHITE);
                                }
                                if (currentBuf == 1) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                    dt2.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();
                                    JSONObject object1 = new JSONObject();
                                    jsonData.put("DT_2", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());

//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(date123 + " " + time123);
                                    log1.setBackgroundColor(Color.WHITE);
                                    log2.setBackgroundColor(Color.WHITE);
                                    log3.setBackgroundColor(Color.GRAY);
                                    log4.setBackgroundColor(Color.WHITE);
                                    log5.setBackgroundColor(Color.WHITE);
                                }
                                if (currentBuf == 2) {

                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    dt3.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();
                                    JSONObject object1 = new JSONObject();
                                    jsonData.put("DT_3", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());

//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(date123 + " " + time123);

                                    log1.setBackgroundColor(Color.WHITE);
                                    log2.setBackgroundColor(Color.WHITE);
                                    log3.setBackgroundColor(Color.WHITE);
                                    log4.setBackgroundColor(Color.GRAY);
                                    log5.setBackgroundColor(Color.WHITE);
                                }
                                if (currentBuf == 3) {
                                    String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    dt4.setText(date123 + " " + time123);

                                    jsonData = new JSONObject();
                                    JSONObject object1 = new JSONObject();
                                    jsonData.put("DT_4", date123 + " " + time123);
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());

//                                    deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(date123 + " " + time123);
                                    log1.setBackgroundColor(Color.WHITE);
                                    log2.setBackgroundColor(Color.WHITE);
                                    log3.setBackgroundColor(Color.WHITE);
                                    log4.setBackgroundColor(Color.WHITE);
                                    log5.setBackgroundColor(Color.GRAY);
                                }


                                calibrateBtn.setEnabled(true);

                                tvTimer.setVisibility(View.INVISIBLE);
                                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                                bufferList.add(new BufferData(null, null, currentTime));
//                        bufferListThree.add(new BufferData(null, null, currentTime));

                                jsonData = new JSONObject();
                                JSONObject object0 = new JSONObject();
                                jsonData.put("CAL", String.valueOf(calValues[currentBuf] + 1));
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket2.send(jsonData.toString());
//                                deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                                Log.e("cValue", currentBuf + "");


//                                int b = currentBuf < 0 ? 4 : currentBuf;
                                int b = currentBuf;

                                Log.e("cValue2", currentBuf + "");
                                Log.e("bValue", b + "");

//                                deviceRef.child("UI").child("PH").child("PH_CAL").child(postCoeffLabels[b]).get().addOnSuccessListener(dataSnapshot2 -> {
//                                    Float postCoeff = dataSnapshot2.getValue(Float.class);
                                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                                if (b == 0) {
                                    myEdit.putString("tem1", tvTempCurr.getText().toString());
                                    myEdit.commit();
                                    jsonData = new JSONObject();
                                    jsonData.put("CALIBRATION_STAT", "incomplete");
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());
//                                    deviceRef.child("Data").child("CALIBRATION_STAT").setValue("incomplete");


                                    CalibDatClass calibDatClass = new CalibDatClass(1, ph1.getText().toString(),
                                            mv1.getText().toString(), slope1.getText().toString(), dt1.getText().toString(),
                                            bufferD1.getText().toString(), phAfterCalib1.getText().toString(), tvTempCurr.getText().toString(),
                                            dt1.getText().toString().length() >= 15 ? dt1.getText().toString().substring(0, 10) : "--",
                                            dt1.getText().toString().length() >= 15 ? dt1.getText().toString().substring(11, 16) : "--");

                                    databaseHelper.updateClbOffDataFive(calibDatClass);


                                    temp1.setText(tvTempCurr.getText());
                                } else if (b == 1) {
                                    myEdit.putString("tem2", tvTempCurr.getText().toString());
                                    myEdit.commit();


                                    CalibDatClass calibDatClass = new CalibDatClass(2, ph2.getText().toString(),
                                            mv2.getText().toString(), slope2.getText().toString(), dt2.getText().toString(),
                                            bufferD2.getText().toString(), phAfterCalib2.getText().toString(), tvTempCurr.getText().toString(),
                                            dt2.getText().toString().length() >= 15 ? dt2.getText().toString().substring(0, 10) : "--",
                                            dt2.getText().toString().length() >= 15 ? dt2.getText().toString().substring(11, 16) : "--");

                                    databaseHelper.updateClbOffDataFive(calibDatClass);

                                    temp2.setText(tvTempCurr.getText());
                                } else if (b == 2) {
                                    myEdit.putString("tem3", tvTempCurr.getText().toString());
                                    myEdit.commit();


                                    CalibDatClass calibDatClass = new CalibDatClass(3, ph3.getText().toString(),
                                            mv3.getText().toString(), slope3.getText().toString(), dt3.getText().toString(),
                                            bufferD3.getText().toString(), phAfterCalib3.getText().toString(), tvTempCurr.getText().toString(),
                                            dt3.getText().toString().length() >= 15 ? dt3.getText().toString().substring(0, 10) : "--",
                                            dt3.getText().toString().length() >= 15 ? dt3.getText().toString().substring(11, 16) : "--");

                                    databaseHelper.updateClbOffDataFive(calibDatClass);

                                    temp3.setText(tvTempCurr.getText());
                                } else if (b == 3) {
                                    myEdit.putString("tem4", tvTempCurr.getText().toString());
                                    myEdit.commit();


                                    CalibDatClass calibDatClass = new CalibDatClass(4, ph4.getText().toString(),
                                            mv4.getText().toString(), slope4.getText().toString(), dt4.getText().toString(),
                                            bufferD4.getText().toString(), phAfterCalib4.getText().toString(), tvTempCurr.getText().toString(),
                                            dt4.getText().toString().length() >= 15 ? dt4.getText().toString().substring(0, 10) : "--",
                                            dt4.getText().toString().length() >= 15 ? dt4.getText().toString().substring(11, 16) : "--");

                                    databaseHelper.updateClbOffDataFive(calibDatClass);
                                    temp4.setText(tvTempCurr.getText());
                                } else if (b == 4) {
                                    myEdit.putString("tem5", tvTempCurr.getText().toString());
                                    myEdit.commit();
                                    temp5.setText(tvTempCurr.getText());
                                    jsonData = new JSONObject();
                                    jsonData.put("CALIBRATION_STAT", "ok");
                                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                    webSocket2.send(jsonData.toString());
//                                    deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");
                                    calibData();

                                    CalibDatClass calibDatClass = new CalibDatClass(5, ph5.getText().toString(),
                                            mv5.getText().toString(), slope5.getText().toString(), dt5.getText().toString(),
                                            bufferD5.getText().toString(), phAfterCalib5.getText().toString(), tvTempCurr.getText().toString(),
                                            dt5.getText().toString().length() >= 15 ? dt5.getText().toString().substring(0, 10) : "--",
                                            dt5.getText().toString().length() >= 15 ? dt5.getText().toString().substring(11, 16) : "--");

                                    databaseHelper.updateClbOffDataFive(calibDatClass);

                                    databaseHelper.insertCalibrationOfflineAllData(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1, DT1.length() >= 15 ? DT1.substring(0, 10) : "--", DT1.length() >= 15 ? DT1.substring(11, 16) : "--");
                                    databaseHelper.insertCalibrationOfflineAllData(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2, DT2.length() >= 15 ? DT2.substring(0, 10) : "--", DT2.length() >= 15 ? DT2.substring(11, 16) : "--");
                                    databaseHelper.insertCalibrationOfflineAllData(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3, DT3.length() >= 15 ? DT3.substring(0, 10) : "--", DT3.length() >= 15 ? DT3.substring(11, 16) : "--");
                                    databaseHelper.insertCalibrationOfflineAllData(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4, DT4.length() >= 15 ? DT4.substring(0, 10) : "--", DT4.length() >= 15 ? DT4.substring(11, 16) : "--");
                                    databaseHelper.insertCalibrationOfflineAllData(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5, DT5.length() >= 15 ? DT5.substring(0, 10) : "--", DT5.length() >= 15 ? DT5.substring(11, 16) : "--");

                                }

                                currentBuf += 1;
                                calibData();
                                deleteAllOfflineCalibData();


                                databaseHelper.insertCalibrationOfflineData(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1, DT1.length() >= 15 ? DT1.substring(0, 10) : "--", DT1.length() >= 15 ? DT1.substring(11, 16) : "--");
                                databaseHelper.insertCalibrationOfflineData(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2, DT2.length() >= 15 ? DT2.substring(0, 10) : "--", DT2.length() >= 15 ? DT2.substring(11, 16) : "--");
                                databaseHelper.insertCalibrationOfflineData(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3, DT3.length() >= 15 ? DT3.substring(0, 10) : "--", DT3.length() >= 15 ? DT3.substring(11, 16) : "--");
                                databaseHelper.insertCalibrationOfflineData(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4, DT4.length() >= 15 ? DT4.substring(0, 10) : "--", DT4.length() >= 15 ? DT4.substring(11, 16) : "--");
                                databaseHelper.insertCalibrationOfflineData(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5, DT5.length() >= 15 ? DT5.substring(0, 10) : "--", DT5.length() >= 15 ? DT5.substring(11, 16) : "--");


                            } else {
//                            --line_3;
//                            --currentBufThree;
                                timer5.cancel();
                                handler55.removeCallbacks(this);
                                wrong_5 = false;
                                calibrateBtn.setEnabled(true);
                                showAlertDialogButtonClicked();

                            }

                        } catch (
                                JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                ;

                runnable55.run();
            }
        };

        try {
            JSONObject object = new JSONObject();
            jsonData = new JSONObject();
            jsonData.put("CAL", String.valueOf(calValues[currentBuf]));
            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
            webSocket2.send(jsonData.toString());

            timer5.start();

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
//            timer5.start();
//        });

    }

    private void calibrateFivePoint() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtn.setEnabled(false);

        tvTimer.setVisibility(View.VISIBLE);
        isCalibrating = true;

//        startTimer();


        timer5 = new CountDownTimer(45000, 1000) { //45000
            @Override
            public void onTick(long millisUntilFinished) {
                calibrateBtn.setEnabled(false);
                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimer.setText(time);
                Log.e("lineN", line + "");
                Source.calibratingNow = true;

                phGraph.setEnabled(false);
                phMvTable.setEnabled(false);
                printCalibData.setEnabled(false);
                calibSpinner.setEnabled(false);
                spin.setEnabled(false);


                if (line == -1) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                }
                if (line == 0) {
                    log1.setBackgroundColor(Color.GRAY);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                    if (Float.parseFloat(String.valueOf(mV1)) <= maxMV1 && Float.parseFloat(String.valueOf(mV1)) >= minMV1) {
                        wrong_5 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_5 = true;
                        timer5.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtn.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }
                }
                if (line == 1) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.GRAY);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                    if (Float.parseFloat(String.valueOf(mV2)) <= maxMV2 && Float.parseFloat(String.valueOf(mV2)) >= minMV2) {
                        wrong_5 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_5 = true;
                        timer5.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtn.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }
                }
                if (line == 2) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.GRAY);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                    if (Float.parseFloat(String.valueOf(mV3)) <= maxMV3 && Float.parseFloat(String.valueOf(mV3)) >= minMV3) {
                        wrong_5 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_5 = true;
                        timer5.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtn.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }
                }
                if (line == 3) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.GRAY);
                    log5.setBackgroundColor(Color.WHITE);

                    if (Float.parseFloat(String.valueOf(mV4)) <= maxMV4 && Float.parseFloat(String.valueOf(mV4)) >= minMV4) {
                        wrong_5 = false;
//                        Toast.makeText(getContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        wrong_5 = true;
                        timer5.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtn.setEnabled(true);
                        showAlertDialogButtonClicked();
                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }

                }
                if (line == 4) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.GRAY);
                    if (Float.parseFloat(String.valueOf(mV5)) <= maxMV5 && Float.parseFloat(String.valueOf(mV5)) >= minMV5) {
                        wrong_5 = false;
                    } else {
                        wrong_5 = true;
                        timer5.cancel();
//                        handler33.removeCallbacks(this);
//                        wrong_3 = false;
                        calibrateBtn.setEnabled(true);
                        showAlertDialogButtonClicked();

                        Toast.makeText(getContext(), "Out of Range", Toast.LENGTH_SHORT).show();

                    }
                }
                if (line > 4) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                    wrong_5 = false;
                }


            }


            @Override
            public void onFinish() {
                runnable55 = new Runnable() {
                    @Override
                    public void run() {
                        Source.calibratingNow = false;

                        phGraph.setEnabled(true);
                        phMvTable.setEnabled(true);
                        printCalibData.setEnabled(true);
                        calibSpinner.setEnabled(true);
                        spin.setEnabled(true);

                        if (!wrong_5) {
                            wrong_5 = false;
                            line = currentBuf + 1;

                            if (currentBuf == 4) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_5").setValue(date123 + " " + time123);
                                calibrateBtn.setEnabled(false);
                                Source.calib_completed_by = Source.logUserName;
                                calibrateBtn.setText("DONE");
                                startTimer();

                            }

                            if (currentBuf == 0) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_1").setValue(date123 + " " + time123);
                                log1.setBackgroundColor(Color.WHITE);
                                log2.setBackgroundColor(Color.GRAY);
                                log3.setBackgroundColor(Color.WHITE);
                                log4.setBackgroundColor(Color.WHITE);
                                log5.setBackgroundColor(Color.WHITE);
                            }
                            if (currentBuf == 1) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(date123 + " " + time123);
                                log1.setBackgroundColor(Color.WHITE);
                                log2.setBackgroundColor(Color.WHITE);
                                log3.setBackgroundColor(Color.GRAY);
                                log4.setBackgroundColor(Color.WHITE);
                                log5.setBackgroundColor(Color.WHITE);
                            }
                            if (currentBuf == 2) {

                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(date123 + " " + time123);

                                log1.setBackgroundColor(Color.WHITE);
                                log2.setBackgroundColor(Color.WHITE);
                                log3.setBackgroundColor(Color.WHITE);
                                log4.setBackgroundColor(Color.GRAY);
                                log5.setBackgroundColor(Color.WHITE);
                            }
                            if (currentBuf == 3) {
                                String date123 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                String time123 = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(date123 + " " + time123);
                                log1.setBackgroundColor(Color.WHITE);
                                log2.setBackgroundColor(Color.WHITE);
                                log3.setBackgroundColor(Color.WHITE);
                                log4.setBackgroundColor(Color.WHITE);
                                log5.setBackgroundColor(Color.GRAY);
                            }


                            calibrateBtn.setEnabled(true);

                            tvTimer.setVisibility(View.INVISIBLE);
                            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                            bufferList.add(new BufferData(null, null, currentTime));
//                        bufferListThree.add(new BufferData(null, null, currentTime));

                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                            Log.e("cValue", currentBuf + "");

                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
//                                int b = currentBuf < 0 ? 4 : currentBuf;
                                int b = currentBuf;

                                Log.e("cValue2", currentBuf + "");
                                Log.e("bValue", b + "");
                                if (coeff == null) return;

                                deviceRef.child("UI").child("PH").child("PH_CAL").child(postCoeffLabels[b]).get().addOnSuccessListener(dataSnapshot2 -> {
                                    Float postCoeff = dataSnapshot2.getValue(Float.class);
                                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    if (b == 0) {
                                        phAfterCalib1.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem1", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC1", String.valueOf(postCoeff));
                                        myEdit.commit();
                                        deviceRef.child("Data").child("CALIBRATION_STAT").setValue("incomplete");

                                        temp1.setText(tvTempCurr.getText());
                                    } else if (b == 1) {
                                        phAfterCalib2.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem2", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC2", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp2.setText(tvTempCurr.getText());
                                    } else if (b == 2) {
                                        phAfterCalib3.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem3", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC3", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp3.setText(tvTempCurr.getText());
                                    } else if (b == 3) {
                                        phAfterCalib4.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem4", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC4", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp4.setText(tvTempCurr.getText());
                                    } else if (b == 4) {
                                        phAfterCalib5.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem5", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC5", String.valueOf(postCoeff));
                                        myEdit.commit();
                                        temp5.setText(tvTempCurr.getText());
                                        deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");
                                        calibData();

                                        databaseHelper.insertCalibrationAllData(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1, DT1.length() >= 15 ? DT1.substring(0, 10) : "--", DT1.length() >= 15 ? DT1.substring(11, 16) : "--");
                                        databaseHelper.insertCalibrationAllData(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2, DT2.length() >= 15 ? DT2.substring(0, 10) : "--", DT2.length() >= 15 ? DT2.substring(11, 16) : "--");
                                        databaseHelper.insertCalibrationAllData(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3, DT3.length() >= 15 ? DT3.substring(0, 10) : "--", DT3.length() >= 15 ? DT3.substring(11, 16) : "--");
                                        databaseHelper.insertCalibrationAllData(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4, DT4.length() >= 15 ? DT4.substring(0, 10) : "--", DT4.length() >= 15 ? DT4.substring(11, 16) : "--");
                                        databaseHelper.insertCalibrationAllData(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5, DT5.length() >= 15 ? DT5.substring(0, 10) : "--", DT5.length() >= 15 ? DT5.substring(11, 16) : "--");


                                    }
                                    currentBuf += 1;
                                    calibData();
                                    deleteAllCalibData();


                                    databaseHelper.insertCalibration(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1, DT1.length() >= 15 ? DT1.substring(0, 10) : "--", DT1.length() >= 15 ? DT1.substring(11, 16) : "--");
                                    databaseHelper.insertCalibration(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2, DT2.length() >= 15 ? DT2.substring(0, 10) : "--", DT2.length() >= 15 ? DT2.substring(11, 16) : "--");
                                    databaseHelper.insertCalibration(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3, DT3.length() >= 15 ? DT3.substring(0, 10) : "--", DT3.length() >= 15 ? DT3.substring(11, 16) : "--");
                                    databaseHelper.insertCalibration(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4, DT4.length() >= 15 ? DT4.substring(0, 10) : "--", DT4.length() >= 15 ? DT4.substring(11, 16) : "--");
                                    databaseHelper.insertCalibration(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5, DT5.length() >= 15 ? DT5.substring(0, 10) : "--", DT5.length() >= 15 ? DT5.substring(11, 16) : "--");


                                });
                            });
                        } else {
//                            --line_3;
//                            --currentBufThree;
                            timer5.cancel();
                            handler55.removeCallbacks(this);
                            wrong_5 = false;
                            calibrateBtn.setEnabled(true);
                            showAlertDialogButtonClicked();

                        }
                    }

                };

                runnable55.run();
            }
        };

        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
            timer5.start();
        });
    }


    public void calibData() {
        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        MV1 = shp.getString("MV1", "--");
        MV2 = shp.getString("MV2", "--");
        MV3 = shp.getString("MV3", "--");
        MV4 = shp.getString("MV4", "--");
        MV5 = shp.getString("MV5", "--");

        DT1 = shp.getString("DT1", "--");
        DT2 = shp.getString("DT2", "--");
        DT3 = shp.getString("DT3", "--");
        DT4 = shp.getString("DT4", "--");
        DT5 = shp.getString("DT5", "--");

        PH1 = shp.getString("PH1", "--");
        PH2 = shp.getString("PH2", "--");
        PH3 = shp.getString("PH3", "--");
        PH4 = shp.getString("PH4", "--");
        PH5 = shp.getString("PH5", "--");

        BFD1 = shp.getString("BFD1", "--");
        BFD2 = shp.getString("BFD2", "--");
        BFD3 = shp.getString("BFD3", "--");
        BFD4 = shp.getString("BFD4", "--");
        BFD5 = shp.getString("BFD5", "--");

        SLOPE1 = shp.getString("SLOPE1", "--");
        SLOPE2 = shp.getString("SLOPE2", "--");
        SLOPE3 = shp.getString("SLOPE3", "--");
        SLOPE4 = shp.getString("SLOPE4", "--");
        SLOPE5 = shp.getString("SLOPE5", "--");

        pHAC1 = shp.getString("pHAC1", "--");
        pHAC2 = shp.getString("pHAC2", "--");
        pHAC3 = shp.getString("pHAC3", "--");
        pHAC4 = shp.getString("pHAC4", "--");
        pHAC5 = shp.getString("pHAC5", "--");

        t1 = shp.getString("tem1", "--");
        t2 = shp.getString("tem2", "--");
        t3 = shp.getString("tem3", "--");
        t4 = shp.getString("tem4", "--");
        t5 = shp.getString("tem5", "--");

    }

    public void calibData3() {
        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        MV1_3 = shp.getString("MV1_3", "--");
        MV2_3 = shp.getString("MV2_3", "--");
        MV3_3 = shp.getString("MV3_3", "--");

        DT1_3 = shp.getString("DT1_3", "--");
        DT2_3 = shp.getString("DT2_3", "--");
        DT3_3 = shp.getString("DT3_3", "--");

        PH1_3 = shp.getString("PH1_3", "--");
        PH2_3 = shp.getString("PH2_3", "--");
        PH3_3 = shp.getString("PH3_3", "--");

        BFD1_3 = shp.getString("BFD1_3", "--");
        BFD2_3 = shp.getString("BFD2_3", "--");
        BFD3_3 = shp.getString("BFD3_3", "--");

        SLOPE1_3 = shp.getString("SLOPE1_3", "--");
        SLOPE2_3 = shp.getString("SLOPE2_3", "--");
        SLOPE3_3 = shp.getString("SLOPE3_3", "--");

        pHAC1_3 = shp.getString("pHAC1_3", "--");
        pHAC2_3 = shp.getString("pHAC2_3", "--");
        pHAC3_3 = shp.getString("pHAC3_3", "--");

        t1_3 = shp.getString("tem1_3", "--");
        t2_3 = shp.getString("tem2_3", "--");
        t3_3 = shp.getString("tem3_3", "--");


    }

    private void startTimer() {

        calibrateBtn.setEnabled(false);
        LOG_INTERVAL = 5;
        tvTimer.setText(String.valueOf(LOG_INTERVAL));
        handler1 = new Handler();
        runnable1 = new Runnable() {
            public void run() {
                Log.d("Runnable", "Handler is working");
                calibrateBtn.setEnabled(false);
                if (LOG_INTERVAL == 0) { // just remove call backs

                    tvTimer.setText(String.valueOf(LOG_INTERVAL));
                    handler1.removeCallbacks(this);
                    calibrateBtn.setEnabled(true);
                    calibrateBtn.setText("Start");
                    currentBuf = 0;
                    line = 0;
                    log1.setBackgroundColor(Color.GRAY);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                    tvTimer.setText("00:45");
                    Log.d("Runnable", "ok");
                } else { // post again
                    --LOG_INTERVAL;
                    tvTimer.setText("00:0" + String.valueOf(LOG_INTERVAL).substring(0, 1));
                    handler1.postDelayed(this, 1000);
                }
            }
        };

        runnable1.run();
    }

    private void startTimer3() {

        calibrateBtnThree.setEnabled(false);
        LOG_INTERVAL_3 = 5;
        tvTimerThree.setText(String.valueOf(LOG_INTERVAL_3));
        handler2 = new Handler();
        runnable2 = new Runnable() {
            public void run() {
                Log.d("Runnable", "Handler is working");
                calibrateBtnThree.setEnabled(false);
                if (LOG_INTERVAL_3 == 0) { // just remove call backs

                    tvTimerThree.setText(String.valueOf(LOG_INTERVAL_3));
                    handler2.removeCallbacks(this);
                    calibrateBtnThree.setEnabled(true);
                    calibrateBtnThree.setText("Start");
                    currentBufThree = 0;
                    line_3 = 0;
                    log1_3.setBackgroundColor(Color.GRAY);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                    tvTimerThree.setText("00:45");
                    Log.d("Runnable", "ok");
                } else { // post again
                    --LOG_INTERVAL_3;
                    tvTimerThree.setText("00:0" + String.valueOf(LOG_INTERVAL_3).substring(0, 1));
                    handler2.postDelayed(this, 1000);
                }
            }
        };

        runnable2.run();
    }


    public void deleteAllCalibData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM CalibData");
        db.close();
    }


    public void deleteAllOfflineCalibData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM CalibOfflineData");
        db.close();
    }

    private void fetchAllDataFromFirebase() {
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        deviceRef.child("PH_MODE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PhCalibFragmentNew.PH_MODE = snapshot.getValue(String.class);
                } else {
                    deviceRef.child("PH_MODE").setValue("both");
                    PhCalibFragmentNew.PH_MODE = "both";
                }
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });
        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float temp = snapshot.getValue(Float.class);
                String tempForm = String.format(Locale.UK, "%.1f", temp);
                tvTempCurr.setText(tempForm + "°C");

                if (temp <= -127.0) {
                    tvTempCurr.setText("NA");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if (ph == null) return;
                String phForm = String.format(Locale.UK, "%.2f", ph);
                tvPhCurr.setText(phForm);
                phView.moveTo(ph);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
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

    }

    private void fetchAllData3Point() {


        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib1_3.setText(ecForm);
                pHAC1_3 = phAfterCalib1_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC1_3", pHAC1_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib2_3.setText(ecForm);
                pHAC2_3 = phAfterCalib2_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC2_3", pHAC2_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib3_3.setText(ecForm);
                pHAC3_3 = phAfterCalib3_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC3_3", pHAC3_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv1_3.setText(ecForm);
                mV1_3 = mv1_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV1_3", mV1_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv2_3.setText(ecForm);
                mV2_3 = mv2_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV2_3", mV2_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv3_3.setText(ecForm);
                mV3_3 = mv3_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV3_3", mV3_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // for slope 1

        // for slope 2
        deviceRef.child("Data").child("SLOPE_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope1_3.setText(sl);
                SLOPE1_3 = slope1_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE1_3", SLOPE1_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        // for slope 3
        deviceRef.child("Data").child("SLOPE_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope2_3.setText(sl);
                SLOPE2_3 = slope2_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE2_3", SLOPE2_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("Data").child("SLOPE_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope3_3.setText(sl);
                SLOPE3_3 = slope3_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE3_3", SLOPE3_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt1_3.setText(time);
                DT1_3 = dt1_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT1_3", DT1_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt2_3.setText(time);
                DT2_3 = dt2_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT2_3", DT2_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt3_3.setText(time);
                DT3_3 = dt3_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT3_3", DT3_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph1_3.setText(phVal);
                PH1_3 = ph1_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH1_3", PH1_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph2_3.setText(phVal);
                PH2_3 = ph2_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH2_3", PH2_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph3_3.setText(phVal);
                PH3_3 = ph3_3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH3_3", PH3_3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


    }

    private void fetchAllData5Point() {

        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv1.setText(ecForm);
                mV1 = mv1.getText().toString();
                Log.d("test1", mV1);

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV1", mV1);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // for slope 2
        deviceRef.child("Data").child("SLOPE_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope2.setText(sl);
                SLOPE2 = slope2.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE2", SLOPE2);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // for slope 3
        deviceRef.child("Data").child("SLOPE_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope3.setText(sl);
                SLOPE3 = slope3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE3", SLOPE3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // for slope 4
        deviceRef.child("Data").child("SLOPE_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope4.setText(sl);
                SLOPE4 = slope4.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE4", SLOPE4);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // for slope 5
        deviceRef.child("Data").child("SLOPE_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float slopes = snapshot.getValue(Float.class);
                String sl = String.format(Locale.UK, "%.2f", slopes);
                slope5.setText(sl);
                SLOPE5 = slope5.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("SLOPE5", SLOPE5);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib1.setText(ecForm);
                pHAC1 = phAfterCalib1.getText().toString();


                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC1", pHAC1);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib2.setText(ecForm);
                pHAC2 = phAfterCalib2.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC2", pHAC2);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib3.setText(ecForm);
                pHAC3 = phAfterCalib3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC3", pHAC3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib4.setText(ecForm);
                pHAC4 = phAfterCalib4.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC4", pHAC4);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                phAfterCalib5.setText(ecForm);
                pHAC5 = phAfterCalib5.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("pHAC5", pHAC5);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv2.setText(ecForm);
                mV2 = mv2.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV2", mV2);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv3.setText(ecForm);
                mV3 = mv3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV3", mV3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv4.setText(ecForm);
                mV4 = mv4.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV4", mV4);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.2f", ec);
                mv5.setText(ecForm);
                mV5 = mv5.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("MV5", mV5);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt1.setText(time);
                DT1 = dt1.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT1", DT1);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt2.setText(time);
                DT2 = dt2.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT2", DT2);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt3.setText(time);
                DT3 = dt3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT3", DT3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt4.setText(time);
                DT4 = dt4.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT4", DT4);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String time = snapshot.getValue(String.class);
                dt5.setText(time);
                DT5 = dt5.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("DT5", DT5);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph1.setText(phVal);
                PH1 = ph1.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH1", PH1);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph2.setText(phVal);
                PH2 = ph2.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH2", PH2);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph3.setText(phVal);
                PH3 = ph3.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH3", PH3);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph4.setText(phVal);
                PH4 = ph4.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH4", PH4);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph5.setText(phVal);
                PH5 = ph5.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH5", PH5);
                myEdit.commit();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void initializeAllViews(View view) {
        fivePointCalib = view.findViewById(R.id.fivePointCalib);
        threePointCalib = view.findViewById(R.id.threePointCalib);

        phMvTable = view.findViewById(R.id.phMvTable);
        phGraph = view.findViewById(R.id.phGraph);
        printCalibData = view.findViewById(R.id.printCalibData);
        printAllCalibData = view.findViewById(R.id.printAllCalibData);
        calibrateBtn = view.findViewById(R.id.startBtn);
        calibrateBtnThree = view.findViewById(R.id.startBtnThree);
        modeText = view.findViewById(R.id.modeText);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvTimerThree = view.findViewById(R.id.tvTimerThree);
        calibSpinner = view.findViewById(R.id.calibSpinner);
        spin = view.findViewById(R.id.calibMode);
        log1_3 = view.findViewById(R.id.log1_3);
        log2_3 = view.findViewById(R.id.log2_3);
        log3_3 = view.findViewById(R.id.log3_3);

        syncOfflineData = view.findViewById(R.id.syncOfflineData);
        fivePointCalibStart = view.findViewById(R.id.fivePointCalibStart);
        threePointCalibStart = view.findViewById(R.id.threePointCalibStart);
        log1 = view.findViewById(R.id.log1);
        log2 = view.findViewById(R.id.log2);
        log3 = view.findViewById(R.id.log3);
        log4 = view.findViewById(R.id.log4);
        log5 = view.findViewById(R.id.log5);
        log3point = view.findViewById(R.id.log3point);
        log5point = view.findViewById(R.id.log5Point);

        ph1 = view.findViewById(R.id.ph1);
        ph2 = view.findViewById(R.id.ph2);
        ph3 = view.findViewById(R.id.ph3);
        ph4 = view.findViewById(R.id.ph4);
        ph5 = view.findViewById(R.id.ph5);

        phAfterCalib1 = view.findViewById(R.id.phAfterCalib1);
        phAfterCalib2 = view.findViewById(R.id.phAfterCalib2);
        phAfterCalib3 = view.findViewById(R.id.phAfterCalib3);
        phAfterCalib4 = view.findViewById(R.id.phAfterCalib4);
        phAfterCalib5 = view.findViewById(R.id.phAfterCalib5);

        mv1 = view.findViewById(R.id.mv1);
        mv2 = view.findViewById(R.id.mv2);
        mv3 = view.findViewById(R.id.mv3);
        mv4 = view.findViewById(R.id.mv4);
        mv5 = view.findViewById(R.id.mv5);

        temp1 = view.findViewById(R.id.temp1);
        temp2 = view.findViewById(R.id.temp2);
        temp3 = view.findViewById(R.id.temp3);
        temp4 = view.findViewById(R.id.temp4);
        temp5 = view.findViewById(R.id.temp5);

        qr1 = view.findViewById(R.id.qr1);
        qr2 = view.findViewById(R.id.qr2);
        qr3 = view.findViewById(R.id.qr3);
        qr4 = view.findViewById(R.id.qr4);
        qr5 = view.findViewById(R.id.qr5);

        bufferD1 = view.findViewById(R.id.bufferD1);
        bufferD2 = view.findViewById(R.id.bufferD2);
        bufferD3 = view.findViewById(R.id.bufferD3);
        bufferD4 = view.findViewById(R.id.bufferD4);
        bufferD5 = view.findViewById(R.id.bufferD5);


        slope1 = view.findViewById(R.id.slope1);
        slope2 = view.findViewById(R.id.slope2);
        slope3 = view.findViewById(R.id.slope3);
        slope4 = view.findViewById(R.id.slope4);
        slope5 = view.findViewById(R.id.slope5);

        bufferD1.setSelected(true);
        bufferD2.setSelected(true);
        bufferD3.setSelected(true);
        bufferD4.setSelected(true);
        bufferD5.setSelected(true);

        phEdit1 = view.findViewById(R.id.phEdit1);
        phEdit2 = view.findViewById(R.id.phEdit2);
        phEdit3 = view.findViewById(R.id.phEdit3);
        phEdit4 = view.findViewById(R.id.phEdit4);
        phEdit5 = view.findViewById(R.id.phEdit5);

        dt1 = view.findViewById(R.id.dt1);
        dt2 = view.findViewById(R.id.dt2);
        dt3 = view.findViewById(R.id.dt3);
        dt4 = view.findViewById(R.id.dt4);
        dt5 = view.findViewById(R.id.dt5);


        ph1_3 = view.findViewById(R.id.ph1_3);
        ph2_3 = view.findViewById(R.id.ph2_3);
        ph3_3 = view.findViewById(R.id.ph3_3);


        phAfterCalib1_3 = view.findViewById(R.id.phAfterCalib1_3);
        phAfterCalib2_3 = view.findViewById(R.id.phAfterCalib2_3);
        phAfterCalib3_3 = view.findViewById(R.id.phAfterCalib3_3);

        mv1_3 = view.findViewById(R.id.mv1_3);
        mv2_3 = view.findViewById(R.id.mv2_3);
        mv3_3 = view.findViewById(R.id.mv3_3);


        temp1_3 = view.findViewById(R.id.temp1_3);
        temp2_3 = view.findViewById(R.id.temp2_3);
        temp3_3 = view.findViewById(R.id.temp3_3);


        qr1_3 = view.findViewById(R.id.qr1_3);
        qr2_3 = view.findViewById(R.id.qr2_3);
        qr3_3 = view.findViewById(R.id.qr3_3);

        bufferD1_3 = view.findViewById(R.id.bufferD1_3);
        bufferD2_3 = view.findViewById(R.id.bufferD2_3);
        bufferD3_3 = view.findViewById(R.id.bufferD3_3);


        slope1_3 = view.findViewById(R.id.slope1_3);
        slope2_3 = view.findViewById(R.id.slope2_3);
        slope3_3 = view.findViewById(R.id.slope3_3);

        bufferD1_3.setSelected(true);
        bufferD2_3.setSelected(true);
        bufferD3_3.setSelected(true);


        calibRecyclerView = view.findViewById(R.id.rvCalibFileView);

        phEdit1_3 = view.findViewById(R.id.phEdit1_3);
        phEdit2_3 = view.findViewById(R.id.phEdit2_3);
        phEdit3_3 = view.findViewById(R.id.phEdit3_3);

        dt1_3 = view.findViewById(R.id.dt1_3);
        dt2_3 = view.findViewById(R.id.dt2_3);
        dt3_3 = view.findViewById(R.id.dt3_3);

        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        phView = view.findViewById(R.id.phView);
        tvEcCurr = view.findViewById(R.id.tvEcCurr);

        phEdit1.setOnClickListener(this::onClick);
        phEdit2.setOnClickListener(this::onClick);
        phEdit3.setOnClickListener(this::onClick);
        phEdit4.setOnClickListener(this::onClick);
        phEdit5.setOnClickListener(this::onClick);

        qr1.setOnClickListener(this::onClick);
        qr2.setOnClickListener(this::onClick);
        qr3.setOnClickListener(this::onClick);
        qr4.setOnClickListener(this::onClick);
        qr5.setOnClickListener(this::onClick);

        phEdit1_3.setOnClickListener(this::onClick);
        phEdit2_3.setOnClickListener(this::onClick);
        phEdit3_3.setOnClickListener(this::onClick);

        qr1_3.setOnClickListener(this::onClick);
        qr2_3.setOnClickListener(this::onClick);
        qr3_3.setOnClickListener(this::onClick);
        if (Constants.OFFLINE_MODE) {
            syncOfflineData.setVisibility(View.GONE);
        } else {
            syncOfflineData.setVisibility(View.VISIBLE);
        }
        syncOfflineData.setOnClickListener(v -> {
            syncOfflineWithOnline();
        });


    }

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.phEdit1:
                EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_1").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_1", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph1.setText(String.valueOf(ph));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
                dialog.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit2:

                EditPhBufferDialog dialog1 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_2", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph2.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog1.show(getParentFragmentManager(), null);
                break;
            case R.id.phEdit3:
                EditPhBufferDialog dialog2 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_3", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph3.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog2.show(getParentFragmentManager(), null);
                break;
            case R.id.phEdit4:
                EditPhBufferDialog dialog3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_4", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph4.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog3.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit5:
                EditPhBufferDialog dialog5 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_5").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_5", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph5.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog5.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit1_3:
                EditPhBufferDialog dialog_3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    System.out.println("1");
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_2", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph1_3.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog_3.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit2_3:

                EditPhBufferDialog dialog1_3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    System.out.println("2");
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_3", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph2_3.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog1_3.show(getParentFragmentManager(), null);
                break;
            case R.id.phEdit3_3:
                EditPhBufferDialog dialog2_3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    System.out.println("3");
                    if (!Constants.OFFLINE_MODE) {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").setValue(String.valueOf(ph));
                    } else {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("B_4", String.valueOf(ph));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            ph3_3.setText(String.valueOf(ph));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                dialog2_3.show(getParentFragmentManager(), null);
                break;
            case R.id.qr1:
                openQRActivity("qr1");
                break;
            case R.id.qr2:
                openQRActivity("qr2");
                break;
            case R.id.qr3:
                openQRActivity("qr3");
                break;
            case R.id.qr4:
                openQRActivity("qr4");
                break;
            case R.id.qr5:
                openQRActivity("qr5");
                break;
            case R.id.qr1_3:
                openQRActivity("qr2");
                break;
            case R.id.qr2_3:
                openQRActivity("qr3");
                break;
            case R.id.qr3_3:
                openQRActivity("qr4");
                break;
            default:
                break;
        }
    }

    private void updateBufferValue(Float value) {
        String newValue = String.valueOf(value);
    }

    private void openQRActivity(String view) {
        Intent intent = new Intent(getContext(), ProbeScanner.class);
        intent.putExtra("activity", "PhCalibFragment");
        intent.putExtra("view", view);
        startActivity(intent);
    }


    File[] reverseFileArray(File[] fileArray) {
        for (int i = 0; i < fileArray.length / 2; i++) {
            File a = fileArray[i];
            fileArray[i] = fileArray[fileArray.length - i - 1];
            fileArray[fileArray.length - i - 1] = a;
        }

        return fileArray.length > 0 ? fileArray : null;
    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket1 = client.newWebSocket(request, new SocketListener());
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            webSocket.cancel();
            webSocket1.cancel();
            Log.e("WebSocketClosed", "onFailure " + (response != null ? response.message().toString() : null) + " " + t.getMessage());

        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            webSocket.cancel();
            webSocket1.cancel();
            Log.e("WebSocketClosed", "onClosed " + reason.toString());
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosing(webSocket, code, reason);
            webSocket.cancel();
            webSocket1.cancel();
            Log.e("WebSocketClosed", "onClosing " + reason.toString());
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
//            webSocket1 = webSocket;

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            getActivity().runOnUiThread(() -> {
//                calibrateBtn.setEnabled(true);
                Toast.makeText(getContext(),
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();

            });

            try {
                jsonData.put("SOCKET_INIT", "Successfully Initialized on PhCalibFragment");
                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                webSocket.send(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Socket Connection Unsuccessful!",
                            Toast.LENGTH_SHORT).show();

                });
            }

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);

            if (webSocket1 == null) {
                webSocket.cancel();
            }


            getActivity().runOnUiThread(() -> {
                try {
                    jsonData = new JSONObject(text);
                    Log.d("JSONReceived:PHFragment", "onMessage: " + text);
                    if (spin.getSelectedItemPosition() == 0) {

                        if (jsonData.has("PH_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            float ph = Float.parseFloat(jsonData.getString("PH_VAL"));
                            String phForm = String.format(Locale.UK, "%.2f", ph);
                            tvPhCurr.setText(phForm);
                            phView.moveTo(ph);
                            AlarmConstants.PH = ph;

                        }

                        if (jsonData.has("TEMP_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            float ph = Float.parseFloat(jsonData.getString("TEMP_VAL"));
                            String tempForm = String.format(Locale.UK, "%.1f", ph);
                            tvTempCurr.setText(tempForm + "°C");

                            if (ph <= -127.0) {
                                tvTempCurr.setText("NA");
                            }
                        }

                        if (jsonData.has("EC_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("EC_VAL");
                            tvEcCurr.setText(val);
                        }

                        if (jsonData.has("MV_1") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {


                            String val = jsonData.getString("MV_1");
                            String ecForm = "0";
                            if (val == "nan") {
                                ecForm = "nan";
                            } else {
                                ecForm = String.format(Locale.UK, "%.2f", Float.parseFloat(val));

                            }
                            mv1.setText(ecForm);
                            mV1 = mv1.getText().toString();
                            Log.d("test1", mV1);

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV1", mV1);
                            myEdit.commit();
                        }

                        if (jsonData.has("MV_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {


                            String val = jsonData.getString("MV_2");
                            String ecForm = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv2.setText(ecForm);
                            mV2 = mv2.getText().toString();
                            Log.d("test2", mV2);

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV2", mV2);
                            myEdit.commit();
                        }


                        if (jsonData.has("MV_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {


                            String val = jsonData.getString("MV_3");
                            String ecForm = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv3.setText(ecForm);
                            mV3 = mv3.getText().toString();
                            Log.d("test3", mV3);

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV3", mV3);
                            myEdit.commit();
                        }

                        if (jsonData.has("MV_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {


                            String val = jsonData.getString("MV_4");
                            String ecForm = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv4.setText(ecForm);
                            mV4 = mv4.getText().toString();
                            Log.d("test4", mV4);

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV4", mV4);
                            myEdit.commit();
                        }

                        if (jsonData.has("MV_5") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("MV_5");
                            String ecForm = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv5.setText(ecForm);
                            mV5 = mv5.getText().toString();
                            Log.d("test5", mV5);

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV5", mV5);
                            myEdit.commit();
                        }

                        if (jsonData.has("POST_VAL_1") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("POST_VAL_1");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));

                            phAfterCalib1.setText(v);
                            pHAC1 = phAfterCalib1.getText().toString();


                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC1", pHAC1);
                            myEdit.commit();
                        }
                        if (jsonData.has("POST_VAL_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("POST_VAL_2");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));

                            phAfterCalib2.setText(v);
                            pHAC2 = phAfterCalib2.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC2", pHAC2);
                            myEdit.commit();
                        }

                        if (jsonData.has("POST_VAL_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("POST_VAL_3");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));

                            phAfterCalib3.setText(v);
                            pHAC3 = phAfterCalib3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC3", pHAC3);
                            myEdit.commit();
                        }

                        if (jsonData.has("POST_VAL_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("POST_VAL_4");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));

                            phAfterCalib4.setText(v);
                            pHAC4 = phAfterCalib4.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC4", pHAC4);
                            myEdit.commit();
                        }

                        if (jsonData.has("POST_VAL_5") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("POST_VAL_5");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));

                            phAfterCalib5.setText(v);
                            pHAC5 = phAfterCalib5.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC5", pHAC5);
                            myEdit.commit();
                        }

                        if (jsonData.has("DT_1") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("DT_1");
                            dt1.setText(val);
                            DT1 = dt1.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT1", DT1);
                            myEdit.commit();
                        }

                        if (jsonData.has("DT_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("DT_2");
                            dt2.setText(val);
                            DT2 = dt2.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT2", DT2);
                            myEdit.commit();
                        }
                        if (jsonData.has("DT_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("DT_3");
                            dt3.setText(val);
                            DT3 = dt3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT3", DT3);
                            myEdit.commit();
                        }
                        if (jsonData.has("DT_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("DT_4");
                            dt4.setText(val);
                            DT4 = dt4.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT4", DT4);
                            myEdit.commit();
                        }
                        if (jsonData.has("DT_5") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("DT_5");
                            dt5.setText(val);
                            DT5 = dt5.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT5", DT5);
                            myEdit.commit();
                        }


                        if (jsonData.has("B_1") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("B_1");
                            ph1.setText(val);
                            PH1 = ph1.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH1", PH1);
                            myEdit.commit();
                        }

                        if (jsonData.has("B_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("B_2");
                            ph2.setText(val);
                            PH2 = ph2.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH2", PH2);
                            myEdit.commit();
                        }

                        if (jsonData.has("B_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("B_3");
                            ph3.setText(val);
                            PH3 = ph3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH3", PH3);
                            myEdit.commit();
                        }

                        if (jsonData.has("B_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("B_4");
                            ph4.setText(val);
                            PH4 = ph4.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH4", PH4);
                            myEdit.commit();
                        }

                        if (jsonData.has("B_5") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("B_5");
                            ph5.setText(val);
                            PH5 = ph5.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH5", PH5);
                            myEdit.commit();
                        }

                        if (jsonData.has("CAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("CAL");
                            ec = Integer.parseInt(val);
                            Log.d("ECVal", "onDataChange: " + ec);
//                            stateChangeModeFive();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            if (jsonData.getString("CAL").equals("11") && jsonData.has("POST_VAL_1")) {
                                String d = jsonData.getString("POST_VAL_1");
                                phAfterCalib1.setText(d);
                                myEdit.putString("tem1", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC1", d);
                                myEdit.commit();
//                                deviceRef.child("Data").child("CALIBRATION_STAT").setValue("incomplete");

                                temp1.setText(tvTempCurr.getText());
                            } else if (jsonData.getString("CAL").equals("21") && jsonData.has("POST_VAL_2")) {
                                String d = jsonData.getString("POST_VAL_2");
                                phAfterCalib2.setText(d);
                                myEdit.putString("tem2", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC2", d);
                                myEdit.commit();

                                temp2.setText(tvTempCurr.getText());
                            } else if (jsonData.getString("CAL").equals("31") && jsonData.has("POST_VAL_3")) {
                                String d = jsonData.getString("POST_VAL_3");
                                phAfterCalib3.setText(d);
                                myEdit.putString("tem3", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC3", d);
                                myEdit.commit();

                                temp3.setText(tvTempCurr.getText());
                            } else if (jsonData.getString("CAL").equals("41") && jsonData.has("POST_VAL_4")) {
                                String d = jsonData.getString("POST_VAL_4");
                                phAfterCalib4.setText(String.valueOf(d));
                                myEdit.putString("tem4", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC4", String.valueOf(d));
                                myEdit.commit();

                                temp4.setText(tvTempCurr.getText());
                            } else if (jsonData.getString("CAL").equals("51") && jsonData.has("POST_VAL_5")) {
                                String d = jsonData.getString("POST_VAL_5");
                                phAfterCalib5.setText(String.valueOf(d));
                                myEdit.putString("tem5", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC5", String.valueOf(d));
                                myEdit.commit();
                                temp5.setText(tvTempCurr.getText());
//                                deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");
                                calibData();
                            }
                        }

                    } else if (spin.getSelectedItemPosition() == 1) {

                        if (jsonData.has("POST_VAL_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("POST_VAL_2");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            phAfterCalib1_3.setText(v);
                            pHAC1_3 = phAfterCalib1_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC1_3", pHAC1_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("POST_VAL_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("POST_VAL_3");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            phAfterCalib2_3.setText(v);
                            pHAC2_3 = phAfterCalib2_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC2_3", pHAC2_3);
                            myEdit.commit();
                        }
                        if (jsonData.has("POST_VAL_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("POST_VAL_4");
                            String v = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            phAfterCalib3_3.setText(v);
                            pHAC3_3 = phAfterCalib3_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("pHAC3_3", pHAC3_3);
                            myEdit.commit();
                        }
                        if (jsonData.has("PH_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            float ph = Float.parseFloat(jsonData.getString("PH_VAL"));
                            String phForm = String.format(Locale.UK, "%.2f", ph);
                            tvPhCurr.setText(phForm);
                            phView.moveTo(ph);

                        }

                        if (jsonData.has("TEMP_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            float ph = Float.parseFloat(jsonData.getString("TEMP_VAL"));
                            String tempForm = String.format(Locale.UK, "%.1f", ph);
                            tvTempCurr.setText(tempForm + "°C");

                            if (ph <= -127.0) {
                                tvTempCurr.setText("NA");
                            }
                        }

                        if (jsonData.has("EC_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String ph = jsonData.getString("EC_VAL");
                            tvEcCurr.setText(ph);

                        }

                        if (jsonData.has("MV_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("MV_2");
                            String e = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv1_3.setText(e);
                            mV1_3 = mv1_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV1_3", mV1_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("MV_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("MV_3");
                            String e = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv2_3.setText(e);
                            mV2_3 = mv2_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV2_3", mV2_3);
                            myEdit.commit();
                        }
                        if (jsonData.has("MV_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("MV_4");
                            String e = String.format(Locale.UK, "%.2f", Float.parseFloat(val));
                            mv3_3.setText(e);
                            mV3_3 = mv3_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("MV3_3", mV3_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("DT_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("DT_2");
                            dt1_3.setText(val);
                            DT1_3 = dt1_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT1_3", DT1_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("DT_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("DT_3");
                            dt2_3.setText(val);
                            DT2_3 = dt2_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT2_3", DT2_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("DT_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("DT_4");
                            dt3_3.setText(val);
                            DT3_3 = dt3_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("DT3_3", DT3_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("B_2") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("B_2");
                            ph1_3.setText(val);
                            PH1_3 = ph1_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH1_3", PH1_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("B_3") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("B_3");
                            ph2_3.setText(val);
                            PH2_3 = ph2_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH2_3", PH2_3);
                            myEdit.commit();
                        }
                        if (jsonData.has("B_4") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                            String val = jsonData.getString("B_4");
                            ph3_3.setText(val);
                            PH3_3 = ph3_3.getText().toString();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();

                            myEdit.putString("PH3_3", PH3_3);
                            myEdit.commit();
                        }

                        if (jsonData.has("CAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                            String val = jsonData.getString("CAL");
                            ec = Integer.parseInt(val);
                            Log.d("ECVal", "onDataChange: " + ec);
//                            stateChangeModeFive();

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            if (jsonData.getString("CAL").equals("21") && jsonData.has("POST_VAL_2")) {
                                String d = jsonData.getString("POST_VAL_2");
                                phAfterCalib1_3.setText(d);
                                myEdit.putString("tem1_3", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC1_3", d);
                                myEdit.commit();


                                temp1_3.setText(tvTempCurr.getText());
                            } else if (jsonData.getString("CAL").equals("31") && jsonData.has("POST_VAL_3")) {
                                String d = jsonData.getString("POST_VAL_3");
                                phAfterCalib2_3.setText(d);
                                myEdit.putString("tem2_3", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC2_3", d);
                                myEdit.commit();

                                temp2_3.setText(tvTempCurr.getText());
                            } else if (jsonData.getString("CAL").equals("41") && jsonData.has("POST_VAL_4")) {
                                String d = jsonData.getString("POST_VAL_4");
                                phAfterCalib3_3.setText(String.valueOf(d));
                                myEdit.putString("tem3_3", tvTempCurr.getText().toString());
                                myEdit.putString("pHAC3_3", String.valueOf(d));
                                myEdit.commit();

                                temp3_3.setText(tvTempCurr.getText());
                                calibData3();
                            }
                        }


                    }


                    if (Constants.OFFLINE_MODE) {
                        offlineDataFeeding();
                    }

                    if (jsonData.has("FAULT") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                        String val = jsonData.getString("FAULT");
                        fault = Integer.parseInt(val);
                        if (fault != null)
                            if (fault == 1) {
                                showAlertDialogButtonClicked();
                            }
                    }

//                    progressDialog.dismiss();
//                    calibrateBtn.setEnabled(true);
//
//                    if (Constants.OFFLINE_MODE){
//                        calibrateBtn.setOnClickListener(v -> {
//                            calibrateFivePointOffline(webSocket);
//                        });
//                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            });

        }

    }

    @Override
    public void onResume() {

        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        BFD1 = shp.getString("BFD1", "");
        BFD2 = BFD1_3 = shp.getString("BFD2", "");
        BFD3 = BFD2_3 = shp.getString("BFD3", "");
        BFD4 = BFD3_3 = shp.getString("BFD4", "");
        BFD5 = shp.getString("BFD5", "");

        bufferD1.setText(BFD1);
        bufferD2.setText(BFD2);
        bufferD3.setText(BFD3);
        bufferD1_3.setText(BFD1_3);
        bufferD2_3.setText(BFD2_3);
        bufferD3_3.setText(BFD3_3);
        bufferD4.setText(BFD4);
        bufferD5.setText(BFD5);

//        if (!Constants.OFFLINE_MODE) {
//            fetchAllDataFromFirebase();
//            fetchAllData5Point();
//            fetchAllData3Point();
//            getAllMvData();
//        } else {
////            tvPhCurr.setText("");
////            tvTempCurr.setText("");
////            tvEcCurr.setText("");
////            offsetCurr.setText("");
////            batteryCurr.setText("");
////            slopeCurr.setText("");
//
//            initiateSocketConnection();
//
//        }
        if (Constants.OFFLINE_MODE) {
            offlineDataFeeding();
        }
        super.onResume();
    }

    @Override
    public void onStart() {
//        initiateSocketConnection();
        if (Constants.OFFLINE_MODE) {
            initiateSocketConnection();
        }
        if (Constants.OFFLINE_MODE) {
            try {
                jsonData = new JSONObject();
                jsonData.put("CAL_MODE", String.valueOf(5));
                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                webSocket1.send(jsonData.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
        }
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mode = "5";
                        fivePointCalib.setVisibility(View.VISIBLE);
                        threePointCalib.setVisibility(View.GONE);
                        threePointCalibStart.setVisibility(View.GONE);
                        fivePointCalibStart.setVisibility(View.VISIBLE);
                        currentBuf = 0;
                        currentBufThree = 0;
                        line = 0;
                        line_3 = 0;
                        if (Constants.OFFLINE_MODE) {
                            try {
                                jsonData = new JSONObject();
                                jsonData.put("CAL_MODE", String.valueOf(5));
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket1.send(jsonData.toString());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                        }
                        log1_3.setBackgroundColor(Color.GRAY);
                        log2_3.setBackgroundColor(Color.WHITE);
                        log3_3.setBackgroundColor(Color.WHITE);

                        log1.setBackgroundColor(Color.GRAY);
                        log2.setBackgroundColor(Color.WHITE);
                        log3.setBackgroundColor(Color.WHITE);
                        log4.setBackgroundColor(Color.WHITE);
                        log5.setBackgroundColor(Color.WHITE);

                        if (Constants.OFFLINE_MODE) {
                            offlineDataFeeding();
                        } else {

                            fetchAllData5Point();
                        }
                        deleteAllCalibData();
                        calibData();

                        databaseHelper.insertCalibration(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1, DT1.length() >= 15 ? DT1.substring(0, 10) : "--", DT1.length() >= 15 ? DT1.substring(11, 16) : "--");
                        databaseHelper.insertCalibration(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2, DT2.length() >= 15 ? DT2.substring(0, 10) : "--", DT2.length() >= 15 ? DT2.substring(11, 16) : "--");
                        databaseHelper.insertCalibration(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3, DT3.length() >= 15 ? DT3.substring(0, 10) : "--", DT3.length() >= 15 ? DT3.substring(11, 16) : "--");
                        databaseHelper.insertCalibration(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4, DT4.length() >= 15 ? DT4.substring(0, 10) : "--", DT4.length() >= 15 ? DT4.substring(11, 16) : "--");
                        databaseHelper.insertCalibration(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5, DT5.length() >= 15 ? DT5.substring(0, 10) : "--", DT5.length() >= 15 ? DT5.substring(11, 16) : "--");

                        if (Constants.OFFLINE_MODE) {

                            deleteAllOfflineCalibData();

                            databaseHelper.insertCalibrationOfflineData(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1, DT1.length() >= 15 ? DT1.substring(0, 10) : "--", DT1.length() >= 15 ? DT1.substring(11, 16) : "--");
                            databaseHelper.insertCalibrationOfflineData(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2, DT2.length() >= 15 ? DT2.substring(0, 10) : "--", DT2.length() >= 15 ? DT2.substring(11, 16) : "--");
                            databaseHelper.insertCalibrationOfflineData(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3, DT3.length() >= 15 ? DT3.substring(0, 10) : "--", DT3.length() >= 15 ? DT3.substring(11, 16) : "--");
                            databaseHelper.insertCalibrationOfflineData(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4, DT4.length() >= 15 ? DT4.substring(0, 10) : "--", DT4.length() >= 15 ? DT4.substring(11, 16) : "--");
                            databaseHelper.insertCalibrationOfflineData(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5, DT5.length() >= 15 ? DT5.substring(0, 10) : "--", DT5.length() >= 15 ? DT5.substring(11, 16) : "--");


                        }
                        if (Constants.OFFLINE_MODE) {
                            offlineDataFeeding();
                        }
                        break;

                    case 1:
                        mode = "3";
                        fivePointCalib.setVisibility(View.GONE);
                        fivePointCalibStart.setVisibility(View.GONE);
                        threePointCalib.setVisibility(View.VISIBLE);
                        threePointCalibStart.setVisibility(View.VISIBLE);
//                        currentBufThree = 0;

                        currentBuf = 0;
                        currentBufThree = 0;
                        line = 0;
                        line_3 = 0;
                        if (Constants.OFFLINE_MODE) {
                            try {
                                jsonData = new JSONObject();
                                jsonData.put("CAL_MODE", String.valueOf(3));
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket1.send(jsonData.toString());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                        }
                        log1_3.setBackgroundColor(Color.GRAY);
                        log2_3.setBackgroundColor(Color.WHITE);
                        log3_3.setBackgroundColor(Color.WHITE);

                        log1.setBackgroundColor(Color.GRAY);
                        log2.setBackgroundColor(Color.WHITE);
                        log3.setBackgroundColor(Color.WHITE);
                        log4.setBackgroundColor(Color.WHITE);
                        log5.setBackgroundColor(Color.WHITE);
                        if (Constants.OFFLINE_MODE) {
                            offlineDataFeeding();
                        } else {
                            fetchAllData3Point();
                        }
                        deleteAllCalibData();
                        calibData3();


                        databaseHelper.insertCalibration(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");
                        databaseHelper.insertCalibration(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");
                        databaseHelper.insertCalibration(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");
                        if (Constants.OFFLINE_MODE) {
                            deleteAllOfflineCalibData();

                            databaseHelper.insertCalibrationOfflineData(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3, DT1_3.length() >= 15 ? DT1_3.substring(0, 10) : "--", DT1_3.length() >= 15 ? DT1_3.substring(11, 16) : "--");
                            databaseHelper.insertCalibrationOfflineData(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3, DT2_3.length() >= 15 ? DT2_3.substring(0, 10) : "--", DT2_3.length() >= 15 ? DT2_3.substring(11, 16) : "--");
                            databaseHelper.insertCalibrationOfflineData(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3, DT3_3.length() >= 15 ? DT3_3.substring(0, 10) : "--", DT3_3.length() >= 15 ? DT3_3.substring(11, 16) : "--");
                        }

                        if (Constants.OFFLINE_MODE) {
                            offlineDataFeeding();
                        }
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(requireContext(), "Select a mode of Calibration", Toast.LENGTH_SHORT).show();
            }
        });

        super.onStart();
    }

    @Override
    public void onStop() {
        if (Constants.OFFLINE_MODE) {
            webSocket1.cancel();
        }
        super.onStop();
    }

    void offlineDataFeeding() {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor calibCSV3;
        Cursor calibCSV5;

        calibCSV3 = db.rawQuery("SELECT * FROM CalibOfflineDataThree", null);
        calibCSV5 = db.rawQuery("SELECT * FROM CalibOfflineDataFive", null);

        int index = 0;

        if (calibCSV3.getCount() == 0) {

            databaseHelper.insertCalibrationOfflineDataThree(1, ph1_3.getText().toString(),
                    mv1_3.getText().toString(), slope1_3.getText().toString(), dt1_3.getText().toString(),
                    bufferD1_3.getText().toString(), phAfterCalib1_3.getText().toString(), tvTempCurr.getText().toString(),
                    dt1_3.getText().toString().length() >= 15 ? dt1_3.getText().toString().substring(0, 10) : "--",
                    dt1_3.getText().toString().length() >= 15 ? dt1_3.getText().toString().substring(11, 16) : "--");

            databaseHelper.insertCalibrationOfflineDataThree(2, ph2_3.getText().toString(),
                    mv2_3.getText().toString(), slope2_3.getText().toString(), dt2_3.getText().toString(),
                    bufferD2_3.getText().toString(), phAfterCalib2_3.getText().toString(), tvTempCurr.getText().toString(),
                    dt2_3.getText().toString().length() >= 15 ? dt2_3.getText().toString().substring(0, 10) : "--",
                    dt2_3.getText().toString().length() >= 15 ? dt2_3.getText().toString().substring(11, 16) : "--");


            databaseHelper.insertCalibrationOfflineDataThree(3, ph3_3.getText().toString(),
                    mv3_3.getText().toString(), slope3_3.getText().toString(), dt3_3.getText().toString(),
                    bufferD3_3.getText().toString(), phAfterCalib3_3.getText().toString(), tvTempCurr.getText().toString(),
                    dt3_3.getText().toString().length() >= 15 ? dt3_3.getText().toString().substring(0, 10) : "--",
                    dt3_3.getText().toString().length() >= 15 ? dt3_3.getText().toString().substring(11, 16) : "--");

        }

        if (calibCSV5.getCount() == 0) {

            databaseHelper.insertCalibrationOfflineDataFive(1, ph1.getText().toString(),
                    mv1.getText().toString(), slope1.getText().toString(), dt1.getText().toString(),
                    bufferD1.getText().toString(), phAfterCalib1.getText().toString(), tvTempCurr.getText().toString(),
                    dt1.getText().toString().length() >= 15 ? dt1.getText().toString().substring(0, 10) : "--",
                    dt1.getText().toString().length() >= 15 ? dt1.getText().toString().substring(11, 16) : "--");


            databaseHelper.insertCalibrationOfflineDataFive(2, ph2.getText().toString(),
                    mv2.getText().toString(), slope2.getText().toString(), dt2.getText().toString(),
                    bufferD2.getText().toString(), phAfterCalib2.getText().toString(), tvTempCurr.getText().toString(),
                    dt2.getText().toString().length() >= 15 ? dt2.getText().toString().substring(0, 10) : "--",
                    dt2.getText().toString().length() >= 15 ? dt2.getText().toString().substring(11, 16) : "--");


            databaseHelper.insertCalibrationOfflineDataFive(3, ph3.getText().toString(),
                    mv3.getText().toString(), slope3.getText().toString(), dt3.getText().toString(),
                    bufferD3.getText().toString(), phAfterCalib3.getText().toString(), tvTempCurr.getText().toString(),
                    dt3.getText().toString().length() >= 15 ? dt3.getText().toString().substring(0, 10) : "--",
                    dt3.getText().toString().length() >= 15 ? dt3.getText().toString().substring(11, 16) : "--");


            databaseHelper.insertCalibrationOfflineDataFive(4, ph4.getText().toString(),
                    mv4.getText().toString(), slope4.getText().toString(), dt4.getText().toString(),
                    bufferD4.getText().toString(), phAfterCalib4.getText().toString(), tvTempCurr.getText().toString(),
                    dt4.getText().toString().length() >= 15 ? dt4.getText().toString().substring(0, 10) : "--",
                    dt4.getText().toString().length() >= 15 ? dt4.getText().toString().substring(11, 16) : "--");


            databaseHelper.insertCalibrationOfflineDataFive(5, ph5.getText().toString(),
                    mv5.getText().toString(), slope5.getText().toString(), dt5.getText().toString(),
                    bufferD5.getText().toString(), phAfterCalib5.getText().toString(), tvTempCurr.getText().toString(),
                    dt5.getText().toString().length() >= 15 ? dt5.getText().toString().substring(0, 10) : "--",
                    dt5.getText().toString().length() >= 15 ? dt5.getText().toString().substring(11, 16) : "--");


        }

        int index5 = 0;
        while (calibCSV5.moveToNext()) {
            String ph = calibCSV5.getString(calibCSV5.getColumnIndex("PH"));
            String mv = calibCSV5.getString(calibCSV5.getColumnIndex("MV"));
            String date = calibCSV5.getString(calibCSV5.getColumnIndex("DT"));
            String slope = calibCSV5.getString(calibCSV5.getColumnIndex("SLOPE"));
            String pHAC = calibCSV5.getString(calibCSV5.getColumnIndex("pHAC"));
            String temperature1 = calibCSV5.getString(calibCSV5.getColumnIndex("temperature"));

            Log.d("Cursor Data", "PH: " + calibCSV5.getString(calibCSV5.getColumnIndex("PH")));


            if (index5 == 0) {
                PH1 = ph;
                MV1 = mv;
                SLOPE1 = slope;
                DT1 = date;
                pHAC1 = pHAC;
                t1 = temperature1;

                ph1.setText(ph);
                mv1.setText(mv);
                slope1.setText(slope);
                dt1.setText(date);
                phAfterCalib1.setText(pHAC);
                temp1.setText(temperature1);


            }
            if (index5 == 1) {
                PH2 = ph;
                MV2 = mv;
                SLOPE2 = slope;
                DT2 = date;
                pHAC2 = pHAC;
                t2 = temperature1;

                ph2.setText(ph);
                mv2.setText(mv);
                slope2.setText(slope);
                dt2.setText(date);
                phAfterCalib2.setText(pHAC);
                temp2.setText(temperature1);

            }
            if (index5 == 2) {
                PH3 = ph;
                MV3 = mv;
                SLOPE3 = slope;
                DT3 = date;
                pHAC3 = pHAC;
                t3 = temperature1;

                ph3.setText(ph);
                mv3.setText(mv);
                slope3.setText(slope);
                dt3.setText(date);
                phAfterCalib3.setText(pHAC);
                temp3.setText(temperature1);

            }
            if (index5 == 3) {
                PH4 = ph;
                MV4 = mv;
                SLOPE4 = slope;
                DT4 = date;
                pHAC4 = pHAC;
                t4 = temperature1;

                ph4.setText(ph);
                mv4.setText(mv);
                slope4.setText(slope);
                dt4.setText(date);
                phAfterCalib4.setText(pHAC);
                temp4.setText(temperature1);

            }
            if (index5 == 4) {
                PH5 = ph;
                MV5 = mv;
                SLOPE5 = slope;
                DT5 = date;
                pHAC5 = pHAC;
                t5 = temperature1;

                ph5.setText(ph);
                mv5.setText(mv);
                slope5.setText(slope);
                dt5.setText(date);
                phAfterCalib5.setText(pHAC);
                temp5.setText(temperature1);
            }

            index5++;
        }


        while (calibCSV3.moveToNext()) {
            String ph = calibCSV3.getString(calibCSV3.getColumnIndex("PH"));
            String mv = calibCSV3.getString(calibCSV3.getColumnIndex("MV"));
            String date = calibCSV3.getString(calibCSV3.getColumnIndex("DT"));
            String slope = calibCSV3.getString(calibCSV3.getColumnIndex("SLOPE"));
            String pHAC = calibCSV3.getString(calibCSV3.getColumnIndex("pHAC"));
            String temperature1 = calibCSV3.getString(calibCSV3.getColumnIndex("temperature"));

            if (index == 0) {

                ph1_3.setText(ph);
                mv1_3.setText(mv);
                slope1_3.setText(slope);
                dt1_3.setText(date);
                phAfterCalib1_3.setText(pHAC);
                temp1_3.setText(temperature1);

                PH1_3 = ph;
                MV1_3 = mv;
                SLOPE1_3 = slope;
                DT1_3 = date;
                pHAC1_3 = pHAC;
                t1_3 = temperature1;
            }
            if (index == 1) {

                ph2_3.setText(ph);
                mv2_3.setText(mv);
                slope2_3.setText(slope);
                dt2_3.setText(date);
                phAfterCalib2_3.setText(pHAC);
                temp2_3.setText(temperature1);


                PH2_3 = ph;
                MV2_3 = mv;
                SLOPE2_3 = slope;
                DT2_3 = date;
                pHAC2_3 = pHAC;
                t2_3 = temperature1;
            }
            if (index == 2) {

                ph3_3.setText(ph);
                mv3_3.setText(mv);
                slope3_3.setText(slope);
                dt3_3.setText(date);
                phAfterCalib3_3.setText(pHAC);
                temp3_3.setText(temperature1);

                PH3_3 = ph;
                MV3_3 = mv;
                SLOPE3_3 = slope;
                DT3_3 = date;
                pHAC3_3 = pHAC;
                t3_3 = temperature1;
            }

            index++;
        }

    }

    private void syncOfflineWithOnline() {
        if (Dashboard.isConnected && !Constants.OFFLINE_MODE) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV;

            calibCSV = db.rawQuery("SELECT * FROM CalibOfflineData", null);

            String[] bufferLabels1 = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
            String[] bufferLabelsThree1 = new String[]{"B_2", "B_3", "B_4"};
            String[] coeffLabels1 = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
            String[] postCoeffLabels1 = new String[]{"POST_VAL_1", "POST_VAL_2", "POST_VAL_3", "POST_VAL_4", "POST_VAL_5"};
            String[] postCoeffLabelsThree1 = new String[]{"POST_VAL_2", "POST_VAL_3", "POST_VAL_4"};
            String[] coeffLabelsThree1 = new String[]{"VAL_2", "VAL_3", "VAL_4"};
            String[] mvS = new String[]{"MV_1", "MV_2", "MV_3", "MV_4", "MV_5"};
            String[] dateS = new String[]{"DT_1", "DT_2", "DT_3", "DT_4", "DT_5"};
            String[] SlopeS = new String[]{"SLOPE_1", "SLOPE_2", "SLOPE_3", "SLOPE_4"};

            int index = 0;


            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
                String slope = calibCSV.getString(calibCSV.getColumnIndex("SLOPE"));
                String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
                String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));


                deviceRef.child("UI").child("PH").child("PH_CAL").child(bufferLabels1[index]).setValue(ph);
                deviceRef.child("UI").child("PH").child("PH_CAL").child(dateS[index]).setValue(date);
                deviceRef.child("UI").child("PH").child("PH_CAL").child(mvS[index]).setValue(Float.parseFloat(mv));
                deviceRef.child("UI").child("PH").child("PH_CAL").child(postCoeffLabels1[index]).setValue(Float.parseFloat(pHAC));
                if (index > 0) {
                    deviceRef.child("UI").child("PH").child("PH_CAL").child(SlopeS[index - 1]).setValue(Float.parseFloat(slope));
                }
                deviceRef.child("Data").child("PH_VAL").setValue(Float.parseFloat(ph));
                if (index == 0) {
                    deviceRef.child("Data").child("TEMP_VAL").setValue(Float.parseFloat(temperature1.replace("°C", "")));
                }
//                deviceRef.child("Data").child("EC_VAL").setValue(Float.parseFloat(temperature1));
                index++;
            }


        } else {
            if (!Dashboard.isConnected) {
                Toast.makeText(getContext(), "Your device is not connected with any internet connection", Toast.LENGTH_LONG).show();
            }
            if (Constants.OFFLINE_MODE) {
                Toast.makeText(getContext(), "Currently your device is in offline mode, so data can sync", Toast.LENGTH_LONG).show();
            }
        }
    }
}