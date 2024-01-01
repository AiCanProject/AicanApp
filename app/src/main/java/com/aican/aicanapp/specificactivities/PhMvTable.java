package com.aican.aicanapp.specificactivities;

import static com.aican.aicanapp.utils.Constants.SERVER_PATH;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.utils.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class PhMvTable extends AppCompatActivity {

    TextView ph1, minMV1, phEdit1, ph2, minMV2, phEdit2, ph3, minMV3, phEdit3, ph4, minMV4, phEdit4, ph5, minMV5, phEdit5, maxMV1, maxMV2, maxMV3, maxMV4, maxMV5;
    TextView minMVEdit1, minMVEdit2, minMVEdit3, minMVEdit4, minMVEdit5, maxMVEdit1, maxMVEdit2, maxMVEdit3, maxMVEdit4, maxMVEdit5;
    String MIN_MV1, MIN_MV2, MIN_MV3, MIN_MV4, MIN_MV5, MAX_MV1, MAX_MV2, MAX_MV3, MAX_MV4, MAX_MV5,
            PH1, PH2, PH3, PH4, PH5;
    DatabaseHelper databaseHelper;
    DatabaseReference deviceRef;
    EditText tempValue;
    Button setATC;
    WebSocket webSocket1;
    JSONObject jsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph_mv_table);

        maxMVEdit1 = findViewById(R.id.maxMVEdit1);
        maxMVEdit2 = findViewById(R.id.maxMVEdit2);
        maxMVEdit3 = findViewById(R.id.maxMVEdit3);
        maxMVEdit4 = findViewById(R.id.maxMVEdit4);
        maxMVEdit5 = findViewById(R.id.maxMVEdit5);

        setATC = findViewById(R.id.setATC);
        tempValue = findViewById(R.id.tempValue);

        minMVEdit1 = findViewById(R.id.minMVEdit1);
        minMVEdit2 = findViewById(R.id.minMVEdit2);
        minMVEdit3 = findViewById(R.id.minMVEdit3);
        minMVEdit4 = findViewById(R.id.minMVEdit4);
        minMVEdit5 = findViewById(R.id.minMVEdit5);

        phEdit1 = findViewById(R.id.phEdit1);
        phEdit2 = findViewById(R.id.phEdit2);
        phEdit3 = findViewById(R.id.phEdit3);
        phEdit4 = findViewById(R.id.phEdit4);
        phEdit5 = findViewById(R.id.phEdit5);

        jsonData = new JSONObject();

        maxMV1 = findViewById(R.id.maxMV1);
        maxMV2 = findViewById(R.id.maxMV2);
        maxMV3 = findViewById(R.id.maxMV3);
        maxMV4 = findViewById(R.id.maxMV4);
        maxMV5 = findViewById(R.id.maxMV5);

        minMV1 = findViewById(R.id.minMV1);
        minMV2 = findViewById(R.id.minMV2);
        minMV3 = findViewById(R.id.minMV3);
        minMV4 = findViewById(R.id.minMV4);
        minMV5 = findViewById(R.id.minMV5);

        ph1 = findViewById(R.id.ph1);
        ph2 = findViewById(R.id.ph2);
        ph3 = findViewById(R.id.ph3);
        ph4 = findViewById(R.id.ph4);
        ph5 = findViewById(R.id.ph5);

        tempValue = findViewById(R.id.tempValue);

        phEdit1.setOnClickListener(this::onClick);
        phEdit2.setOnClickListener(this::onClick);
        phEdit3.setOnClickListener(this::onClick);
        phEdit4.setOnClickListener(this::onClick);
        phEdit5.setOnClickListener(this::onClick);

        maxMVEdit1.setOnClickListener(this::onClick);
        maxMVEdit2.setOnClickListener(this::onClick);
        maxMVEdit3.setOnClickListener(this::onClick);
        maxMVEdit4.setOnClickListener(this::onClick);
        maxMVEdit5.setOnClickListener(this::onClick);

        minMVEdit1.setOnClickListener(this::onClick);
        minMVEdit2.setOnClickListener(this::onClick);
        minMVEdit3.setOnClickListener(this::onClick);
        minMVEdit4.setOnClickListener(this::onClick);
        minMVEdit5.setOnClickListener(this::onClick);


        databaseHelper = new DatabaseHelper(PhMvTable.this);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        databaseHelper.insert_action_data(time, date, "pHMvTable : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

        databaseHelper.deletePhBufferMVTable();
        insertIntoDB();

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);


        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
