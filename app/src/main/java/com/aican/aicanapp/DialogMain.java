package com.aican.aicanapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.PhMvTable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogMain extends AppCompatDialogFragment {
    Context context;
    public DialogMain(Context context){
        this.context = context;
    }
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
                ((Activity) context).finish();

//                startActivity(new Intent(getContext(), Dashboard.class));
            }
        });

        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Source.userId = userID.getText().toString();
                Source.userPasscode = passcode.getText().toString();
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String presentDate = dateFormat.format(date);
                Date present = getParsedDate(presentDate);
                for (int i = 0; i < Source.id_fetched.size(); i++) {
                    if (Source.userId.equals(Source.id_fetched.get(i)) && Source.userPasscode.equals(Source.passcode_fetched.get(i))) {
                        Source.logUserName = Source.name_fetched.get(i);
                        Source.loginUserRole = Source.role_fetched.get(i);
                        if (present.compareTo(getParsedDate(Source.expiryDate_fetched.get(i))) < 0) {
                            Log.d("expiryDate", "Present date :" + presentDate + " Expiry Date: " + Source.expiryDate_fetched.get(i));
                            String date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                            databaseHelper.insert_action_data(time, date1, Source.userTrack + Source.name_fetched.get(i), "", "", "", "", PhActivity.DEVICE_ID);

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor myShared = sharedPreferences.edit();

                            myShared.putString("userid", Source.userId);
                            myShared.commit();

                            if (Source.status_export && (Source.role_fetched.get(i).equals("Supervisor") || Source.role_fetched.get(i).equals("Admin"))) {

                                Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                                Source.status_export = false;
                                Intent intent = new Intent(getContext(), Export.class);
                                startActivity(intent);
                            } else if (Source.status_export) {
                                Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                            }

                            if (!Source.status_export) {
                                Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                            }
                            Source.status_export = false;


                            if (Source.status_phMvTable && (Source.role_fetched.get(i).equals("Supervisor") || Source.role_fetched.get(i).equals("Admin"))) {

                                Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                                Source.status_phMvTable = false;
                                Intent intent = new Intent(getContext(), PhMvTable.class);
                                startActivity(intent);
                            } else if (Source.status_phMvTable) {
                                Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                            }


                            if (!Source.status_phMvTable) {
                                Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                            }
                            Source.status_phMvTable = false;

                            dismiss();
                            PhFragment.checking();
                            return;
                        } else {
                            Toast.makeText(getContext(), "Password expired, please change it", Toast.LENGTH_SHORT).show();
                            Log.d("expiryDate", "Present date :" + presentDate + " Expiry Date: " + Source.expiryDate_fetched.get(i));
                        }
                    }
                }
                Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

    private Date getParsedDate(String date) {
        Date presentDt = null;
        try {
            presentDt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    .parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return presentDt;
    }
}
