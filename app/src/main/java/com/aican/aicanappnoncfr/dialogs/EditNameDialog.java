package com.aican.aicanappnoncfr.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aican.aicanappnoncfr.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

public class EditNameDialog extends BottomSheetDialogFragment {

    String deviceId;
    String type;
    String name;
    OnNameChangedListener onNameChangedListener;
    EditText etName;
    Button btnSave;

    public EditNameDialog(String deviceId, String type, String name, OnNameChangedListener onNameChangedListener) {
        this.deviceId = deviceId;
        this.type = type;
        this.name = name;
        this.onNameChangedListener = onNameChangedListener;
    }

    public EditNameDialog(String name) {
        this.name = name;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        btnSave = view.findViewById(R.id.btnSave);

        etName.setText(name);

        btnSave.setOnClickListener(v -> {
            if (validate()) {
                onNameChangedListener.onNameChanged(deviceId, type, etName.getText().toString());
                dismiss();
            }
        });

    }

    private boolean validate() {
        if (etName.getText() == null || etName.getText().length() == 0) {
            return false;
        }
        return true;
    }

    public interface OnNameChangedListener {
        void onNameChanged(String deviceId, String type, String newName);
    }
}
