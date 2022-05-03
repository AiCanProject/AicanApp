package com.aican.aicanapp.Dashboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.AddDevice.AddDeviceOption;
import com.aican.aicanapp.Authentication.LoginActivity;
import com.aican.aicanapp.FirebaseAccounts.DeviceAccount;
import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.FirebaseAccounts.SecondaryAccount;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.CoolingAdapter;
import com.aican.aicanapp.adapters.PhAdapter;
import com.aican.aicanapp.adapters.PumpAdapter;
import com.aican.aicanapp.adapters.TempAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dataClasses.CoolingDevice;
import com.aican.aicanapp.dataClasses.PhDevice;
import com.aican.aicanapp.dataClasses.PumpDevice;
import com.aican.aicanapp.dataClasses.TempDevice;
import com.aican.aicanapp.dialogs.EditNameDialog;
import com.aican.aicanapp.specificactivities.ConnectDeviceActivity;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.InstructionActivity;
import com.aican.aicanapp.utils.DashboardListsOptionsClickListener;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.LogDescriptor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Dashboard extends AppCompatActivity implements DashboardListsOptionsClickListener, EditNameDialog.OnNameChangedListener {

    public static final String TAG = "Dashboard";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final int GRAPH_PLOT_DELAY = 15000;
    public static final String DEVICE_TYPE_PH = "PHMETER";
    public static final String DEVICE_TYPE_PUMP = "P_PUMP";
    public static final String DEVICE_TYPE_TEMP = "TEMP_CONTROLLER";
    public static final String DEVICE_TYPE_COOLING = "PELTIER";

    CardView phDev, tempDev, IndusDev, peristalticDev;

    DatabaseReference primaryDatabase;
    String mUid;
    Button setting, export;

    ArrayList<String> deviceIds;
    HashMap<String, String> deviceIdIds;
    HashMap<String, String> deviceTypes;
    HashMap<String, String> deviceNames;

    ArrayList<PhDevice> phDevices;
    ArrayList<PumpDevice> pumpDevices;
    ArrayList<TempDevice> tempDevices;
    ArrayList<CoolingDevice> coolingDevices;

    TempAdapter tempAdapter;
    CoolingAdapter coolingAdapter;
    PhAdapter phAdapter;
    PumpAdapter pumpAdapter;

    private DatabaseHelper databaseHelper;

    private DrawerLayout drawerLayout;

    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private RecyclerView tempRecyclerView, coolingRecyclerView, phRecyclerView, pumpRecyclerView;
    private FloatingActionButton addNewDevice;
    private TextView tvTemp, tvCooling, tvPump, tvPh, tvName, tvConnectDevice, tvInstruction;
    private ImageView ivLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);
        Source.id_fetched = new ArrayList<>();
        Source.passcode_fetched = new ArrayList<>();
        Source.role_fetched = new ArrayList<>();
        Source.name_fetched = new ArrayList<>();

        phDev = findViewById(R.id.ph_dev);
        IndusDev = findViewById(R.id.indusPh_dev);
        peristalticDev = findViewById(R.id.peristaltic_dev);
        tempDev = findViewById(R.id.temp_dev);
        tvInstruction = findViewById(R.id.tvInstruction);

        addNewDevice = findViewById(R.id.add_new_device);
        tempRecyclerView = findViewById(R.id.temp_recyclerview);
        coolingRecyclerView = findViewById(R.id.cooling_recyclerview);
        phRecyclerView = findViewById(R.id.ph_recyclerview);
        pumpRecyclerView = findViewById(R.id.pump_recyclerview);
        tvName = findViewById(R.id.tvName);
        ivLogout = findViewById(R.id.ivLogout);
        tvConnectDevice = findViewById(R.id.tvConnectDevice);