//                String ecForm = String.format(Locale.UK, "%.1f", phVal);
                minMV1.setText(String.valueOf(phVal));
                MIN_MV1 = minMV1.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("minMV1", MIN_MV1);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV2.setText(String.valueOf(phVal));
                MIN_MV2 = minMV2.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("minMV2", MIN_MV2);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV3.setText(String.valueOf(phVal));
                MIN_MV3 = minMV3.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("minMV3", MIN_MV3);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV4.setText(String.valueOf(phVal));
                MIN_MV4 = minMV4.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("minMV4", MIN_MV4);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                minMV5.setText(String.valueOf(phVal));
                MIN_MV5 = minMV5.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("minMV5", MIN_MV5);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV1.setText(String.valueOf(phVal));
                MAX_MV1 = maxMV1.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("maxMV1", MAX_MV1);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV2.setText(String.valueOf(phVal));
                MAX_MV2 = maxMV2.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("maxMV2", MAX_MV2);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV3.setText(String.valueOf(phVal));
                MAX_MV3 = maxMV3.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("maxMV3", MAX_MV3);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV4.setText(String.valueOf(phVal));
                MAX_MV4 = maxMV4.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("maxMV4", MAX_MV4);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float phVal = snapshot.getValue(Float.class);
                maxMV5.setText(String.valueOf(phVal));
                MAX_MV5 = maxMV5.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("maxMV5", MAX_MV5);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PhMvTable.this, "error : " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph1.setText(phVal);
                PH1 = ph1.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH1", PH1);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph2.setText(phVal);
                PH2 = ph2.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH2", PH2);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph3.setText(phVal);
                PH3 = ph3.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH3", PH3);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph4.setText(phVal);
                PH4 = ph4.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH4", PH4);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("UI").child("PH").child("PH_CAL").child("B_5").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String phVal = snapshot.getValue(String.class);
                ph5.setText(phVal);
                PH5 = ph5.getText().toString();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("PH5", PH5);
                myEdit.commit();
                updateDB();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("T_SET").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    deviceRef.child("Data").child("T_SET").setValue(0);
                } else {
                    Float phVal = snapshot.getValue(Float.class);
                    if (phVal != null) {
                        tempValue.setText(phVal.toString());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        setATC.setOnClickListener(v -> {
            if (!tempValue.getText().toString().equals("")) {
                Float va = Float.parseFloat(tempValue.getText().toString());

                if (Constants.OFFLINE_MODE) {
                    try {
                        jsonData.put("R_C", va);
                        jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                        webSocket1.send(jsonData.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    deviceRef.child("Data").child("T_SET").setValue(va);
                    databaseHelper.insert_action_data(time, date, "Temperature offset at: " + tempValue.getText() + " set by " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);
                }
            }
        });

    }

    private void insertIntoDB() {
        databaseHelper.insertPHBuffer(1, "-", "-", "-", PhMvTable.this);
        databaseHelper.insertPHBuffer(2, "-", "-", "-", PhMvTable.this);
        databaseHelper.insertPHBuffer(3, "-", "-", "-", PhMvTable.this);
        databaseHelper.insertPHBuffer(4, "-", "-", "-", PhMvTable.this);
        databaseHelper.insertPHBuffer(5, "-", "-", "-", PhMvTable.this);


    }

    private void updateDB() {
        databaseHelper.updateBufferData(1, ph1.getText().toString(), minMV1.getText().toString(), maxMV1.getText().toString(), PhMvTable.this);
        databaseHelper.updateBufferData(2, ph2.getText().toString(), minMV2.getText().toString(), maxMV2.getText().toString(), PhMvTable.this);
        databaseHelper.updateBufferData(3, ph3.getText().toString(), minMV3.getText().toString(), maxMV3.getText().toString(), PhMvTable.this);
        databaseHelper.updateBufferData(4, ph4.getText().toString(), minMV4.getText().toString(), maxMV4.getText().toString(), PhMvTable.this);
        databaseHelper.updateBufferData(5, ph5.getText().toString(), minMV5.getText().toString(), maxMV5.getText().toString(), PhMvTable.this);

    }

    private void insertToSQLDB() {

//        databaseHelper.delete_Buffer(PhMvTable.this);

//        databaseHelper.insertPHBuffer(1, ph1.getText().toString(), minMV1.getText().toString(), maxMV1.getText().toString(), PhMvTable.this);
//        databaseHelper.insertPHBuffer(2, ph2.getText().toString(), minMV2.getText().toString(), maxMV2.getText().toString(), PhMvTable.this);
//        databaseHelper.insertPHBuffer(3, ph3.getText().toString(), minMV3.getText().toString(), maxMV3.getText().toString(), PhMvTable.this);
//        databaseHelper.insertPHBuffer(4, ph4.getText().toString(), minMV4.getText().toString(), maxMV4.getText().toString(), PhMvTable.this);
//        databaseHelper.insertPHBuffer(5, ph5.getText().toString(), minMV5.getText().toString(), maxMV5.getText().toString(), PhMvTable.this);
    }

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.phEdit1:
                EditPhBufferDialog dialog = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("PH1", String.valueOf(ph));
//                    myEdit.commit();
//                    ph1.setText(sharedPreferences.getString("PH1", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_1").setValue(String.valueOf(ph));
                    insertToSQLDB();
                    databaseHelper.updateBufferData(1, String.valueOf(ph), minMV1.getText().toString(), maxMV1.getText().toString(), PhMvTable.this);
                });
                dialog.show(getSupportFragmentManager(), null);
                break;

            case R.id.phEdit2:

                EditPhBufferDialog dialog1 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("PH2", String.valueOf(ph));
//                    myEdit.commit();
//                    ph2.setText(sharedPreferences.getString("PH2", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_2").setValue(String.valueOf(ph));
                    insertToSQLDB();
                    databaseHelper.updateBufferData(2, String.valueOf(ph), minMV2.getText().toString(), maxMV2.getText().toString(), PhMvTable.this);
                });
                dialog1.show(getSupportFragmentManager(), null);
                break;
            case R.id.phEdit3:
                EditPhBufferDialog dialog2 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("PH3", String.valueOf(ph));
