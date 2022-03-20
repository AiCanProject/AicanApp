package com.aican.aicanapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DialogMain extends AppCompatDialogFragment {
    @NonNull
    public EditText userID, passcode;
    public Button authenticate;
    private String[] lines;

    private static final String FILE_NAME = "user_info.txt";

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog_layout, null);

        userID = view.findViewById(R.id.userId);
        passcode = view.findViewById(R.id.userPwd);
        authenticate = view.findViewById(R.id.authenticateRole);

        builder.setView(view);
        builder.setCancelable(false);

        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            lines = sb.toString().split("\\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Source.userId = userID.getText().toString();
                Source.userPasscode = passcode.getText().toString();
                if (Source.userPasscode.equals(lines[2]) && Source.userId.equals(lines[3])) {
                    Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                    dismiss();
                    Source.status = true;
                } else {
                    Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return builder.create();
    }
}
