package com.aican.aicanapp.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public class AuthenticateRoleDialog extends DialogFragment {

    private static final String FILE_NAME = "user_info.txt";

    EditText userId, passCode;
    Button generate;
    String validPasscode, validUserID;
    String[] lines;
    static AtomicReference<Boolean> success;

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
        boolean success;
        generate.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), PhCalibFragment.class);
            intent.putExtra(userId.getText().toString(), passCode.getText().toString());
            //intent.putExtra("success", success);
            startActivity(intent);
        });
    }
}