package com.aican.aicanappnoncfr.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.specificactivities.PhActivity;
import com.aican.aicanappnoncfr.specificactivities.PhCalibrateActivity;

import org.jetbrains.annotations.NotNull;

public class  SelectCalibrationPointsDialog extends DialogFragment {

    TextView tvSelect;
    RadioGroup radioGroup;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_calib_points, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSelect = view.findViewById(R.id.tvSelect);
        radioGroup = view.findViewById(R.id.radioGroup);


        tvSelect.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), PhCalibrateActivity.class);
            if (radioGroup.getCheckedRadioButtonId() == R.id.rb3Point) {
                intent.putExtra(PhCalibrateActivity.CALIBRATION_TYPE, PhCalibrateActivity.THREE_POINT_CALIBRATION);
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.rb5Point) {
                intent.putExtra(PhCalibrateActivity.CALIBRATION_TYPE, PhCalibrateActivity.FIVE_POINT_CALIBRATION);
            }
            intent.putExtra(Dashboard.KEY_DEVICE_ID, PhActivity.DEVICE_ID);
            startActivity(intent);

            dismiss();
        });

    }
}
