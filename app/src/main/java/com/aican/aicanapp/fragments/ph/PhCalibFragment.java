package com.aican.aicanapp.fragments.ph;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
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
import android.widget.VideoView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dialogs.SelectCalibrationPointsDialog;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.PhCalibrateActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PhCalibFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String THREE_POINT_CALIBRATION = "3";
    public static final String FIVE_POINT_CALIBRATION = "5";
    public static final String CALIBRATION_TYPE = "calibration_type";


    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, tvTimer;
    TextView ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5;
    DatabaseReference deviceRef;
    LinearLayout point3, point5;
    Button calibrateBtn, btnNext;
    Spinner spin;
    String[] mode = {"3", "5"};


    float[] buffers = new float[]{2.0F, 4.0F, 7.0F, 9.0F, 11.0F};
    String[] bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
    String[] coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
    int[] calValues = new int[]{10, 20, 30, 40, 50};
    int currentBuf = 0;

    String deviceId;
    String calibrationType;
    Activity activity;


    DatabaseReference coeffRef = null;

    float coeff = 0;
    float ph = 0;
    boolean isCalibrating = false;

    private void loadBuffers() {
        deviceRef.child("UI").child("PH").child("PH_CAL").get().addOnSuccessListener(snapshot -> {
            for (int i = 0; i < bufferLabels.length; ++i) {
                buffers[i] = Float.parseFloat(snapshot.child(bufferLabels[i]).getValue(String.class));

            }

            ph1.setText(String.valueOf(buffers[0]));
            ph2.setText(String.valueOf(buffers[1]));
            ph3.setText(String.valueOf(buffers[2]));

            if (point3.getVisibility() == View.VISIBLE && point5.getVisibility() == View.VISIBLE) {
                ph4.setText(String.valueOf(buffers[3]));
                ph5.setText(String.valueOf(buffers[4]));
            }
        });
    }


    private void displayCoeffAndPrepareNext(float coeff) {
        if(currentBuf==buffers.length-1) {
            btnNext.setText("Done");
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
            isCalibrating = false;
        }
        btnNext.setVisibility(View.VISIBLE);

        //tvCoefficientLabel.setVisibility(View.VISIBLE);
        mv1.setText(String.format(Locale.UK, "%.2f", coeff));
        //tvCoefficient.setVisibility(View.VISIBLE);
    }

    private void setupCoeffListener() {
        if (coeffRef != null) {
            coeffRef.removeEventListener(coeffListener);
        }
        coeffRef = deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]);
        coeffRef.addValueEventListener(coeffListener);
    }


    private void setupListeners() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                //if(ph==null) return;
                //String phFormat = String.format(Locale.UK, "%.2f", ph);
                //tvPhCurr.setText(phFormat);

                //phView.moveTo(ph);
                updatePh(ph);
                //PhCalibFragment.this.tvPhNext = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String temp = snapshot.getValue(Integer.class).toString();
                tvTempCurr.setText(temp + "°C");
                //updatePh(temp);
                //PhFragment.this.ph = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String ec = snapshot.getValue(Integer.class).toString();
                tvEcCurr.setText(ec);
                //updatePh(temp);
                //PhFragment.this.ph = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void updatePh(float ph) {
        String newText;
        if (ph < 0 || ph > 14) {
            newText = "--";
        } else {
            newText = String.format(Locale.UK, "%.2f", ph);
        }
        tvPhNext.setText(newText);

        if (getContext() != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
            Animation slideInBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvPhCurr.setVisibility(View.INVISIBLE);
                    TextView t = tvPhCurr;
                    tvPhCurr = tvPhNext;
                    tvPhNext = t;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            tvPhCurr.startAnimation(fadeOut);
            tvPhNext.setVisibility(View.VISIBLE);
            tvPhNext.startAnimation(slideInBottom);
        }else{
            tvPhCurr.setText(newText);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ph_calib, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //calibrationType = getIntent().getStringExtra(CALIBRATION_TYPE);

        ph1 = view.findViewById(R.id.ph1);
        ph2 = view.findViewById(R.id.ph2);
        ph3 = view.findViewById(R.id.ph3);
        ph4 = view.findViewById(R.id.ph4);
        ph5 = view.findViewById(R.id.ph5);
        mv1 = view.findViewById(R.id.mv1);
        mv2 = view.findViewById(R.id.mv2);
        mv3 = view.findViewById(R.id.mv3);
        mv4 = view.findViewById(R.id.mv4);
        mv5 = view.findViewById(R.id.mv5);


        tvTimer = view.findViewById(R.id.tvTimer);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);
        tvEcCurr = view.findViewById(R.id.tvEcCurr);
        calibrateBtn = view.findViewById(R.id.startBtn);
        btnNext = view.findViewById(R.id.btnNext);
        point3 = view.findViewById(R.id.log3point);
        point5 = view.findViewById(R.id.log5Point);

        spin = view.findViewById(R.id.calibMode);
        spin.setOnItemSelectedListener(this);
        ArrayAdapter aa = new ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);

        calibrateBtn.setOnClickListener(v -> {
            calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryAlpha));
            calibrateBtn.setEnabled(false);
            tvTimer.setVisibility(View.VISIBLE);
            isCalibrating = true;
            setupCoeffListener();
            CountDownTimer timer = new CountDownTimer(120000, 1000) {
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
                            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                            deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                                Float coeff = dataSnapshot.getValue(Float.class);
                                if (coeff == null) return;
                                displayCoeffAndPrepareNext(coeff);
                            });

                            handler.postDelayed(runnable, 1000);
                        }};
                    runnable.run();

                }
            };
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t -> {
                timer.start();
            });


        });


        btnNext.setOnClickListener(v -> {
            if (currentBuf >= buffers.length - 1) {
                //onBackPressed();
                return;
            }
            btnNext.setVisibility(View.INVISIBLE);
            currentBuf += 1;

            calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            calibrateBtn.setEnabled(true);

            //phView.moveTo(buffers[currentBuf]);
            updateBufferValue(buffers[currentBuf]);

            //tvCoefficient.setVisibility(View.INVISIBLE);
            //tvCoefficientLabel.setVisibility(View.INVISIBLE);

        });




        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        // setupGraph();
        setupListeners();
        loadBuffers();


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        //Toast.makeText(requireContext(),mode[i] , Toast.LENGTH_LONG).show();
        //calibrationType = spin.getItemAtPosition(i).toString();
        if (spin.getSelectedItemPosition() == 0) {


            buffers = new float[]{4.0F, 7.0F, 9.0F};
            bufferLabels = new String[]{"B_2", "B_3", "B_4"};
            coeffLabels = new String[]{"VAL_2", "VAL_3", "VAL_4"};
            calValues = new int[]{20, 30, 40};

            point3.setVisibility(View.VISIBLE);
            point5.setVisibility(View.GONE);
        }else if (spin.getSelectedItemPosition() == 1) {


            buffers = new float[]{4.0F, 7.0F, 9.0F};
            bufferLabels = new String[]{"B_2", "B_3", "B_4"};
            coeffLabels = new String[]{"VAL_2", "VAL_3", "VAL_4"};
            calValues = new int[]{20, 30, 40};

            point5.setVisibility(View.VISIBLE);
            point3.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void updateBufferValue(Float value) {
        String newValue = String.valueOf(value);
        ph2.setText(newValue);

        //Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        //Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        //fadeOut.setAnimationListener(new Animation.AnimationListener() {
          //  @Override
            //public void onAnimationStart(Animation animation) {
            //}
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                tvBufferCurr.setVisibility(View.INVISIBLE);
//                TextView t = tvBufferCurr;
//                tvBufferCurr = tvBufferNext;
//                tvBufferNext = t;
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        tvBufferCurr.startAnimation(fadeOut);
//        tvBufferNext.setVisibility(View.VISIBLE);
//        tvBufferNext.startAnimation(slideInBottom);
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
    boolean isTimeOptionsVisible = false;


}