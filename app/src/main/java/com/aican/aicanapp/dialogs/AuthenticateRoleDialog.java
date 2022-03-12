package com.aican.aicanapp.dialogs;


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
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class AuthenticateRoleDialog extends DialogFragment {

    EditText userId, passCode;
    Button generate, addSign;


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
        AtomicReference<Boolean> success = new AtomicReference<>(false);
        generate.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PhCalibFragment.class);
            intent.putExtra(userId.getText().toString(), passCode.getText().toString());
            intent.putExtra("success", success);
            startActivity(intent);

            if (isEmailValid() && isPassCodeValid()) {



                success.set(true);
                dismiss();
            }
        });
    }

    private boolean isPassCodeValid() {
        String validName = passCode.getText().toString();
        if (validName.isEmpty()) {
            Toast.makeText(requireContext(), "Enter Passcode!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isEmailValid() {
        String validName = userId.getText().toString();
        if (!validName.isEmpty()) {

        } else {
            Toast.makeText(requireContext(), "Enter Email Address!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

}