//                    myEdit.commit();
//                    ph3.setText(sharedPreferences.getString("PH3", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_3").setValue(String.valueOf(ph));
                    insertToSQLDB();
                    databaseHelper.updateBufferData(3, String.valueOf(ph), minMV3.getText().toString(), maxMV3.getText().toString(), PhMvTable.this);

                });
                insertToSQLDB();
                dialog2.show(getSupportFragmentManager(), null);
                break;
            case R.id.phEdit4:
                EditPhBufferDialog dialog3 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("PH4", String.valueOf(ph));
//                    myEdit.commit();
//                    ph4.setText(sharedPreferences.getString("PH4", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_4").setValue(String.valueOf(ph));
                    insertToSQLDB();
                    databaseHelper.updateBufferData(4, String.valueOf(ph), minMV4.getText().toString(), maxMV4.getText().toString(), PhMvTable.this);

                });
                dialog3.show(getSupportFragmentManager(), null);
                break;

            case R.id.phEdit5:
                EditPhBufferDialog dialog5 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("PH5", String.valueOf(ph));
//                    myEdit.commit();
//                    ph5.setText(sharedPreferences.getString("PH5", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("B_5").setValue(String.valueOf(ph));
                    insertToSQLDB();
                    databaseHelper.updateBufferData(5, String.valueOf(ph), minMV5.getText().toString(), maxMV5.getText().toString(), PhMvTable.this);

                });
                dialog5.show(getSupportFragmentManager(), null);
                break;

            case R.id.minMVEdit1:
                EditPhBufferDialog dialog6 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("minMV1", String.valueOf(ph));
//                    myEdit.commit();
//                    minMV1.setText(sharedPreferences.getString("minMV1", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV1").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(1, ph1.getText().toString(), String.valueOf(ph), maxMV1.getText().toString(), PhMvTable.this);


                });
                dialog6.show(getSupportFragmentManager(), null);
                break;
            case R.id.minMVEdit2:
                EditPhBufferDialog dialog7 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("minMV2", String.valueOf(ph));
//                    myEdit.commit();
//                    minMV2.setText(sharedPreferences.getString("minMV2", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV2").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(2, ph2.getText().toString(), String.valueOf(ph), maxMV2.getText().toString(), PhMvTable.this);

                });
                dialog7.show(getSupportFragmentManager(), null);
                break;
            case R.id.minMVEdit3:
                EditPhBufferDialog dialog8 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("minMV3", String.valueOf(ph));
