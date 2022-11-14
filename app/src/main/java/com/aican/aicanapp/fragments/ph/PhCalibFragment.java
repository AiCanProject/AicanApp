package com.aican.aicanapp.fragments.ph;

import static android.content.Context.MODE_PRIVATE;

import static java.lang.Integer.parseInt;

import com.itextpdf.kernel.pdf.PdfDocument;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
//import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Environment;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.ProbeScan.ProbeScanner;
import com.aican.aicanapp.adapters.CalibFileAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;

import com.aican.aicanapp.Source;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.dialogs.ExitConfirmDialog;
import com.aican.aicanapp.specificactivities.PhCalibrateActivity;
import com.aican.aicanapp.specificactivities.PhMvTable;
import com.aican.aicanapp.userdatabase.UserDatabase;
import com.aican.aicanapp.userdatabase.UserDatabaseModel;
import com.aican.aicanapp.utils.OnBackPressed;
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
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class PhCalibFragment extends Fragment implements OnBackPressed {

    private static final String FILE_NAME = "user_info.txt";
    private static final String FILE_NAMEE = "user_calibrate.txt";

    public static final String THREE_POINT_CALIBRATION = "three";
    public static final String FIVE_POINT_CALIBRATION = "five";
    public static final String CALIBRATION_TYPE = "calibration_type";
    public static String PH_MODE = "both";

    int pageHeight = 1120;
    int pagewidth = 792;
    Bitmap bmp, scaledbmp;

    Button fivePoint, threePoint;

    LinearLayout log3, log5, calibSpinner;

    int ec;
    String nullEntry, reportDate, reportTime;
    Integer fault;
    TextView phAfterCalib1, phAfterCalib2, phAfterCalib3, phAfterCalib4, phAfterCalib5;
    TextView temp1, temp2, temp3, temp4, temp5;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, tvTimer, lastCalib;
    TextView ph1, mv1, phEdit1, ph2, mv2, phEdit2, ph3, mv3, phEdit3, ph4, mv4, phEdit4, ph5, mv5, phEdit5, dt1, dt2, dt3, dt4, dt5;
    TextView qr1, qr2, qr3, qr4, qr5;
    TextView bufferD1, bufferD2, bufferD3, bufferD4, bufferD5, modeText;

    String companyName;

    DatabaseReference deviceRef;
    Button calibrateBtn, btnNext, printCalib, phMvTable;
    Spinner spin;
    String MV1, MV2, MV3, MV4, MV5, PH1, PH2, PH3, PH4, PH5, DT1, DT2, DT3, DT4, DT5, BFD1, BFD2, BFD3, BFD4, BFD5;
    String t1, t2, t3, t4, t5;
    String pHAC1, pHAC2, pHAC3, pHAC4, pHAC5;
    String mode;
    String test1, test2, test3;
    String mV1, mV2, mV3, mV4, mV5;
    String tm1, tm2, tm3, tm4, tm5;
    RecyclerView calibRecyclerView;
    String offset, battery, slope, temp;
    String calib_stat = "incomplete";

    PhView phView;
    TextView title;
    DatabaseHelper databaseHelper;
    CalibFileAdapter calibFileAdapter;
    ArrayList<BufferData> bufferList = new ArrayList<>();
    ArrayList<BufferData> bufferListThree = new ArrayList<>();

    String[] lines, liness;
    LinearLayout ll1;
    String deviceID = "";

    float[] buffers = new float[]{1.0F, 4.0F, 7.0F, 9.2F, 12.0F};
    float[] pHAfterCalibBuffer = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F};
    float[] pHAfterCalibBufferThree = new float[]{0.0F, 0.0F, 0.0F};
    float[] buffersThree = new float[]{4.0F, 7.0F, 9.2F};
    String[] bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
    String[] bufferLabelsThree = new String[]{"B_2", "B_3", "B_4"};
    String[] coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
    String[] postCoeffLabels = new String[]{"POST_VAL_1", "POST_VAL_2", "POST_VAL_3", "POST_VAL_4", "POST_VAL_5"};
    String[] postCoeffLabelsThree = new String[]{"POST_VAL_2", "POST_VAL_3", "POST_VAL_4"};
    String[] coeffLabelsThree = new String[]{"VAL_2", "VAL_3", "VAL_4"};
    int[] calValues = new int[]{10, 20, 30, 40, 50};
    int[] calValuesThree = new int[]{20, 30, 40};
    int currentBuf = 0;
    int currentBufThree = 0;

    DatabaseReference coeffRef = null;
    DatabaseReference coeffRefThree = null;
    float coeff = 0;
    float coeffThree = 0;
    boolean isCalibrating = false;

    LinearLayout l1, l2, l3, l4, l5;

    String coef;
    String currentTime;
    String strDate;

    private void loadBuffers() {
        //////////////////////////////////////////////////////
        deviceRef.child("UI").child("PH").child("PH_CAL").get().addOnSuccessListener(snapshot -> {
            for (int i = 0; i < bufferLabels.length; ++i) {
                buffers[i] = Float.parseFloat(snapshot.child(bufferLabels[i]).getValue(String.class));
                pHAfterCalibBuffer[i] = snapshot.child(postCoeffLabels[i]).getValue(Float.class);
            }
            for (int i = 0; i < bufferLabelsThree.length; i++) {
                buffersThree[i] = Float.parseFloat(snapshot.child(bufferLabelsThree[i]).getValue(String.class));
                pHAfterCalibBufferThree[i] = snapshot.child(postCoeffLabelsThree[i]).getValue(Float.class);
            }
            if (spin.getSelectedItemPosition() == 0) {

                ph1.setText(String.valueOf(buffers[0]));
                ph2.setText(String.valueOf(buffers[1]));
                ph3.setText(String.valueOf(buffers[2]));
                ph4.setText(String.valueOf(buffers[3]));
                ph5.setText(String.valueOf(buffers[4]));

                phAfterCalib1.setText(String.valueOf(pHAfterCalibBuffer[0]));
                phAfterCalib2.setText(String.valueOf(pHAfterCalibBuffer[1]));
                phAfterCalib3.setText(String.valueOf(pHAfterCalibBuffer[2]));
                phAfterCalib4.setText(String.valueOf(pHAfterCalibBuffer[3]));
                phAfterCalib5.setText(String.valueOf(pHAfterCalibBuffer[4]));

                pHAC1 = String.valueOf(pHAfterCalibBuffer[0]);
                pHAC2 = String.valueOf(pHAfterCalibBuffer[1]);
                pHAC3 = String.valueOf(pHAfterCalibBuffer[2]);
                pHAC4 = String.valueOf(pHAfterCalibBuffer[3]);
                pHAC5 = String.valueOf(pHAfterCalibBuffer[4]);


            } else if (spin.getSelectedItemPosition() == 1) {
                ph1.setText(String.valueOf(buffersThree[0]));
                ph2.setText(String.valueOf(buffersThree[1]));
                ph3.setText(String.valueOf(buffersThree[2]));

                phAfterCalib1.setText(String.valueOf(pHAfterCalibBufferThree[0]));
                phAfterCalib2.setText(String.valueOf(pHAfterCalibBufferThree[1]));
                phAfterCalib3.setText(String.valueOf(pHAfterCalibBufferThree[2]));

                pHAC1 = String.valueOf(pHAfterCalibBufferThree[0]);
                pHAC2 = String.valueOf(pHAfterCalibBufferThree[1]);
                pHAC3 = String.valueOf(pHAfterCalibBufferThree[2]);

            }
        });
    }


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
        coeffRef = deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]);
        coeffRef.addValueEventListener(coeffListener);
    }

    private void setupCoeffListenerThree() {
        if (coeffRefThree != null) {
            coeffRefThree.removeEventListener(coeffListener);
        }
        coeffRefThree = deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabelsThree[currentBufThree]);
        coeffRefThree.addValueEventListener(coeffListener);
    }


    private void setupListeners() {

        if (spin.getSelectedItemPosition() == 0) {

            l4.setVisibility(View.VISIBLE);
            l5.setVisibility(View.VISIBLE);

            SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

            t1 = shp.getString("tem1", "--");
            t2 = shp.getString("tem2", "--");
            t3 = shp.getString("tem3", "--");
            t4 = shp.getString("tem4", "--");
            t5 = shp.getString("tem5", "--");

            temp1.setText(t1);
            temp2.setText(t2);
            temp3.setText(t3);
            temp4.setText(t4);
            temp5.setText(t5);


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
                    tm1 = dt1.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT1", tm1);
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
                    tm2 = dt2.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT2", tm2);
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
                    tm3 = dt3.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT3", tm3);
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
                    tm4 = dt4.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT4", tm4);
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
                    tm5 = dt5.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT5", tm5);
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

        } else if (spin.getSelectedItemPosition() == 1) {

            mv4.setText(" ");
            mv5.setText(" ");
            dt4.setText(" ");
            dt5.setText(" ");
            SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

            t1 = shp.getString("tem1", "--");
            t2 = shp.getString("tem2", "--");
            t3 = shp.getString("tem3", "--");
            t4 = shp.getString("tem4", "--");
            t5 = shp.getString("tem5", "--");


            temp1.setText(t1);
            temp2.setText(t2);
            temp3.setText(t3);
            temp4.setText(t4);
            temp5.setText(t5);

            l4.setVisibility(View.INVISIBLE);
            l5.setVisibility(View.INVISIBLE);
            deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_2").addValueEventListener(new ValueEventListener() {
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

            deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_3").addValueEventListener(new ValueEventListener() {
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
            deviceRef.child("UI").child("PH").child("PH_CAL").child("POST_VAL_4").addValueEventListener(new ValueEventListener() {
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

            deviceRef.child("UI").child("PH").child("PH_CAL").child("MV_2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float ec = snapshot.getValue(Float.class);
                    String ecForm = String.format(Locale.UK, "%.2f", ec);
                    mv1.setText(ecForm);
                    mV1 = mv1.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("MV2", mV1);
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
                    mv2.setText(ecForm);
                    mV2 = mv2.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("MV3", mV2);
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
                    mv3.setText(ecForm);
                    mV3 = mv3.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("MV4", mV3);
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
                    dt1.setText(time);
                    tm1 = dt1.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT2", tm1);
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
                    dt2.setText(time);
                    tm2 = dt2.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT3", tm2);
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
                    dt3.setText(time);
                    tm3 = dt3.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("DT4", tm3);
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
                    ph1.setText(phVal);
                    PH2 = ph1.getText().toString();

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
                    ph2.setText(phVal);
                    PH3 = ph2.getText().toString();

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
                    ph3.setText(phVal);
                    PH4 = ph3.getText().toString();

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("PH4", PH4);
                    myEdit.commit();
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


        if (spin.getSelectedItemPosition() == 0) {
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    ec = snapshot.getValue(Integer.class);
                    Log.d("ECVal", "onDataChange: " + ec);
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
                    } else if (ec == 51) {
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
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_1").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 21) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 31) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 41) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 51) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(new Date());
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_5").setValue(strDate);
                        calibrateBtn.setText("DONE");
                        calibrateBtn.setEnabled(false);
//                        currentBuf = 0;
//                        currentBufThree = -1;


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                calibrateBtn.setText("START");
                                calibrateBtn.setEnabled(true);
                                deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                            }
                        }, 90000);   //5 seconds

                    } else if (ec == 0) {
                        calibrateBtn.setText("START");
                        calibrateBtn.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        } else if (spin.getSelectedItemPosition() == 1) {
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    ec = snapshot.getValue(Integer.class);

                    if (ec == 20 || ec == 0) {
                        l1.setBackgroundColor(Color.LTGRAY);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.WHITE);
                    } else if (ec == 30 || ec == 21) {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.LTGRAY);
                        l3.setBackgroundColor(Color.WHITE);

                    } else if (ec == 40 || ec == 31) {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.LTGRAY);

                    } else {
                        l1.setBackgroundColor(Color.WHITE);
                        l2.setBackgroundColor(Color.WHITE);
                        l3.setBackgroundColor(Color.WHITE);
                        l4.setBackgroundColor(Color.WHITE);
                        l5.setBackgroundColor(Color.WHITE);
                    }

                    if (ec == 21) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 31) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 41) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(new Date());
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_4").setValue(strDate);
                        calibrateBtn.setText("DONE");
                        calibrateBtn.setEnabled(false);
