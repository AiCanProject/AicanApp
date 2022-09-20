package com.aican.aicanapp.fragments.ph;

import static android.content.Context.MODE_PRIVATE;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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

    LinearLayout log3, log5, calibSpinner;

    int ec;
    String nullEntry, reportDate, reportTime;
    Integer fault;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, tvTimer, lastCalib;
    TextView ph1, mv1, phEdit1, ph2, mv2, phEdit2, ph3, mv3, phEdit3, ph4, mv4, phEdit4, ph5, mv5, phEdit5, dt1, dt2, dt3, dt4, dt5;
    TextView qr1, qr2, qr3, qr4, qr5;
    TextView bufferD1, bufferD2, bufferD3, bufferD4, bufferD5, modeText;

    DatabaseReference deviceRef;
    Button calibrateBtn, btnNext, printCalib, phMvTable;
    Spinner spin;
    String MV1, MV2, MV3, MV4, MV5, PH1, PH2, PH3, PH4, PH5, DT1, DT2, DT3, DT4, DT5, BFD1, BFD2, BFD3, BFD4, BFD5;
    String mode;
    String test1, test2, test3;
    String mV1, mV2, mV3, mV4, mV5;
    String tm1, tm2, tm3, tm4, tm5;
    RecyclerView calibRecyclerView;
    String offset, battery, slope, temp;

    PhView phView;
    TextView title;
    DatabaseHelper databaseHelper;
    CalibFileAdapter calibFileAdapter;
    ArrayList<BufferData> bufferList = new ArrayList<>();
    ArrayList<BufferData> bufferListThree = new ArrayList<>();

    String[] lines, liness;
    LinearLayout ll1;

    float[] buffers = new float[]{1.0F, 4.0F, 7.0F, 9.2F, 12.0F};
    float[] buffersThree = new float[]{4.0F, 7.0F, 9.2F};
    String[] bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
    String[] bufferLabelsThree = new String[]{"B_2", "B_3", "B_4"};
    String[] coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
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
        deviceRef.child("UI").child("PH").child("PH_CAL").get().addOnSuccessListener(snapshot -> {
            for (int i = 0; i < bufferLabels.length; ++i) {
                buffers[i] = Float.parseFloat(snapshot.child(bufferLabels[i]).getValue(String.class));
            }
            for (int i = 0; i < bufferLabelsThree.length; i++) {
                buffersThree[i] = Float.parseFloat(snapshot.child(bufferLabelsThree[i]).getValue(String.class));

            }
            if (spin.getSelectedItemPosition() == 0) {

                ph1.setText(String.valueOf(buffers[0]));
                ph2.setText(String.valueOf(buffers[1]));
                ph3.setText(String.valueOf(buffers[2]));
                ph4.setText(String.valueOf(buffers[3]));
                ph5.setText(String.valueOf(buffers[4]));

            } else if (spin.getSelectedItemPosition() == 1) {
                ph1.setText(String.valueOf(buffersThree[0]));
                ph2.setText(String.valueOf(buffersThree[1]));
                ph3.setText(String.valueOf(buffersThree[2]));
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

            l4.setVisibility(View.INVISIBLE);
            l5.setVisibility(View.INVISIBLE);

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
                        currentBuf = -1;


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
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_1").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 31) {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(date);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_2").setValue(strDate);
                        calibrateBtn.setEnabled(true);


                    } else if (ec == 41) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        strDate = simpleDateFormat.format(new Date());
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("DT_3").setValue(strDate);
                        calibrateBtn.setText("DONE");
                        calibrateBtn.setEnabled(false);
                        currentBuf = -1;


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


        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:

                        log3.setVisibility(View.VISIBLE);
                        log5.setVisibility(View.VISIBLE);
                        mode = "5";

                        buffers = new float[]{2.0F, 4.0F, 7.0F, 9.0F, 11.0F};
                        bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
                        coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
                        calValues = new int[]{10, 20, 30, 40, 50};

                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
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

                        databaseHelper.insertCalibration(PH1, MV1, DT1, BFD1);
                        databaseHelper.insertCalibration(PH2, MV2, DT2, BFD2);
                        databaseHelper.insertCalibration(PH3, MV3, DT3, BFD3);
                        databaseHelper.insertCalibration(PH4, MV4, DT4, BFD4);
                        databaseHelper.insertCalibration(PH5, MV5, DT5, BFD5);

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

                        buffers = new float[]{4.0F, 7.0F, 9.0F};
                        bufferLabels = new String[]{"B_2", "B_3", "B_4"};
                        coeffLabels = new String[]{"VAL_2", "VAL_3", "VAL_4"};
                        calValues = new int[]{20, 30, 40};

                        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
                        setupCoeffListenerThree();
                        setupListeners();
                        loadBuffers();

                        deleteAllCalibData();
                        calibDataThree();

                        bufferD2.setText(BFD2);
                        bufferD3.setText(BFD3);
                        bufferD4.setText(BFD4);


                        databaseHelper.insertCalibration(PH2, MV2, DT2, BFD2);
                        databaseHelper.insertCalibration(PH3, MV3, DT3, BFD3);
                        databaseHelper.insertCalibration(PH4, MV4, DT4, BFD4);

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
            Source.userTrack = "PhCalibFragment logged in by ";
            dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
        }
        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPhCurr.setText("--");
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
                if (mode.equals(3)) {
                    calibrate();
                } else {
                    calibrate();
                }
            }
        });


        test3 = mv2.getText().

                toString();

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
                exportCalibData();

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


                try {
                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CalibrationData.xlsx");

                    PdfSaveOptions options = new PdfSaveOptions();
                    options.setCompliance(PdfCompliance.PDF_A_1_B);
//                    File Pdfdir = new File(Environment.getExternalStorageDirectory()+"/LabApp/Currentlog/CalibPdf");
////                    if (!Pdfdir.exists()) {
////                        if (!Pdfdir.mkdirs()) {
////                            Log.d("App", "failed to create directory");
////                        }
////                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
                    File tempRoot = new File(tempPath);
                    fileNotWrite(tempRoot);
                    File[] tempFilesAndFolders = tempRoot.listFiles();
                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CalibrationData" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);

                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData";
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


                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(root);
                File[] filesAndFoldersPDF = rootPDF.listFiles();


                calibFileAdapter = new CalibFileAdapter(requireContext().getApplicationContext(), filesAndFoldersPDF);
                calibRecyclerView.setAdapter(calibFileAdapter);
                calibFileAdapter.notifyDataSetChanged();
                calibRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();
        File[] filesAndFoldersPDF = new File[1];

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            Toast.makeText(requireContext(), "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                if (filesAndFolders[i].getName().endsWith(".pdf")) {
                    filesAndFoldersPDF[0] = filesAndFolders[i];
                }
            }
        }

        calibFileAdapter = new

                CalibFileAdapter(requireContext().

                getApplicationContext(), filesAndFoldersPDF);
        calibRecyclerView.setAdapter(calibFileAdapter);
        calibFileAdapter.notifyDataSetChanged();
        calibRecyclerView.setLayoutManager(new

                LinearLayoutManager(requireContext().

                getApplicationContext()));


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

            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportTime + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(offset + "," + battery + "," + temp + "," + slope + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Callibration Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("pH,mV,DATE");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));

                String record1 = ph + "," + mv + "," + date;

                printWriter.println(record1);
            }
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Operator\nSign" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + "Supervisor\nSign" + "," + nullEntry + "," + nullEntry);

            calibCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);

            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/";
            Workbook workbook = new Workbook(inputFile + "CalibrationData.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 18.5);
            worksheet.getCells().setColumnWidth(2, 18.5);
            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/CalibrationData/CalibrationData.xlsx", SaveFormat.XLSX);

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
        String time = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
        databaseHelper.insert_action_data(time, "Calibrated by " + Source.userName, "", "", "", "");
        calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
        calibrateBtn.setEnabled(false);
        tvTimer.setVisibility(View.VISIBLE);
        isCalibrating = true;
        if (mode.equals("5")) {
            setupCoeffListener();
        } else {
            setupCoeffListenerThree();
        }
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
                        currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
                        bufferList.add(new BufferData(null, null, currentTime));
                        bufferListThree.add(new BufferData(null, null, currentTime));

                        if (spin.getSelectedItemPosition() == 0) {
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
                                if (coeff == null) return;
                                currentBuf += 1;
                            });
                        } else {
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValuesThree[currentBufThree] + 1);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabelsThree[currentBufThree]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
                                if (coeff == null) return;
                                currentBufThree += 1;
                            });
                        }

                        deviceRef.child("Data").child("PH_VAL").get().addOnSuccessListener(dataSnapshot -> {
                            Float ph = dataSnapshot.getValue(Float.class);
                            String phForm = String.format(Locale.UK, "%.2f", ph);
                            tvPhCurr.setText(phForm);
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
}
