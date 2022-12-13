package com.aican.aicanapp.fragments.ph;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.ProbeScan.ProbeScanner;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.CalibFileAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PHCalibGraph;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.PhMvTable;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

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

public class PhCalibFragmentNew extends Fragment {

    private static float LOG_INTERVAL = 0;
    private static float LOG_INTERVAL_3 = 0;
    Handler handler1;
    Runnable runnable1;

    Handler handler2;
    Runnable runnable2;

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
    Button calibrateBtn, calibrateBtnThree,printCalibData,phMvTable,phGraph;
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

        fetchAllDataFromFirebase();
        fetchAllData5Point();
        fetchAllData3Point();

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
//                        currentBuf = 0;
                        fetchAllData5Point();
                        deleteAllCalibData();
                        calibData();
                        databaseHelper.insertCalibration(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1);
                        databaseHelper.insertCalibration(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2);
                        databaseHelper.insertCalibration(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3);
                        databaseHelper.insertCalibration(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4);
                        databaseHelper.insertCalibration(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5);

                        break;

                    case 1:
                        mode = "3";
                        fivePointCalib.setVisibility(View.GONE);
                        fivePointCalibStart.setVisibility(View.GONE);
                        threePointCalib.setVisibility(View.VISIBLE);
                        threePointCalibStart.setVisibility(View.VISIBLE);
//                        currentBufThree = 0;
                        fetchAllData3Point();
                        deleteAllCalibData();
                        calibData3();
                        databaseHelper.insertCalibration(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3);
                        databaseHelper.insertCalibration(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3);
                        databaseHelper.insertCalibration(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3);

                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(requireContext(), "Select a mode of Calibration", Toast.LENGTH_SHORT).show();
            }
        });

        DialogMain dialogMain = new DialogMain();
        dialogMain.setCancelable(false);
        Source.userTrack = "PhCalibPage logged : ";
        if (Source.subscription.equals("cfr")) {


            dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
        }

        calibrateBtn.setOnClickListener(v -> {
            calibrateFivePoint();
        });

        calibrateBtnThree.setOnClickListener(v -> {
            calibrateThreePoint();
        });

        phMvTable.setOnClickListener(v ->{
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

        printCalibData.setOnClickListener(v ->{
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

        phGraph.setOnClickListener(v ->{
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

        calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), reverseFileArray(filesAndFolders));
        calibRecyclerView.setAdapter(calibFileAdapter);
        calibFileAdapter.notifyDataSetChanged();
        calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

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
        String user_name = "Username: " + Source.logUserName;
        String device_id = "DeviceID: " + deviceID;

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
//        document.add(new Paragraph(text).add(text1).add(text2));
        document.add(new Paragraph(company_name + "\n" + user_name + "\n" + device_id));
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

        Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);


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


    private void calibrateThreePoint() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        calibrateBtnThree.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtnThree.setEnabled(false);

        tvTimerThree.setVisibility(View.VISIBLE);
        isCalibrating = true;

//        startTimer();


        CountDownTimer timer = new CountDownTimer(1500, 1000) { //45000
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimerThree.setText(time);
                Log.e("lineNThree", line + "");
                if (line_3 == -1) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                }
                if (line_3 == 0) {
                    log1_3.setBackgroundColor(Color.GRAY);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                }
                if (line_3 == 1) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.GRAY);
                    log3_3.setBackgroundColor(Color.WHITE);
                }
                if (line_3 == 2) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.GRAY);
                }

                if (line_3 > 2) {
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
                }
                calibrateBtnThree.setEnabled(false);


            }

            final Handler handler = new Handler();
            Runnable runnable;

            @Override
            public void onFinish() {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        line_3 = currentBufThree + 1;

                        if (currentBufThree == 2) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(strDate);
                            calibrateBtnThree.setEnabled(false);
                            calibrateBtnThree.setText("DONE");
                            startTimer3();

                        }

                        if (currentBufThree == 0) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(strDate);
                        }
                        if (currentBufThree == 1) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(strDate);
                        }


                        calibrateBtnThree.setEnabled(true);

                        tvTimerThree.setVisibility(View.INVISIBLE);
                        String currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
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
                                    myEdit.putString("pHAC1__3", String.valueOf(postCoeff));
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
                                    deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");

                                    temp3_3.setText(tvTempCurr.getText());
                                }
                                currentBufThree += 1;
                                calibData3();
                                deleteAllCalibData();
                                databaseHelper.insertCalibration(PH1_3, MV1_3, SLOPE1_3, DT1_3, BFD1_3, pHAC1_3, t1_3);
                                databaseHelper.insertCalibration(PH2_3, MV2_3, SLOPE2_3, DT2_3, BFD2_3, pHAC2_3, t2_3);
                                databaseHelper.insertCalibration(PH3_3, MV3_3, SLOPE3_3, DT3_3, BFD3_3, pHAC3_3, t3_3);


                            });
                        });
                    }

                };

                runnable.run();
            }
        };

        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
            timer.start();
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
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
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
                    log1_3.setBackgroundColor(Color.WHITE);
                    log2_3.setBackgroundColor(Color.WHITE);
                    log3_3.setBackgroundColor(Color.WHITE);
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

    private static int line = 0;
    private static int line_3 = 0;

    private void calibrateFivePoint() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtn.setEnabled(false);

        tvTimer.setVisibility(View.VISIBLE);
        isCalibrating = true;

