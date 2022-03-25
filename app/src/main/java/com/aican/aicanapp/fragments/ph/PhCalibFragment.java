package com.aican.aicanapp.fragments.ph;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;

import com.aican.aicanapp.Source;
import com.aican.aicanapp.UserDatabase;
import com.aican.aicanapp.UserDatabaseModel;
import com.aican.aicanapp.adapters.BufferAdapter;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.dialogs.ExitConfirmDialog;
import com.aican.aicanapp.utils.OnBackPressed;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhCalibFragment extends Fragment implements OnBackPressed {

    private static final String FILE_NAME = "user_info.txt";


    String deviceId;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, tvTimer, lastCalib;
    TextView ph1, mv1, phEdit1, ph2, mv2, phEdit2, ph3, mv3, phEdit3, ph4, mv4, phEdit4, ph5, mv5, phEdit5;
    DatabaseReference deviceRef;
    RecyclerView bufferRecycler;
    Button calibrateBtn, btnNext;
    Spinner spin;
    String[] mode = { "5"};

    ArrayList<BufferData> bufferList = new ArrayList<>();

    String[] lines;
    LinearLayout ll1;


    float[] buffers = new float[]{1.0F, 4.0F, 7.0F, 9.2F, 12.0F};
    String[] bufferLabels = new String[]{"B_1", "B_2", "B_3", "B_4", "B_5"};
    String[] coeffLabels = new String[]{"VAL_1", "VAL_2", "VAL_3", "VAL_4", "VAL_5"};
    int[] calValues = new int[]{10, 20, 30, 40, 50};
    int currentBuf = 0;

    DatabaseReference coeffRef = null;
    float coeff = 0;
    float ph = 0;
    boolean isCalibrating = false;

    String buff;
    String coef;
    String currentTime;
    String strDate;

    private void loadBuffers() {
        deviceRef.child("UI").child("PH").child("PH_CAL").get().addOnSuccessListener(snapshot -> {
            for (int i = 0; i < bufferLabels.length; ++i) {
                buffers[i] = Float.parseFloat(snapshot.child(bufferLabels[i]).getValue(String.class));
            }

            ph1.setText(String.valueOf(buffers[0]));
            ph2.setText(String.valueOf(buffers[1]));
            ph3.setText(String.valueOf(buffers[2]));
            ph4.setText(String.valueOf(buffers[3]));
            ph5.setText(String.valueOf(buffers[4]));
        });

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ph1 && v.getId() == R.id.mv1) {
                ph1.setBackgroundColor(Color.GRAY);
                mv1.setBackgroundColor(Color.GRAY);
            }
        }
    };

    private void displayCoeffAndPrepareNext(float coeff) {
        if (currentBuf == buffers.length - 1) {
            btnNext.setText("Done");
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
            isCalibrating = false;
        }
        btnNext.setVisibility(View.VISIBLE);

        coef = String.format(Locale.UK, "%.2f", coeff);
        //bufferList.add(new BufferData(null,String.format(Locale.UK, "%.2f", coeff)));
        //bufferData.setMv(String.format(Locale.UK, "%.2f", coeff));
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
                if (ph == null) return;
                String phForm = String.format(Locale.UK, "%.2f", ph);
                tvPhCurr.setText(phForm);
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

        deviceRef.child("UI").child("PH").child("PH_CAL").child("VAL_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.1f", ec);
                mv1.setText(ecForm);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("VAL_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.1f", ec);
                mv2.setText(ecForm);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("UI").child("PH").child("PH_CAL").child("VAL_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.1f", ec);
                mv3.setText(ecForm);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("VAL_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.1f", ec);
                mv4.setText(ecForm);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("VAL_5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ec = snapshot.getValue(Float.class);
                String ecForm = String.format(Locale.UK, "%.1f", ec);
                mv5.setText(ecForm);
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
        } else {
            tvPhCurr.setText(newText);
        }
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastCalib = view.findViewById(R.id.lastCalibration);
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

        ll1 = view.findViewById(R.id.ll1);
        phEdit1 = view.findViewById(R.id.phEdit1);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);
        tvEcCurr = view.findViewById(R.id.tvEcCurr);
        calibrateBtn = view.findViewById(R.id.startBtn);
        btnNext = view.findViewById(R.id.nextBtn);
        spin = view.findViewById(R.id.calibMode);
        //bufferRecycler = view.findViewById(R.id.buffer_items);

        btnNext.setEnabled(false);
        ArrayAdapter aa = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mode);

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);



        DialogMain dialogMain = new DialogMain();
        // dialogMain.setCancelable(false);

        dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");

        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


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
                if (Source.status) {
                    calibrate();
                } else {
                    Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lastCalib.setText("Last Calibration by ");

        btnNext.setOnClickListener(v -> {
            if (currentBuf >= buffers.length - 1) {
                return;
            }
            btnNext.setVisibility(View.INVISIBLE);
            currentBuf += 1;

            calibrateBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            calibrateBtn.setEnabled(true);
            updateBufferValue((float) buffers[currentBuf]);
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        setupListeners();

        loadBuffers();

        //bufferRecycler.setHasFixedSize(true);
        //bufferRecycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,true));
        //BufferAdapter bufferAdapter = new BufferAdapter(getBuffer(), requireContext());
        //bufferAdapter.notifyDataSetChanged();
        //bufferRecycler.setAdapter(bufferAdapter);

    }


    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.phEdit1:
            case R.id.phEdit2:
            case R.id.phEdit3:
            case R.id.phEdit4:
            case R.id.phEdit5:


                EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
                    updateBufferValue(ph);
                    deviceRef.child("UI").child("PH").child("PH_CAL").child(bufferLabels[currentBuf]).setValue(String.valueOf(ph));
                });
                dialog.show(getParentFragmentManager(), null);
                break;
            default:
                break;
        }
    }

    private void updateBufferValue(Float value) {
        String newValue = String.valueOf(value);
        //bufferData.setPh(newValue);
        //ph2.setText(newValue);
    }

    public void calibrate() {
        Source.status = false;
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
                        currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
                        bufferList.add(new BufferData(null, null, currentTime));
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf] + 1);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                            Float coeff = dataSnapshot.getValue(Float.class);
                            if (coeff == null) return;
                            displayCoeffAndPrepareNext(coeff);
                            btnNext.setEnabled(true);

                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm");
                            strDate = simpleDateFormat.format(date);
                            //bufferData.setTime(strDate);


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

            dialog.show(getParentFragmentManager(), null);
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}