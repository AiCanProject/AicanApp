package com.aican.aicanappnoncfr.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.fragments.ph.PhCalibFragment;

import org.jetbrains.annotations.NotNull;

public class AuthenticateRoleDialog extends DialogFragment {

    EditText userId, passCode;
    Button generate;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_authenticate_role_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = view.findViewById(R.id.userId);
        passCode = view.findViewById(R.id.userPwd);
        generate = view.findViewById(R.id.authenticateRole);

        generate.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), PhCalibFragment.class);
            intent.putExtra(userId.getText().toString(), passCode.getText().toString());
            startActivity(intent);
        });
    }
}