//        setting = findViewById(R.id.settings);
        mUid = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).getUid();
        primaryDatabase = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference().child("USERS").child(mUid);
        deviceIds = new ArrayList<>();
        phDevices = new ArrayList<>();
        pumpDevices = new ArrayList<>();
        coolingDevices = new ArrayList<>();
        tempDevices = new ArrayList<>();
        deviceTypes = new HashMap<>();
        deviceIdIds = new HashMap<>();
        deviceNames = new HashMap<>();

        //showNetworkDialog();

        phRecyclerView.setVisibility(View.VISIBLE);
        phDev.setCardBackgroundColor(Color.GRAY);
        tempRecyclerView.setVisibility(View.GONE);
        coolingRecyclerView.setVisibility(View.GONE);
        pumpRecyclerView.setVisibility(View.GONE);

        tvInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          //      showNetworkDialog();
                Intent intent = new Intent(Dashboard.this, InstructionActivity.class);
                startActivity(intent);
            }
        });
        phDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    showNetworkDialog();
                if (phDevices.size() != 0) {
                    phRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    phRecyclerView.setVisibility(View.GONE);
                }
                tempRecyclerView.setVisibility(View.GONE);
                coolingRecyclerView.setVisibility(View.GONE);
                pumpRecyclerView.setVisibility(View.GONE);

                phDev.setCardBackgroundColor(Color.GRAY);
                tempDev.setCardBackgroundColor(Color.WHITE);
                peristalticDev.setCardBackgroundColor(Color.WHITE);
                IndusDev.setCardBackgroundColor(Color.WHITE);
            }
        });

        tempDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showNetworkDialog();


                if (tempDevices.size() != 0) {
                    tempRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    tempRecyclerView.setVisibility(View.GONE);
                }
                phRecyclerView.setVisibility(View.GONE);
                coolingRecyclerView.setVisibility(View.GONE);
                pumpRecyclerView.setVisibility(View.GONE);


                tempDev.setCardBackgroundColor(Color.GRAY);
                phDev.setCardBackgroundColor(Color.WHITE);
                peristalticDev.setCardBackgroundColor(Color.WHITE);
                IndusDev.setCardBackgroundColor(Color.WHITE);


            }
        });

        peristalticDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showNetworkDialog();
                if (pumpDevices.size() != 0) {
                    pumpRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    pumpRecyclerView.setVisibility(View.GONE);
                }
                tempRecyclerView.setVisibility(View.GONE);
                coolingRecyclerView.setVisibility(View.GONE);
                phRecyclerView.setVisibility(View.GONE);


                peristalticDev.setCardBackgroundColor(Color.GRAY);
                tempDev.setCardBackgroundColor(Color.WHITE);
                phDev.setCardBackgroundColor(Color.WHITE);
                IndusDev.setCardBackgroundColor(Color.WHITE);

            }
        });

        IndusDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showNetworkDialog();
                if (coolingDevices.size() != 0) {
                    coolingRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    coolingRecyclerView.setVisibility(View.GONE);
                }

                phRecyclerView.setVisibility(View.GONE);
                tempRecyclerView.setVisibility(View.GONE);
                pumpRecyclerView.setVisibility(View.GONE);



                IndusDev.setCardBackgroundColor(Color.GRAY);
                tempDev.setCardBackgroundColor(Color.WHITE);
                peristalticDev.setCardBackgroundColor(Color.WHITE);
                phDev.setCardBackgroundColor(Color.WHITE);

            }
        });

        addNewDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddDevice = new Intent(Dashboard.this, AddDeviceOption.class);
                startActivity(toAddDevice);
            }
        });

