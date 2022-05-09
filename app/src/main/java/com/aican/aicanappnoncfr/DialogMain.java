package com.aican.aicanappnoncfr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.data.DatabaseHelper;
import com.aican.aicanappnoncfr.specificactivities.Export;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DialogMain extends AppCompatDialogFragment {
    @NonNull
    public EditText userID, passcode;
    public Button authenticate, homeBtn;

    DatabaseHelper databaseHelper;

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog_layout, null);

        userID = view.findViewById(R.id.userId);
        passcode = view.findViewById(R.id.userPwd);
        authenticate = view.findViewById(R.id.authenticateRole);
        homeBtn = view.findViewById(R.id.homeBtn);

        databaseHelper = new DatabaseHelper(getContext());

        builder.setView(view);
        builder.setCancelable(false);

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), Dashboard.class));
            }
        });

        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Source.userId = userID.getText().toString();
                Source.userPasscode = passcode.getText().toString();
                for(int i=0; i<Source.id_fetched.size(); i++){
                    if(Source.userId.equals(Source.id_fetched.get(i)) && Source.userPasscode.equals(Source.passcode_fetched.get(i))){

                        String time = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
                        databaseHelper.insert_action_data(time, Source.userTrack + Source.name_fetched.get(i), "", "", "", "" );

                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor myShared = sharedPreferences.edit();

                        myShared.putString("userid", Source.userId);
                        myShared.commit();

                        if(Source.status_export && Source.role_fetched.get(i).equals("Supervisor")){
                            Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                            Source.status_export = false;
                            Intent intent = new Intent(getContext(), Export.class);
                            startActivity(intent);
                        }
                        else if(Source.status_export){
                            Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                        }

                        if(!Source.status_export){
                            Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                        }
                        Source.status_export = false;

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
