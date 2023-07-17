package com.aican.aicanapp.userdatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.specificactivities.PhActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditUserDatabase extends AppCompatActivity {
    Spinner spinner;
    EditText name, passwordText;
    String[] r = {"Operator", "Supervisor"};
    DatabaseHelper databaseHelper;
    String username = "";
    String userRole = "";
    Button update;
    String password = "";
    String uid = "";
    String prevPasscode = "";
    TextView previPasscode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_database);

        spinner = findViewById(R.id.selectRole);
        name = findViewById(R.id.username);
        databaseHelper = new DatabaseHelper(this);
        update = findViewById(R.id.updateBtn);
        passwordText = findViewById(R.id.password);
        previPasscode = findViewById(R.id.previPasscode);

        Intent intent = getIntent();

        username = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userrole");
        uid = intent.getStringExtra("uid");
        prevPasscode = intent.getStringExtra("passcode");

        name.setText(username);

        ArrayAdapter<String> role = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, r);
        role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(role);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userRole = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (userRole.equals(r[0])) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(1);
        }

//        previPasscode.setText(prevPasscode);

        update.setOnClickListener(view -> {

            if (passwordText.getText().toString().isEmpty() || passwordText.getText().toString().equals("") || passwordText.getText().toString().equals(prevPasscode)) {
                if (passwordText.getText().toString().equals(prevPasscode)) {
                    previPasscode.setText("Your previous passcode is same as your current passcode");
                    passwordText.setError("Your previous passcode is same as your current passcode");
                } else {
                    passwordText.setError("Enter some password");
                }
            } else {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                password = passwordText.getText().toString();
                boolean passwordUpdated = databaseHelper.updateUserDetails(username, uid, name.getText().toString(), userRole, password);
                if (passwordUpdated) {
                    databaseHelper.insert_action_data(time, date, "Username: " + username + ", Name: " + name.getText().toString() + ", UID: " + uid
                            + " Password changed", "", "", "", "", PhActivity.DEVICE_ID);

                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getExpiryDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cal.setTime(sdf.parse(presentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // use add() method to add the days to the given date
        cal.add(Calendar.DAY_OF_MONTH, 90);
        String expiryDate = sdf.format(cal.getTime());

        return expiryDate;
    }

    private String getPresentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);
        return presentDate;
    }

}