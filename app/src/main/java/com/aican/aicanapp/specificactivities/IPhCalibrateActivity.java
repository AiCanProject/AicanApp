package com.aican.aicanapp.specificactivities;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.dialogs.ExitConfirmDialog;
import com.aican.aicanapp.ph.PhView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class IPhCalibrateActivity extends AppCompatActivity {


    public static final String TWO_POINT_CALIBRATION = "two";
    public static final String FIVE_POINT_CALIBRATION = "five";
    public static final String CALIBRATION_TYPE = "calibration_type";

    PhView phView;
    Button btnStart, btnNext;
    TextView tvTimer, tvCoefficient, tvBufferCurr, tvBufferNext, tvPh, tvCoefficientLabel, tvEdit;
    Toolbar toolbar;
    LineChart lineChart;
    LinearLayout llStart, llStop, llClear, llExport;

    ArrayList<Entry> entriesOriginal;

    float[] buffers = new float[]{4.0F, 7.0F};
    String[] bufferLabels = new String[]{"B_2", "B_3"};
    String[] coeffLabels = new String[]{"VAL_2", "VAL_3"};
    int[] calValues = new int[]{11, 20, 30, 40, 50};
    int currentBuf = 0;

    DatabaseReference deviceRef;
    String deviceId;
    String calibrationType;

    float coeff = 0;
    float ph = 0;
    boolean isCalibrating = false;
    ValueEventListener coeffListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
            Float coeff = snapshot.getValue(Float.class);
            if (coeff == null) return;
            IPhCalibrateActivity.this.coeff = coeff;
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {

        }
    };
    boolean isTimeOptionsVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iph_calibrate);


        deviceId = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        calibrationType = getIntent().getStringExtra(CALIBRATION_TYPE);
        if (deviceId == null || calibrationType == null) {
            throw new RuntimeException();
        }

        if (calibrationType.equals(TWO_POINT_CALIBRATION)) {

            buffers = new float[]{4.0F, 7.0F};
            bufferLabels = new String[]{"B_2", "B_3"};
            coeffLabels = new String[]{"VAL_2", "VAL_3", "VAL_4"};
            calValues = new int[]{11};

        }


        phView = findViewById(R.id.phView);
//        phTextView = findViewById(R.id.phTextView);
        btnStart = findViewById(R.id.startBtn);
        btnNext = findViewById(R.id.next_btn);
        tvTimer = findViewById(R.id.tvTimer);
//        plvCoefficient = findViewById(R.id.plvCoefficient);
        tvCoefficient = findViewById(R.id.tvCoefficient);
        toolbar = findViewById(R.id.main_toolbar);
        tvBufferCurr = findViewById(R.id.tvBufferCurr);
        tvBufferNext = findViewById(R.id.tvBufferNext);
        tvPh = findViewById(R.id.tvPh);
        tvCoefficientLabel = findViewById(R.id.tvCoefficientLabel);
        lineChart = findViewById(R.id.line_chart);
        tvEdit = findViewById(R.id.tvEdit);


        tvBufferCurr.setText(String.valueOf(buffers[0]));

        phView.setCurrentPh(buffers[0]);

        entriesOriginal = new ArrayList<>();

        btnStart.setOnClickListener(v -> {
            btnStart.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryAlpha));
            btnStart.setEnabled(false);
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

                @Override
                public void onFinish() {
                    runOnUiThread(() -> {
                        tvTimer.setVisibility(View.INVISIBLE);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                            Float coeff = dataSnapshot.getValue(Float.class);
                            if (coeff == null) return;
                            displayCoeffAndPrepareNext(coeff);
                        });
                    });
                }
            };
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
                timer.start();
            });
        });

        btnNext.setOnClickListener(v -> {
            if (currentBuf >= buffers.length - 1) {
                onBackPressed();
                return;
            }
            btnNext.setVisibility(View.INVISIBLE);
            currentBuf += 1;

            btnStart.setBackgroundColor(ContextCompat.getColor(IPhCalibrateActivity.this, R.color.colorPrimary));
            btnStart.setEnabled(true);

            phView.moveTo(buffers[currentBuf]);
            updateBufferValue(buffers[currentBuf]);

            tvCoefficient.setVisibility(View.INVISIBLE);
            tvCoefficientLabel.setVisibility(View.INVISIBLE);

        });


        tvEdit.setOnClickListener(v -> {


            EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
                updateBufferValue(ph);
                deviceRef.child("UI").child("PH").child("PH_CAL").child(bufferLabels[currentBuf]).setValue(String.valueOf(ph));
            });
            dialog.show(getSupportFragmentManager(),null);

        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(deviceId)).getReference().child("IPHMETER").child(IndusPhActivity.DEVICE_ID);

        //setupGraph();
        loadBuffers();
        setupPhListener();
        setupCoeffListener();


    }

    DatabaseReference coeffRef = null;

    private void loadBuffers() {
        deviceRef.child("UI").child("PH").child("PH_CAL").get().addOnSuccessListener(snapshot -> {
            for (int i = 0; i < bufferLabels.length; ++i) {
                buffers[i] = Float.parseFloat(snapshot.child(bufferLabels[i]).getValue(String.class));
            }

            tvBufferCurr.setText(String.valueOf(buffers[0]));
            phView.setCurrentPh(buffers[0]);
        });
    }


    private void displayCoeffAndPrepareNext(float coeff) {
        if(currentBuf==buffers.length-1) {
            btnNext.setText("Done");
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
            isCalibrating = false;
        }
        btnNext.setVisibility(View.VISIBLE);

        tvCoefficientLabel.setVisibility(View.VISIBLE);
        tvCoefficient.setText(String.format(Locale.UK, "%.2f", coeff));
        tvCoefficient.setVisibility(View.VISIBLE);
    }

    private void setupCoeffListener() {
        if (coeffRef != null) {
            coeffRef.removeEventListener(coeffListener);
        }
        coeffRef = deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]);
        coeffRef.addValueEventListener(coeffListener);
    }

    private void setupPhListener() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if (ph == null) return;
                String phString = String.format(Locale.UK, "%.2f", ph);
                tvPh.setText(phString);
                IPhCalibrateActivity.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private int getAttr(@AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrRes, typedValue, true);

        return typedValue.data;
    }

    private void updateBufferValue(Float value) {
        String newValue = String.valueOf(value);
        tvBufferNext.setText(newValue);

        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvBufferCurr.setVisibility(View.INVISIBLE);
                TextView t = tvBufferCurr;
                tvBufferCurr = tvBufferNext;
                tvBufferNext = t;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tvBufferCurr.startAnimation(fadeOut);
        tvBufferNext.setVisibility(View.VISIBLE);
        tvBufferNext.startAnimation(slideInBottom);
    }


    @Override
    public void onBackPressed() {
        if (isCalibrating) {
            ExitConfirmDialog dialog = new ExitConfirmDialog((new ExitConfirmDialog.DialogCallbacks() {
                @Override
                public void onYesClicked(Dialog dialog1) {
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                    dialog1.dismiss();
                    isCalibrating = false;
                    onBackPressed();
                }

                @Override
                public void onNoClicked(Dialog dialog1) {
                    dialog1.dismiss();
                }
            }));

            dialog.show(getSupportFragmentManager(), null);
        } else {
            super.onBackPressed();
        }
    }

}