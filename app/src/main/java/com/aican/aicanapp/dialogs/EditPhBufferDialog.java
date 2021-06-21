package com.aican.aicanapp.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aican.aicanapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

public class EditPhBufferDialog extends BottomSheetDialogFragment {

    Button btnChange;
    TextInputLayout tilPh;
    TextInputEditText etPh;

    OnValueChangedListener onValueChangedListener;

    public EditPhBufferDialog(OnValueChangedListener onValueChangedListener) {
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
        tilPh = view.findViewById(R.id.tilPh);
        etPh = view.findViewById(R.id.etPh);

        btnChange.setOnClickListener(v->{
            try{
                float ph = Float.parseFloat(etPh.getText().toString());
                onValueChangedListener.onValueChanged(ph);
                dismiss();
            }catch (Exception e){
                tilPh.setError("Invalid pH value");
            }
        });
    }

    public interface OnValueChangedListener{
        void onValueChanged(Float pH);
    }
}
