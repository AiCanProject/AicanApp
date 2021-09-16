package com.aican.aicanapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aican.aicanapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

public class EditSetTempDialog extends BottomSheetDialogFragment {

    Button btnChange;
    TextInputLayout tilSetTemp;
    TextInputEditText etSetTemp;

    OnValueChangedListener onValueChangedListener;
    public interface OnValueChangedListener {
        void onValueChanged(int setTemp);
    }

    public EditSetTempDialog(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_ph_buffer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnChange = view.findViewById(R.id.btnChange);
        tilSetTemp = view.findViewById(R.id.tilPh);
        etSetTemp = view.findViewById(R.id.etPh);
        etSetTemp.setHint("Set Temperature");

        btnChange.setOnClickListener(v -> {
            try{
                int temp= Integer.parseInt(etSetTemp.getText().toString());
                if(temp>=20&& temp<=300) {
                    onValueChangedListener.onValueChanged(temp);
                    dismiss();
                }
                else{
                    tilSetTemp.setError("Invalid Temperature");
                }
            }catch (Exception e){
                tilSetTemp.setError("Invalid Temperature");
            }
        });
    }
    @Override
    public void onResume() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onResume();
    }
}
