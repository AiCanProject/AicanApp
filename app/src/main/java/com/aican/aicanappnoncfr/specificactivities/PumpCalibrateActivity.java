package com.aican.aicanappnoncfr.specificactivities;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.adapters.StepsAdapter;
import com.aican.aicanappnoncfr.dataClasses.Step;
import com.aican.aicanappnoncfr.pumpController.HorizontalSlider;
import com.aican.aicanappnoncfr.utils.ItemDecoratorBars;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PumpCalibrateActivity extends AppCompatActivity {

    ImageView ivStartBtn;
    RelativeLayout startLayout, saveLayout;
    Button btnSave;
    HorizontalSlider slider;
    RecyclerView rvSteps;
    SwitchCompat switchDir;

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

        ivStartBtn.setOnClickListener(v -> {
            showSaveLayout();
            calibrate();
            deviceRef.child("UI").child("STATUS").setValue(PumpActivity.STATUS_CAL_START);
            deviceRef.child("UI").child("CAL").child("CAL").setValue(1);
        });

        btnSave.setOnClickListener(v -> {
            int calVal = slider.getProgress();
            deviceRef.child("UI").child("CAL").child("CAL_VAL").setValue(calVal);
            onBackPressed();
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
        CountDownTimer timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                runOnUiThread(() -> {
                    slider.setDisabled(false);
                    btnSave.setBackgroundColor(ContextCompat.getColor(PumpCalibrateActivity.this, R.color.colorPrimary));
                    btnSave.setText("Save");
                });
                deviceRef.child("UI").child("STATUS").setValue(PumpActivity.STATUS_CAL_FINISH);
                deviceRef.child("UI").child("CAL").child("CAL").setValue(0);
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