//        startTimer();


        CountDownTimer timer = new CountDownTimer(1500, 1000) { //45000
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimer.setText(time);
                Log.e("lineN", line + "");
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
                }
                if (line == 1) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.GRAY);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                }
                if (line == 2) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.GRAY);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                }
                if (line == 3) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.GRAY);
                    log5.setBackgroundColor(Color.WHITE);
                }
                if (line == 4) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.GRAY);
                }
                if (line > 4) {
                    log1.setBackgroundColor(Color.WHITE);
                    log2.setBackgroundColor(Color.WHITE);
                    log3.setBackgroundColor(Color.WHITE);
                    log4.setBackgroundColor(Color.WHITE);
                    log5.setBackgroundColor(Color.WHITE);
                }
                calibrateBtn.setEnabled(false);


            }

            final Handler handler = new Handler();
            Runnable runnable;

            @Override
            public void onFinish() {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        line = currentBuf + 1;

                        if (currentBuf == 4) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_5").setValue(strDate);
                            calibrateBtn.setEnabled(false);
                            calibrateBtn.setText("DONE");
                            startTimer();

                        }

                        if (currentBuf == 0) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_1").setValue(strDate);
                        }
                        if (currentBuf == 1) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(strDate);
                        }
                        if (currentBuf == 2) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(strDate);
                        }
                        if (currentBuf == 3) {
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            String strDate = simpleDateFormat.format(date);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(strDate);
                        }


                        calibrateBtn.setEnabled(true);

                        tvTimer.setVisibility(View.INVISIBLE);
                        String currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
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
                                    deviceRef.child("Data").child("CALIBRATION_STAT").setValue("ok");

                                    temp5.setText(tvTempCurr.getText());
                                }
                                currentBuf += 1;
                                calibData();
                                deleteAllCalibData();
                                databaseHelper.insertCalibration(PH1, MV1, SLOPE1, DT1, BFD1, pHAC1, t1);
                                databaseHelper.insertCalibration(PH2, MV2, SLOPE2, DT2, BFD2, pHAC2, t2);
                                databaseHelper.insertCalibration(PH3, MV3, SLOPE3, DT3, BFD3, pHAC3, t3);
                                databaseHelper.insertCalibration(PH4, MV4, SLOPE4, DT4, BFD4, pHAC4, t4);
                                databaseHelper.insertCalibration(PH5, MV5, SLOPE5, DT5, BFD5, pHAC5, t5);

                            });
                        });
                    }

                };

                runnable.run();
            }
        };

        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
            timer.start();
        });
    }

    public void deleteAllCalibData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM CalibData");
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


    }

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.phEdit1:
                EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_1").setValue(String.valueOf(ph));
                });
                dialog.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit2:

                EditPhBufferDialog dialog1 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").setValue(String.valueOf(ph));
                });
                dialog1.show(getParentFragmentManager(), null);
                break;
            case R.id.phEdit3:
                EditPhBufferDialog dialog2 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").setValue(String.valueOf(ph));
                });
                dialog2.show(getParentFragmentManager(), null);
                break;
            case R.id.phEdit4:
                EditPhBufferDialog dialog3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").setValue(String.valueOf(ph));
                });
                dialog3.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit5:
                EditPhBufferDialog dialog5 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_5").setValue(String.valueOf(ph));
                });
                dialog5.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit1_3:
                EditPhBufferDialog dialog_3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    System.out.println("1");
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").setValue(String.valueOf(ph));
                });
                dialog_3.show(getParentFragmentManager(), null);
                break;

            case R.id.phEdit2_3:

                EditPhBufferDialog dialog1_3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    System.out.println("2");
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").setValue(String.valueOf(ph));
                });
                dialog1_3.show(getParentFragmentManager(), null);
                break;
            case R.id.phEdit3_3:
                EditPhBufferDialog dialog2_3 = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    System.out.println("3");
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").setValue(String.valueOf(ph));
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

        super.onResume();
    }

    File[] reverseFileArray(File[] fileArray) {
        for (int i = 0; i < fileArray.length / 2; i++) {
            File a = fileArray[i];
            fileArray[i] = fileArray[fileArray.length - i - 1];
            fileArray[fileArray.length - i - 1] = a;
        }

        return fileArray.length > 0 ? fileArray : null;
    }

}