//                    myEdit.commit();
//                    minMV3.setText(sharedPreferences.getString("minMV3", ""));
//                    Toast.makeText(this, ph + "", Toast.LENGTH_SHORT).show();
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV3").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(3, ph3.getText().toString(), String.valueOf(ph), maxMV3.getText().toString(), PhMvTable.this);

                });
                dialog8.show(getSupportFragmentManager(), null);
                break;
            case R.id.minMVEdit4:
                EditPhBufferDialog dialog9 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("minMV4", String.valueOf(ph));
//                    myEdit.commit();
//                    minMV4.setText(sharedPreferences.getString("minMV4", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV4").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(4, ph4.getText().toString(), String.valueOf(ph), maxMV4.getText().toString(), PhMvTable.this);

                });
                dialog9.show(getSupportFragmentManager(), null);
                break;
            case R.id.minMVEdit5:
                EditPhBufferDialog dialog10 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("minMV5", String.valueOf(ph));
//                    myEdit.commit();
//                    minMV5.setText(sharedPreferences.getString("minMV5", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("minMV5").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(5, ph5.getText().toString(), String.valueOf(ph), maxMV5.getText().toString(), PhMvTable.this);

                });
                dialog10.show(getSupportFragmentManager(), null);
                break;

            case R.id.maxMVEdit1:
                EditPhBufferDialog dialog11 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("maxMV1", String.valueOf(ph));
//                    myEdit.commit();
//
//                    maxMV1.setText(sharedPreferences.getString("maxMV1", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV1").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(1, ph1.getText().toString(), minMV1.getText().toString(), String.valueOf(ph), PhMvTable.this);


                });
                dialog11.show(getSupportFragmentManager(), null);
                break;
            case R.id.maxMVEdit2:
                EditPhBufferDialog dialog12 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("maxMV2", String.valueOf(ph));
//                    myEdit.commit();
//
//                    maxMV2.setText(sharedPreferences.getString("maxMV2", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV2").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(2, ph2.getText().toString(), minMV2.getText().toString(), String.valueOf(ph), PhMvTable.this);

                });
                dialog12.show(getSupportFragmentManager(), null);
                break;
            case R.id.maxMVEdit3:
                EditPhBufferDialog dialog13 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("maxMV3", String.valueOf(ph));
//                    myEdit.commit();
//
//                    maxMV3.setText(sharedPreferences.getString("maxMV3", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV3").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(3, ph3.getText().toString(), minMV3.getText().toString(), String.valueOf(ph), PhMvTable.this);

                });
                dialog13.show(getSupportFragmentManager(), null);
                break;
            case R.id.maxMVEdit4:
                EditPhBufferDialog dialog14 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("maxMV4", String.valueOf(ph));
//                    myEdit.commit();
//
//                    maxMV4.setText(sharedPreferences.getString("maxMV4", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV4").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(4, ph4.getText().toString(), minMV4.getText().toString(), String.valueOf(ph), PhMvTable.this);

                });
                dialog14.show(getSupportFragmentManager(), null);
                break;
            case R.id.maxMVEdit5:
                EditPhBufferDialog dialog15 = new EditPhBufferDialog(ph -> {
//                    myEdit.putString("maxMV5", String.valueOf(ph));
//                    myEdit.commit();
//
//                    maxMV5.setText(sharedPreferences.getString("maxMV5", ""));
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("maxMV5").setValue(ph);
                    insertToSQLDB();
                    databaseHelper.updateBufferData(5, ph5.getText().toString(), minMV5.getText().toString(), String.valueOf(ph), PhMvTable.this);

                });
                dialog15.show(getSupportFragmentManager(), null);
                break;

            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Constants.OFFLINE_MODE) {
            initiateSocketConnection();
        }
    }

    @Override
    public void onStop() {

//        deviceRef.child("Data").child("AUTOLOG").setValue(0);
//        deviceRef.child("Data").child("LOG_INTERVAL").setValue(0);
        if (Constants.OFFLINE_MODE) {
            webSocket1.cancel();
        }
        super.onStop();


    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket1 = client.newWebSocket(request, new SocketListener());
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            runOnUiThread(() -> {
                Toast.makeText(PhMvTable.this,
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();

                try {
                    jsonData = new JSONObject();
                    jsonData.put("SOCKET_INIT", "Successfully Initialized on phLogFragment");
                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                    webSocket.send(jsonData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(PhMvTable.this,
                                "Socket Connection Unsuccessful!",
                                Toast.LENGTH_SHORT).show();

                    });
                }

            });

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            runOnUiThread(() -> {
                try {
                    jsonData = new JSONObject(text);
                    Log.d("JSONReceived:PHFragment", "onMessage: " + text);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }

    }


}