//                        currentBuf = 0;
//                        currentBufThree = -1;


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                calibrateBtn.setText("START");
                                calibrateBtn.setEnabled(true);
                                deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
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


    /**
     * Inflate the layout for this fragment
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_calib, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnNext = view.findViewById(R.id.nextBtn);
        phView = view.findViewById(R.id.phView);
        ph1 = view.findViewById(R.id.ph1);
        mv1 = view.findViewById(R.id.mv1);
        ph2 = view.findViewById(R.id.ph2);
        mv2 = view.findViewById(R.id.mv2);
        ph3 = view.findViewById(R.id.ph3);
        mv3 = view.findViewById(R.id.mv3);

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

        bufferD1.setSelected(true);
        bufferD2.setSelected(true);
        bufferD3.setSelected(true);
        bufferD4.setSelected(true);
        bufferD5.setSelected(true);


        ph4 = view.findViewById(R.id.ph4);
        mv4 = view.findViewById(R.id.mv4);
        printCalib = view.findViewById(R.id.printCalibData);
        phMvTable = view.findViewById(R.id.phMvTable);
        ph5 = view.findViewById(R.id.ph5);
        mv5 = view.findViewById(R.id.mv5);

        fivePoint = view.findViewById(R.id.fivePoint);
        threePoint = view.findViewById(R.id.threePoint);

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

        title = view.findViewById(R.id.tvTitle);
        l1 = view.findViewById(R.id.log1);
        l2 = view.findViewById(R.id.log2);
        l3 = view.findViewById(R.id.log3);
        l4 = view.findViewById(R.id.log4);
        l5 = view.findViewById(R.id.log5);

        temp1 = view.findViewById(R.id.temp1);
        temp2 = view.findViewById(R.id.temp2);
        temp3 = view.findViewById(R.id.temp3);
        temp4 = view.findViewById(R.id.temp4);
        temp5 = view.findViewById(R.id.temp5);

        phAfterCalib1 = view.findViewById(R.id.phAfterCalib1);
        phAfterCalib2 = view.findViewById(R.id.phAfterCalib2);
        phAfterCalib3 = view.findViewById(R.id.phAfterCalib3);
        phAfterCalib4 = view.findViewById(R.id.phAfterCalib4);
        phAfterCalib5 = view.findViewById(R.id.phAfterCalib5);

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
        modeText = view.findViewById(R.id.modeText);
        calibSpinner = view.findViewById(R.id.calibSpinner);
        calibRecyclerView = view.findViewById(R.id.rvCalibFileView);
        nullEntry = "";


        getFirebaseValue();

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
//        deviceRef.child("PH_MODE").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    PH_MODE = snapshot.getValue(String.class);
//                } else {
//                    deviceRef.child("PH_MODE").setValue("both");
//                    PH_MODE = "both";
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
//            }
//        });

        deviceRef.child("Data").child("CALIBRATION_STAT").setValue("incomplete");


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
//        if (liness != null) {
//            lastCalib.setText("Last Calibrated By \n" + liness[0]);
//        }

        String[] spinselect = {"5", "3"};


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinselect);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
//
//        if (PH_MODE.equals("5")) {
//
//        }

//        SharedPreferences spinPref = getContext().getSharedPreferences("spinnerPref", MODE_PRIVATE);
//        int spinnerValue = spinPref.getInt("userChoiceSpinner", -1);
//        if (spinnerValue != -1) {
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
//        }

        fivePoint.setOnClickListener(v -> {
            spin.setSelection(0);
        });

        threePoint.setOnClickListener(v -> {
            spin.setSelection(1);
        });

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:

                        log3.setVisibility(View.VISIBLE);
                        log5.setVisibility(View.VISIBLE);
                        mode = "5";
                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

//                        PhCalibFragment fragment = new PhCalibFragment();
//
//                        Log.d("navigation", "loadFragments: Frag is loaded");
//                        getActivity().getSupportFragmentManager()
//                                .beginTransaction()
//                                .replace(R.id.fragmentContainerView, fragment)
//                                .addToBackStack(null)
//                                .commit();


                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);


//                        Fragment fragment = new PhCalibFragment();
//                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
//                        fragmentTransaction.addToBackStack(null);
//                        fragmentTransaction.commit();


                        buffers = new float[]{2.0F, 4.0F, 7.0F, 9.0F, 11.0F};
                        bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
                        coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
                        postCoeffLabels = new String[]{"POST_VAL_1", "POST_VAL_2", "POST_VAL_3", "POST_VAL_4", "POST_VAL_5"};
                        calValues = new int[]{10, 20, 30, 40, 50};
                        calValuesThree = new int[]{20, 30, 40};

                        currentBuf = 0;
                        currentBufThree = 0;
                        setupCoeffListener();
                        setupListeners();
                        loadBuffers();

                        deleteAllCalibData();
                        calibData();

                        bufferD1.setText(BFD1);
                        bufferD2.setText(BFD2);
                        bufferD3.setText(BFD3);
                        bufferD4.setText(BFD4);
                        bufferD5.setText(BFD5);

                        databaseHelper.insertCalibration(PH1, MV1, DT1, BFD1, pHAC1, t1);
                        databaseHelper.insertCalibration(PH2, MV2, DT2, BFD2, pHAC2, t2);
                        databaseHelper.insertCalibration(PH3, MV3, DT3, BFD3, pHAC3, t3);
                        databaseHelper.insertCalibration(PH4, MV4, DT4, BFD4, pHAC4, t4);
                        databaseHelper.insertCalibration(PH5, MV5, DT5, BFD5, pHAC5, t5);

                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL_MODE").setValue(2);

//                        int userChoice = spin.getSelectedItemPosition();
//
//                        SharedPreferences sharedPrefs = getContext().getSharedPreferences("spinnerPref", 0);
//                        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
//                        prefEditor.putInt("userChoiceSpinner", userChoice);
//                        prefEditor.commit();

                        break;

                    case 1:
                        mode = "3";

                        log3.setVisibility(View.VISIBLE);
                        log5.setVisibility(View.GONE);


//                        Fragment fragment1 = new PhCalibFragment();
//                        FragmentManager fragmentManager1 = getActivity().getSupportFragmentManager();
//                        FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
//                        fragmentTransaction1.replace(R.id.fragmentContainerView, fragment1);
//                        fragmentTransaction1.addToBackStack(null);
//                        fragmentTransaction1.commit();

                        buffers = new float[]{4.0F, 7.0F, 9.0F};
                        bufferLabels = new String[]{"B_2", "B_3", "B_4"};
                        coeffLabels = new String[]{"VAL_2", "VAL_3", "VAL_4"};
                        postCoeffLabels = new String[]{"POST_VAL_2", "POST_VAL_3", "POST_VAL_4"};
                        calValuesThree = new int[]{20, 30, 40};
                        calValues = new int[]{10, 20, 30, 40, 50};

                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);


//                        PhCalibFragment fragment1 = new PhCalibFragment();
//
//                        Log.d("navigation", "loadFragments: Frag is loaded");
//                        getActivity().getSupportFragmentManager()
//                                .beginTransaction()
//                                .replace(R.id.fragmentContainerView, fragment1)
//                                .addToBackStack(null)
//                                .commit();

                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);


                        currentBuf = 0;
                        currentBufThree = 0;
                        setupCoeffListenerThree();
                        setupListeners();
                        loadBuffers();

                        deleteAllCalibData();
                        calibDataThree();

                        bufferD2.setText(BFD2);
                        bufferD3.setText(BFD3);
                        bufferD4.setText(BFD4);


                        databaseHelper.insertCalibration(PH2, MV2, DT2, BFD2, pHAC2, t2);
                        databaseHelper.insertCalibration(PH3, MV3, DT3, BFD3, pHAC3, t3);
                        databaseHelper.insertCalibration(PH4, MV4, DT4, BFD4, pHAC4, t4);

                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL_MODE").setValue(1);

//                        int userChoice1 = spin.getSelectedItemPosition();
//
//
//                        SharedPreferences sharedPrefs1 = getContext().getSharedPreferences("spinnerPref", 0);
//                        SharedPreferences.Editor prefEditor1 = sharedPrefs1.edit();
//                        prefEditor1.putInt("userChoiceSpinner", userChoice1);
//                        prefEditor1.commit();

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
            phEdit2.setOnClickListener(this::onClick);
            phEdit3.setOnClickListener(this::onClick);
            phEdit4.setOnClickListener(this::onClick);
            phEdit5.setOnClickListener(this::onClick);
        } else if (spin.getSelectedItemPosition() == 1) {
            phEdit1.setOnClickListener(this::onClick);
            phEdit2.setOnClickListener(this::onClick);
            phEdit3.setOnClickListener(this::onClick);
        }

        if (spin.getSelectedItemPosition() == 0) {
            qr1.setOnClickListener(this::onClick);
            qr2.setOnClickListener(this::onClick);
            qr3.setOnClickListener(this::onClick);
            qr4.setOnClickListener(this::onClick);
            qr5.setOnClickListener(this::onClick);
        } else if (spin.getSelectedItemPosition() == 1) {
            qr1.setOnClickListener(this::onClick);
            qr2.setOnClickListener(this::onClick);
            qr3.setOnClickListener(this::onClick);
        }

        if (Source.subscription.equals("cfr")) {

            DialogMain dialogMain = new DialogMain();
            dialogMain.setCancelable(false);
            Source.userTrack = "PhCalibPage logged : ";
            dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
        }
        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPhCurr.setText("--");
                tvTempCurr.setText("--");
                FileOutputStream fos = null;

                try {
                    fos = getContext().openFileOutput(FILE_NAMEE, MODE_PRIVATE);
                    if (Source.userName != null)
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
                calibrate();
            }
        });


        test3 = mv2.getText().toString();

        phMvTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PhMvTable.class);
                startActivity(intent);
            }
        });

        printCalib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


//                try {
//                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CalibrationData.xlsx");
//
//                    PdfSaveOptions options = new PdfSaveOptions();
//                    options.setCompliance(PdfCompliance.PDF_A_1_B);
////                    File Pdfdir = new File(Environment.getExternalStorageDirectory()+"/LabApp/Currentlog/CalibPdf");
//////                    if (!Pdfdir.exists()) {
//////                        if (!Pdfdir.mkdirs()) {
//////                            Log.d("App", "failed to create directory");
//////                        }
//////                    }
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
//                    String currentDateandTime = sdf.format(new Date());
//                    String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
//                    File tempRoot = new File(tempPath);
//                    fileNotWrite(tempRoot);
//                    File[] tempFilesAndFolders = tempRoot.listFiles();
//                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CD_" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);
//
//                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
//                    File root1 = new File(path1);
//                    fileNotWrite(root1);
//                    File[] filesAndFolders1 = root1.listFiles();
//
//                    if (filesAndFolders1 == null || filesAndFolders1.length == 0) {
//
//                        return;
//                    } else {
//                        for (int i = 0; i < filesAndFolders1.length; i++) {
//                            if (filesAndFolders1[i].getName().endsWith(".csv") || filesAndFolders1[i].getName().endsWith(".xlsx")) {
//                                filesAndFolders1[i].delete();
//                            }
//                        }
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(root);
                File[] filesAndFoldersPDF = rootPDF.listFiles();


                calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), reverseFileArray(filesAndFoldersPDF));
                calibRecyclerView.setAdapter(calibFileAdapter);
                calibFileAdapter.notifyDataSetChanged();
                calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();
//
//        if (filesAndFolders == null || filesAndFolders.length == 0) {
//            Toast.makeText(requireContext(), "No Files Found", Toast.LENGTH_SHORT).show();
//            return;
//        } else {
//            for (int i = 0; i < filesAndFolders.length; i++) {
//                if (filesAndFolders[i].getName().endsWith(".pdf")) {
//                    filesAndFoldersPDF[0] = filesAndFolders[i];
//                }
//            }
//        }

        calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), filesAndFolders);
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

    public void fileNotWrite(File file) {
        file.setWritable(false);
        if (file.canWrite()) {
            Log.d("csv", "Nhi kaam kar rha");
        } else {
            Log.d("csvnw", "Party Bhaiiiii");
        }
    }

    public void calibData() {
        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        MV1 = shp.getString("MV1", "");
        MV2 = shp.getString("MV2", "");
        MV3 = shp.getString("MV3", "");
        MV4 = shp.getString("MV4", "");
        MV5 = shp.getString("MV5", "");

        DT1 = shp.getString("DT1", "");
        DT2 = shp.getString("DT2", "");
        DT3 = shp.getString("DT3", "");
        DT4 = shp.getString("DT4", "");
        DT5 = shp.getString("DT5", "");

        PH1 = shp.getString("PH1", "");
        PH2 = shp.getString("PH2", "");
        PH3 = shp.getString("PH3", "");
        PH4 = shp.getString("PH4", "");
        PH5 = shp.getString("PH5", "");

        BFD1 = shp.getString("BFD1", "");
        BFD2 = shp.getString("BFD2", "");
        BFD3 = shp.getString("BFD3", "");
        BFD4 = shp.getString("BFD4", "");
        BFD5 = shp.getString("BFD5", "");

    }

    public void calibDataThree() {

        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        MV2 = shp.getString("MV2", "");
        MV3 = shp.getString("MV3", "");
        MV4 = shp.getString("MV4", "");

        DT2 = shp.getString("DT2", "");
        DT3 = shp.getString("DT3", "");
        DT4 = shp.getString("DT4", "");

        PH2 = shp.getString("PH2", "");
        PH3 = shp.getString("PH3", "");
        PH4 = shp.getString("PH4", "");

        BFD2 = shp.getString("BFD2", "");
        BFD3 = shp.getString("BFD3", "");
        BFD4 = shp.getString("BFD4", "");
    }


    public void deleteAllCalibData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM CalibData");
        db.close();
    }

    public void exportCalibData() {


        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {

            reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            file = new File(exportDir, "CalibrationData.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);
            SharedPreferences shp = getContext().getSharedPreferences("Extras", MODE_PRIVATE);
            offset = "Offset: " + shp.getString("offset", "");
            battery = "Battery: " + shp.getString("battery", "");
            slope = "Slope: " + shp.getString("slope", "");
            temp = "Temperature: " + shp.getString("temp", "");


            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);
//            printWriter.println(companyName + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Company: " + companyName);
            printWriter.println("Username: " + Source.logUserName);
            printWriter.println("DeviceID: " + deviceID);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportTime + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(offset + "," + battery);
            printWriter.println(temp);
            printWriter.println(slope);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Calibration Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("________pH____,pH After Calib,_____mV__,__DATE___TIME__,Temperature");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
                String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
                String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));
//                String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));

                String record1 = ph + "," + pHAC + "," + mv + "," + date + "," + temperature1;

                printWriter.println(record1);
            }

            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            if (spin.getSelectedItemPosition() == 0) {
                printWriter.println("Calibration: " + calib_stat);
                printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
                printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
                printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            }
            printWriter.println("Operator Sign");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + "Supervisor Sign");
            calibCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);

            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
            Workbook workbook = new Workbook(inputFile + "CalibrationData.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 12.5);
            worksheet.getCells().setColumnWidth(1, 12.5);
            worksheet.getCells().setColumnWidth(2, 12.5);
            worksheet.getCells().setColumnWidth(3, 18.5);
//            worksheet.getCells().setColumnWidth(3, 12.5);
            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CalibrationData.xlsx", SaveFormat.XLSX);

        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
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


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CD_" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf");
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

        float columnWidth[] = {200f, 210f, 170f, 340f, 170f};
        Table table = new Table(columnWidth);
        table.addCell("pH");
        table.addCell("pH After Calib");
        table.addCell("mV");
        table.addCell("Date & Time");
        table.addCell("Temperature");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);


        while (calibCSV.moveToNext()) {
            String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
            String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
            String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
            String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
            String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));

            table.addCell(ph);
            table.addCell(pHAC + "");
            table.addCell(mv);
            table.addCell(date);
            table.addCell(temperature1);

        }
        document.add(table);

        if (spin.getSelectedItemPosition() == 0) {
            document.add(new Paragraph("Calibration : " + calib_stat));
        }

        document.add(new Paragraph("Operator Sign                                                                                      Supervisor Sign"));


        document.close();

        Toast.makeText(getContext(), "Pdf generated", Toast.LENGTH_SHORT).show();

    }

//    private void generatePDF1() {
//
//        String company_name = "Company: " + companyName;
//        String user_name = "Username: " + Source.logUserName;
//        String device_id = "DeviceID: " + deviceID;
//
//        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//
//        SharedPreferences shp = getContext().getSharedPreferences("Extras", MODE_PRIVATE);
//        offset = "Offset: " + shp.getString("offset", "");
//        battery = "Battery: " + shp.getString("battery", "");
//        slope = "Slope: " + shp.getString("slope", "");
//        temp = "Temperature: " + shp.getString("temp", "");
//
//        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.wifi_router);
//        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);
//
//        PdfDocument pdfDocument = new PdfDocument();
//        Paint paint = new Paint();
//
//        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
//        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);
//        Canvas canvas = myPage.getCanvas();
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(12.0f);
//        canvas.drawText(company_name, 30, 70, paint);
//        canvas.drawText(user_name, 30, 85, paint);
//        canvas.drawText(device_id, 30, 100, paint);
//        canvas.drawText(reportDate, 30, 115, paint);
//
//
//        pdfDocument.finishPage(myPage);
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/", "AICAN" + Math.random() + ".pdf");
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(file));
//
//            Toast.makeText(getContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//
//        pdfDocument.close();
//    }


//    private void generatePDF2() {
//
//        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//
//        SharedPreferences shp = getContext().getSharedPreferences("Extras", MODE_PRIVATE);
//        offset = "Offset: " + shp.getString("offset", "");
//        battery = "Battery: " + shp.getString("battery", "");
//        slope = "Slope: " + shp.getString("slope", "");
//        temp = "Temperature: " + shp.getString("temp", "");
//
//        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.wifi_router);
//        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);
//
//        PdfDocument pdfDocument = new PdfDocument();
//        Paint paint = new Paint();
//        Paint title = new Paint();
//        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
//
//        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
//        Canvas canvas = myPage.getCanvas();
//
//
//        canvas.drawBitmap(scaledbmp, 56, 40, paint);
//
//        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//        title.setTextSize(15);
//
//        title.setColor(ContextCompat.getColor(getContext(), R.color.purple_200));
//
//        canvas.drawText("A portal for IT professionals.", 209, 100, title);
//        canvas.drawText("Geeks for Geeks", 209, 80, title);
//
//        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//        title.setColor(ContextCompat.getColor(getContext(), R.color.purple_200));
//        title.setTextSize(15);
//
//        title.setTextAlign(Paint.Align.CENTER);
//        canvas.drawText("This is sample document which we have created.", 396, 560, title);
//
//
//        pdfDocument.finishPage(myPage);
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/", "AICAN" + Math.random() + ".pdf");
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(file));
//
//            Toast.makeText(getContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//
//        pdfDocument.close();
//    }

    private void onClick(View v) {

        if (spin.getSelectedItemPosition() == 0) {
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
                default:
                    break;
            }
        } else if (spin.getSelectedItemPosition() == 1) {
            switch (v.getId()) {
                case R.id.phEdit1:
                    EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
                        updateBufferValue(ph);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").setValue(String.valueOf(ph));
                    });
                    dialog.show(getParentFragmentManager(), null);
                    break;

                case R.id.phEdit2:

                    EditPhBufferDialog dialog1 = new EditPhBufferDialog(ph -> {
                        updateBufferValue(ph);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").setValue(String.valueOf(ph));
                    });
                    dialog1.show(getParentFragmentManager(), null);
                    break;
                case R.id.phEdit3:
                    EditPhBufferDialog dialog2 = new EditPhBufferDialog(ph -> {
                        updateBufferValue(ph);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").setValue(String.valueOf(ph));
                    });
                    dialog2.show(getParentFragmentManager(), null);
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
                default:
                    break;
            }
        }

        setupListeners();
    }

    private void openQRActivity(String view) {
        Intent intent = new Intent(getContext(), ProbeScanner.class);
        intent.putExtra("activity", "PhCalibFragment");
        intent.putExtra("view", view);
        startActivity(intent);
    }


    private void updateBufferValue(Float value) {
        String newValue = String.valueOf(value);
    }

    public void calibrate() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        databaseHelper.insert_action_data(time, date, "Calibrated : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);
        calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtn.setEnabled(false);
        tvTimer.setVisibility(View.VISIBLE);
        isCalibrating = true;
        if (spin.getSelectedItemPosition() == 0) {
            setupCoeffListener();
        } else {
            setupCoeffListenerThree();
        }
        CountDownTimer timer = new CountDownTimer(45000, 1000) { //45000
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
                        currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
                        bufferList.add(new BufferData(null, null, currentTime));
                        bufferListThree.add(new BufferData(null, null, currentTime));

                        if (spin.getSelectedItemPosition() == 0) {

                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                            Log.e("cValue", currentBuf + "");
                            int b = currentBuf;

                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
//                                int b = currentBuf < 0 ? 4 : currentBuf;
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
                                    databaseHelper.insertCalibration(PH1, MV1, DT1, BFD1, pHAC1, t1);
                                    databaseHelper.insertCalibration(PH2, MV2, DT2, BFD2, pHAC2, t2);
                                    databaseHelper.insertCalibration(PH3, MV3, DT3, BFD3, pHAC3, t3);
                                    databaseHelper.insertCalibration(PH4, MV4, DT4, BFD4, pHAC4, t4);
                                    databaseHelper.insertCalibration(PH5, MV5, DT5, BFD5, pHAC5, t5);

                                });
                            });

                        } else if (spin.getSelectedItemPosition() == 1) {
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree] + 1);
                            int a = currentBufThree;
                            Log.e("currentBufThree0", currentBufThree + "");// 2

                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabelsThree[currentBufThree]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
                                Log.e("currentBufThree1", currentBufThree + "");// -1
//                                int a = currentBufThree < 0 ? 2 : currentBufThree;
                                if (coeff == null) return;

                                deviceRef.child("UI").child("PH").child("PH_CAL").child(postCoeffLabelsThree[a]).get().addOnSuccessListener(dataSnapshot2 -> {
                                    Float postCoeff = dataSnapshot2.getValue(Float.class);
                                    Log.e("currentBufThree2", currentBufThree + "");// crash
                                    if (postCoeff == null) return;

                                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    if (a == 0) {
                                        phAfterCalib1.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem1", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC1", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp1.setText(tvTempCurr.getText());
                                    } else if (a == 1) {
                                        phAfterCalib2.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem2", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC2", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp2.setText(tvTempCurr.getText());
                                    } else if (a == 2) {
                                        phAfterCalib3.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem3", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC3", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp3.setText(tvTempCurr.getText());
                                    } else if (a == 3) {
                                        phAfterCalib4.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem4", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC4", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp4.setText(tvTempCurr.getText());
                                    } else if (a == 4) {
                                        phAfterCalib5.setText(String.valueOf(postCoeff));
                                        myEdit.putString("tem5", tvTempCurr.getText().toString());
                                        myEdit.putString("pHAC5", String.valueOf(postCoeff));
                                        myEdit.commit();

                                        temp5.setText(tvTempCurr.getText());
                                    }
                                    currentBufThree += 1;
                                    currentBufThree = currentBufThree % 3;
                                    calibDataThree();
                                    deleteAllCalibData();
                                    databaseHelper.insertCalibration(PH2, MV2, DT2, BFD2, pHAC2, t2);
                                    databaseHelper.insertCalibration(PH3, MV3, DT3, BFD3, pHAC3, t3);
                                    databaseHelper.insertCalibration(PH4, MV4, DT4, BFD4, pHAC4, t4);

                                });

                            });
                        }
                        deviceRef.child("Data").child("PH_VAL").get().addOnSuccessListener(dataSnapshot -> {
                            Float ph = dataSnapshot.getValue(Float.class);
                            String phForm = String.format(Locale.UK, "%.2f", ph);
                            tvPhCurr.setText(phForm);
                        });

                        deviceRef.child("Data").child("TEMP_VAL").get().addOnSuccessListener(dataSnapshot -> {
                            Float temp = dataSnapshot.getValue(Float.class);
                            String tempForm = String.format(Locale.UK, "%.1f", temp);
                            tvTempCurr.setText(tempForm + "°C");

                            if (temp <= -127.0) {
                                tvTempCurr.setText("NA");
                            }
                        });
                    }


                };

                runnable.run();
            }
        };

        if (spin.getSelectedItemPosition() == 0) {
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
                timer.start();
            });
        } else if (spin.getSelectedItemPosition() == 1) {
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree % 3]).addOnSuccessListener(t -> {
                timer.start();
            });
        }


    }


    ValueEventListener coeffListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
            Float coeff = snapshot.getValue(Float.class);
            if (coeff == null) return;
            PhCalibFragment.this.coeff = coeff;
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {
        }
    };


    @Override
    public void onBackPressed() {
        if (isCalibrating) {
            ExitConfirmDialog dialogs = new ExitConfirmDialog((new ExitConfirmDialog.DialogCallbacks() {
                @Override
                public void onYesClicked(Dialog dialogs) {
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                    dialogs.dismiss();
                    isCalibrating = false;
                }

                @Override
                public void onNoClicked(Dialog dialogs) {
                    dialogs.dismiss();
                }
            }));

            dialogs.show(getParentFragmentManager(), null);
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onResume() {

        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        BFD1 = shp.getString("BFD1", "");
        BFD2 = shp.getString("BFD2", "");
        BFD3 = shp.getString("BFD3", "");
        BFD4 = shp.getString("BFD4", "");
        BFD5 = shp.getString("BFD5", "");

        bufferD1.setText(BFD1);
        bufferD2.setText(BFD2);
        bufferD3.setText(BFD3);
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
