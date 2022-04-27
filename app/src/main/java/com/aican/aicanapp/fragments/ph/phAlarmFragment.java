package com.aican.aicanapp.fragments.ph;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.aican.aicanapp.R;

public class phAlarmFragment extends Fragment {


    RadioGroup radioGroup;
    EditText inputPh;
    Button alarm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ph_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = view.findViewById(R.id.groupradio);
       // inputPh = view.findViewById(R.id.reqType);
        alarm = view.findViewById(R.id.startAlarm);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
                    }
                });

        alarm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(requireContext(), "No answer has been selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    RadioButton radioButton = (RadioButton)radioGroup.findViewById(selectedId);

                    Toast.makeText(requireContext(), radioButton.getText(), Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}