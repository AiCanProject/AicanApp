package com.aican.aicanapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogMain extends AppCompatDialogFragment {
    @NonNull
    public EditText userID, passcode;
    public Button authenticate;

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog_layout, null);

        userID = view.findViewById(R.id.userId);
        passcode = view.findViewById(R.id.userPwd);
        authenticate = view.findViewById(R.id.authenticateRole);

        builder.setView(view);
        builder.setCancelable(false);

        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Source.userId = userID.getText().toString();
                Source.userPasscode = passcode.getText().toString();
                for(int i=0; i<Source.id_fetched.size(); i++){
                    if(Source.userId.equals(Source.id_fetched.get(i)) && Source.userPasscode.equals(Source.passcode_fetched.get(i))){
                        Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }
                }
                Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }
}
