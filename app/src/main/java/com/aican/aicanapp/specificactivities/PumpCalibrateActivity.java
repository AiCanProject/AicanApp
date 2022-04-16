package com.aican.aicanapp.specificactivities;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.StepsAdapter;
import com.aican.aicanapp.dataClasses.Step;
import com.aican.aicanapp.pumpController.HorizontalSlider;
import com.aican.aicanapp.utils.ItemDecoratorBars;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class PumpCalibrateActivity extends AppCompatActivity {

    ImageView ivStartBtn;
    RelativeLayout startLayout, saveLayout;
    Button btnSave;
    HorizontalSlider slider;
    RecyclerView rvSteps;
    SwitchCompat switchDir;

    TextView tvTimer;
    ArrayList<Step> steps;
    StepsAdapter stepsAdapter;

    String deviceId = null;
    DatabaseReference deviceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_calibrate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        deviceId = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if (deviceId == null) throw new RuntimeException();

        ivStartBtn = findViewById(R.id.ivStartBtn);
        startLayout = findViewById(R.id.startLayout);
        saveLayout = findViewById(R.id.saveLayout);
        btnSave = findViewById(R.id.savBtn);
        slider = findViewById(R.id.slider);
        rvSteps = findViewById(R.id.rvSteps);
        switchDir = findViewById(R.id.switchClockwise);

        tvTimer = findViewById(R.id.tvTimer);


        steps = new ArrayList<>();
        stepsAdapter = new StepsAdapter(steps);
        rvSteps.setAdapter(stepsAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvSteps);
        rvSteps.addItemDecoration(new ItemDecoratorBars(
                px(15F), px(10F), px(20F),
                ContextCompat.getColor(this, R.color.grey_light),
                ContextCompat.getColor(this, R.color.colorPrimary)
        ));

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(deviceId)).getReference()
                .child("P_PUMP").child(deviceId);

        deviceRef.child("UI").child("Direction").get().addOnSuccessListener(snapshot -> {
            Integer dir = snapshot.getValue(Integer.class);
            if (dir == null) return;

            switchDir.setChecked(dir == 0);
        });

        deviceRef.child("UI").child("Cal").get().addOnSuccessListener(snapshot -> {
            Integer speed = snapshot.getValue(Integer.class);
            if (speed == null) return;
            slider.setProgress(speed);
        });


        switchDir.setOnCheckedChangeListener((v, isChecked) -> {
            deviceRef.child("UI").child("Direction").setValue(isChecked ? 0 : 1);
        });

        ivStartBtn.setOnClickListener(v -> {
            showSaveLayout();
            calibrate();
            deviceRef.child("UI").child("Start").setValue(1);
            //deviceRef.child("UI").child("Cal").setValue(1);
        });

        btnSave.setOnClickListener(v -> {
            Float calVal = slider.getProgress();
            deviceRef.child("UI").child("Cal").setValue(calVal);
            onBackPressed();
        });

        deviceRef.child("UI").child("Cal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //float calV = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadSteps();

    }

    private void showSaveLayout() {
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startLayout.startAnimation(slideOutLeft);
        saveLayout.setVisibility(View.VISIBLE);
        saveLayout.startAnimation(slideInRight);
    }

    private void loadSteps() {
        steps.add(new Step(ContextCompat.getColor(this, R.color.blue_dark)));
        steps.add(new Step(ContextCompat.getColor(this, R.color.green)));
        steps.add(new Step(ContextCompat.getColor(this, R.color.blue)));
        stepsAdapter.notifyDataSetChanged();
    }

    private void calibrate() {
        slider.setDisabled(true);
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
                    slider.setDisabled(false);
                    btnSave.setBackgroundColor(ContextCompat.getColor(PumpCalibrateActivity.this, R.color.colorPrimary));
                    btnSave.setText("Save");
                });
                deviceRef.child("UI").child("Start").setValue(0);
            }
        };
        timer.start();
    }

    private int px(Float value){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}