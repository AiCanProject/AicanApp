package com.aican.aicanapp.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.IPhCalibrateActivity;
import com.aican.aicanapp.specificactivities.IndusPhActivity;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.PhCalibrateActivity;

import org.jetbrains.annotations.NotNull;

public class IndustryCalibrationPointDialog extends DialogFragment {

    TextView tvSelect;
    RadioGroup radioGroup;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_industry_calibration_point_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSelect = view.findViewById(R.id.tvSelect);
        radioGroup = view.findViewById(R.id.radioGroup);


        tvSelect.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), IPhCalibrateActivity.class);
            /*if (radioGroup.getCheckedRadioButtonId() == R.id.rb3Point) {
                intent.putExtra(PhCalibrateActivity.CALIBRATION_TYPE, PhCalibrateActivity.THREE_POINT_CALIBRATION);
            } else */
            if (radioGroup.getCheckedRadioButtonId() == R.id.rb2Point) {
                intent.putExtra(IPhCalibrateActivity.CALIBRATION_TYPE, PhCalibrateActivity.FIVE_POINT_CALIBRATION);
            }
            intent.putExtra(Dashboard.KEY_DEVICE_ID, IndusPhActivity.DEVICE_ID);
            startActivity(intent);

            dismiss();
        });

    }

}