//        setting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Dashboard.this, AdminLoginActivity.class);
//                intent.putExtra("checkBtn","addUser");
//                startActivity(intent);
//
//            }
//        });

        ivLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance(PrimaryAccount.getInstance(getApplicationContext())).signOut();
            finish();

            Intent intent  = new Intent(Dashboard.this, LoginActivity.class);
            startActivity(intent);
        });

        tvConnectDevice.setOnClickListener(v -> {
            startActivity(new Intent(this, ConnectDeviceActivity.class));
        });

        setUpNavDrawer();
        setUpToolBar();
        setUpTemp();
        setUpCooling();
        setUpPh();
        setUpPump();
    }

    private void showNetworkDialog(){
        if(!isNetworkAvailable()==true)
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Internet Connection Alert")
                    .setMessage("Please Check Your Internet Connection")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
        }
        else if(isNetworkAvailable()==true)
        {
            Toast.makeText(Dashboard.this,
                    "Welcome", Toast.LENGTH_LONG).show();
        }
    }

    private void getList() {
        Cursor res = databaseHelper.get_data();
        if (res.getCount() == 0) {
            Toast.makeText(Dashboard.this, "No entry", Toast.LENGTH_SHORT).show();
        }
        while (res.moveToNext()) {
            Source.id_fetched.add(res.getString(res.getColumnIndex("id")));
            Source.passcode_fetched.add(res.getString(res.getColumnIndex("passcode")));
            Source.role_fetched.add(res.getString(res.getColumnIndex("role")));
            Source.name_fetched.add(res.getString(res.getColumnIndex("name")));
        }
    }

    private void setUpNavDrawer() {
        String uid = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).getUid();
        FirebaseFirestore.getInstance(PrimaryAccount.getInstance(this))
                .collection("NAMES").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    tvName.setText(documentSnapshot.get("NAME", String.class));
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {

        if (phDevices.size() != 0) {
            phRecyclerView.setVisibility(View.VISIBLE);
            phDev.setCardBackgroundColor(Color.GRAY);
        }else {
            phRecyclerView.setVisibility(View.GONE);
        }
        tempRecyclerView.setVisibility(View.GONE);
        coolingRecyclerView.setVisibility(View.GONE);
        pumpRecyclerView.setVisibility(View.GONE);

        getList();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    //Toolbar------------------------------------------------------------------------------------------------------
    public void setUpToolBar() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
    //Toolbar------------------------------------------------------------------------------------------------------

    //Temperature RC------------------------------------------------------------------------------------------------------
    public void setUpTemp() {
        tempRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tempAdapter = new TempAdapter(tempDevices, this::onOptionsIconClicked);
        tempRecyclerView.setAdapter(tempAdapter);
    }
    //Temperature RC------------------------------------------------------------------------------------------------------

    //Cooling RC------------------------------------------------------------------------------------------------------
    public void setUpCooling() {
        coolingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        coolingAdapter = new CoolingAdapter(coolingDevices, this::onOptionsIconClicked);
        coolingRecyclerView.setAdapter(coolingAdapter);
    }
    //Cooling RC------------------------------------------------------------------------------------------------------

    //Ph RC------------------------------------------------------------------------------------------------------
    public void setUpPh() {
        phRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        phAdapter = new PhAdapter(phDevices, this::onOptionsIconClicked);
        phRecyclerView.setAdapter(phAdapter);
    }
    //Ph RC------------------------------------------------------------------------------------------------------

    //Pump RC------------------------------------------------------------------------------------------------------
    public void setUpPump() {
        pumpRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pumpAdapter = new PumpAdapter(pumpDevices, this::onOptionsIconClicked);
        pumpRecyclerView.setAdapter(pumpAdapter);
    }
    //Pump RC------------------------------------------------------------------------------------------------------

    private void refresh() {
        deviceIds.clear();
        phDevices.clear();
        coolingDevices.clear();
        tempDevices.clear();
        pumpDevices.clear();
        deviceIdIds.clear();
        getDeviceIds();
    }

    /**
     * Connect to devices account and get device details
     */
    private void getDeviceIds() {
        primaryDatabase.child("DEVICES").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                    String deviceId = deviceSnapshot.getValue(String.class);
                    deviceIds.add(deviceId);
                    deviceIdIds.put(deviceId, deviceSnapshot.getKey());
                }
                getDeviceAccounts();
            }
        });
    }

    private void getDeviceAccounts() {
        AtomicInteger accountsLoaded = new AtomicInteger();
        DatabaseReference secondaryDatabase = FirebaseDatabase.getInstance(SecondaryAccount.getInstance(this)).getReference();
        for (String id : deviceIds) {
            secondaryDatabase.child(id).get().addOnSuccessListener(dataSnapshot -> {
                accountsLoaded.incrementAndGet();

                DeviceAccount deviceAccount = dataSnapshot.getValue(DeviceAccount.class);
                if (deviceAccount == null) return;
                deviceTypes.put(id, deviceAccount.type);
                initialiseFirebaseForDevice(id, deviceAccount);

                if (accountsLoaded.get() == deviceIds.size()) {
                    getDevices();
                }
            }).addOnFailureListener(exception -> {
                exception.printStackTrace();
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.d("TAG", "onCanceled: ");
                }
            });
        }

    }

    private void initialiseFirebaseForDevice(String deviceId, DeviceAccount deviceAccount) {
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setApiKey(deviceAccount.api)
                .setApplicationId(deviceAccount.app)
                .setDatabaseUrl(deviceAccount.database)
                .setProjectId(deviceAccount.project)
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(this, firebaseOptions, deviceId);
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);
        } catch (IllegalStateException e) {
            //Ignore
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void getDevices() {
        AtomicInteger devicesLoaded = new AtomicInteger();
        for (String id : deviceIds) {
            FirebaseApp app = FirebaseApp.getInstance(id);

            FirebaseDatabase.getInstance(app).getReference().child(deviceTypes.get(id)).child(id).get().addOnSuccessListener(dataSnapshot -> {
                devicesLoaded.incrementAndGet();
                DataSnapshot data = dataSnapshot.child("Data");
                DataSnapshot ui = dataSnapshot.child("UI");
                String name = dataSnapshot.child("NAME").getValue(String.class);
                deviceNames.put(id, name);
                switch (deviceTypes.get(id)) {
                    case "PHMETER": {
                        PhDevice device = new PhDevice(
                                id,
                                name,
                                data.child("PH_VAL").getValue(Float.class),
                                data.child("EC_VAL").getValue(Float.class),
                                data.child("TEMP_VAL").getValue(Integer.class),
                                data.child("TDS_VAL").getValue(Long.class)
                        );
                        phDevices.add(device);
//                        setPhDeviceListeners(device, phDevices.size()-1);
                        break;
                    }
                    case "P_PUMP": {
                        Integer mode = ui.child("Mode").getValue(Integer.class);
                        Integer status = ui.child("Start").getValue(Integer.class);
//                        if (mode == 0) {
//                            pumpDevices.add(new PumpDevice(
//                                    id,
//                                    name,
//                                    mode,
//                                    ui.child("Speed").getValue(Integer.class),
//                                    ui.child("Direction").getValue(Integer.class),
//                                    ui.child("Volume").getValue(Integer.class),
//                                    status
//
//                            ));
//                        } else {
//                            pumpDevices.add(new PumpDevice(
//                                    id,
//                                    name,
//                                    mode,
//                                    ui.child("MODE").child("PUMP").child("SPEED").getValue(Integer.class),
//                                    ui.child("MODE").child("PUMP").child("DIR").getValue(Integer.class),
//                                    null,
//                                    status
//                            ));
//                        }

                        pumpDevices.add(new PumpDevice(
                                id,
                                name,
                                mode,
                                ui.child("Speed").getValue(Integer.class),
                                ui.child("Direction").getValue(Integer.class),
                                ui.child("Volume").getValue(Integer.class),
                                status

                        ));

                        break;
                    }
                    case "TEMP_CONTROLLER": {
                        tempDevices.add(new TempDevice(
                                id,
                                name,
                                data.child("TEMP1_VAL").getValue(Integer.class)
                        ));
                        break;
                    }
                    case "PELTIER": {
                        coolingDevices.add(new CoolingDevice(
                                id,
                                name,
                                ui.child("TEMP").child("TEMP_VAL").getValue(Integer.class)
                        ));
                        break;
                    }
                }
                if (devicesLoaded.get() == deviceIds.size()) {
                    tempAdapter.notifyDataSetChanged();
                    coolingAdapter.notifyDataSetChanged();
                    phAdapter.notifyDataSetChanged();
                    pumpAdapter.notifyDataSetChanged();
                    if (tempDevices.size() == 0) {
                        tempRecyclerView.setVisibility(View.GONE);
//                        tvTemp.setVisibility(View.GONE);
                    } else {
                        tempRecyclerView.setVisibility(View.VISIBLE);
  //                      tvTemp.setVisibility(View.VISIBLE);
                    }
                    if (coolingDevices.size() == 0) {
                        coolingRecyclerView.setVisibility(View.GONE);
    //                    tvCooling.setVisibility(View.GONE);
                    } else {
                        coolingRecyclerView.setVisibility(View.VISIBLE);
      //                  tvCooling.setVisibility(View.VISIBLE);
                    }
                    if (phDevices.size() == 0) {
                        phRecyclerView.setVisibility(View.GONE);
        //                tvPh.setVisibility(View.GONE);
                    } else {
                        phRecyclerView.setVisibility(View.VISIBLE);
          //              tvPh.setVisibility(View.VISIBLE);
                    }
                    if (pumpDevices.size() == 0) {
                        pumpRecyclerView.setVisibility(View.GONE);
            //            tvPump.setVisibility(View.GONE);
                    } else {
                        pumpRecyclerView.setVisibility(View.VISIBLE);
              //          tvPump.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    public boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {

                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onOptionsIconClicked(View view, String deviceId) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.device_options, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.menuRemoveDevice) {
                String uid = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).getUid();
                if (uid != null && deviceIdIds.containsKey(deviceId)) {
                    FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference()
                            .child("USERS").child(uid).child("DEVICES").child(deviceIdIds.get(deviceId)).removeValue()
                            .addOnSuccessListener(d -> {
                                FirebaseFirestore.getInstance().collection("Devices Registered").document(deviceId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        refresh();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                            });
                }
                return true;
            } else if (item.getItemId() == R.id.menuRename) {
                EditNameDialog dialog = new EditNameDialog(
                        deviceId,
                        deviceTypes.get(deviceId),
                        deviceNames.get(deviceId),
                        this
                );
                dialog.show(getSupportFragmentManager(), null);
            }

            return false;
        });
        menu.show();
    }

    @Override
    public void onNameChanged(String deviceId, String type, String newName) {
        FirebaseDatabase.getInstance(FirebaseApp.getInstance(deviceId)).getReference()
                .child(type).child(deviceId).child("NAME").setValue(newName);
        refresh();
    }
}