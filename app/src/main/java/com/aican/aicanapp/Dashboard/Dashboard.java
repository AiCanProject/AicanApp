package com.aican.aicanapp.Dashboard;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
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
import com.aican.aicanapp.DownloadHandler.DownloadHandler;
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
import com.aican.aicanapp.specificactivities.AvailableWifiDevices;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
    File file;
    File fileDestination;
    DatabaseReference primaryDatabase;
    DatabaseReference databaseReference;
    String mUid;
    Button setting, export;
    private TextView internetStatus, locationD, weather, batteryPercentage;

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
        Source.expiryDate_fetched = new ArrayList<>();
        Source.dateCreated_fetched = new ArrayList<>();

        phDev = findViewById(R.id.ph_dev);
        IndusDev = findViewById(R.id.indusPh_dev);
        peristalticDev = findViewById(R.id.peristaltic_dev);
        tempDev = findViewById(R.id.temp_dev);
        tvInstruction = findViewById(R.id.tvInstruction);

        batteryPercentage = findViewById(R.id.batteryPercent);
        internetStatus = findViewById(R.id.internetStatus);
        locationD = findViewById(R.id.locationText);
        weather = findViewById(R.id.weather);

        addNewDevice = findViewById(R.id.add_new_device);
        tempRecyclerView = findViewById(R.id.temp_recyclerview);
        coolingRecyclerView = findViewById(R.id.cooling_recyclerview);
        phRecyclerView = findViewById(R.id.ph_recyclerview);
        pumpRecyclerView = findViewById(R.id.pump_recyclerview);
        tvName = findViewById(R.id.tvName);
        ivLogout = findViewById(R.id.ivLogout);
        tvConnectDevice = findViewById(R.id.tvConnectDevice);
        setting = findViewById(R.id.settings);
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

        // subscription checking
        subscriptionChecker();

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
                } else {
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


        NewAsyncTask newAsyncTask = new NewAsyncTask(this);
        newAsyncTask.execute(Dashboard.this);

        file = new File(getExternalFilesDir(null) + "/" + getString(R.string.folderLocation));

        fileDestination = new File(Environment.getExternalStorageDirectory(), "/" + getString(R.string.folderLocation));

        if (!file.exists()) {
            file.mkdirs();
        }

        CheckForUpdate();

        tempDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showNetworkDialog();


                if (tempDevices.size() != 0) {
                    tempRecyclerView.setVisibility(View.VISIBLE);
                } else {
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
                if (pumpDevices.size() != 0) {
                    pumpRecyclerView.setVisibility(View.VISIBLE);
                } else {
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
                if (coolingDevices.size() != 0) {
                    coolingRecyclerView.setVisibility(View.VISIBLE);
                } else {
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

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, AdminLoginActivity.class);
                intent.putExtra("checkBtn", "addUser");
                startActivity(intent);
            }
        });

        ivLogout.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, AdminLoginActivity.class);
            intent.putExtra("checkBtn", "logout");
            startActivity(intent);
        });

        tvConnectDevice.setOnClickListener(v -> {
            startActivity(new Intent(this, AvailableWifiDevices.class));
        });

        // battery percentage
        BatteryManager bm = (BatteryManager) getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int tabBatteryPer = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            batteryPercentage.setText(tabBatteryPer + "%");
        }

        setUpNavDrawer();
        setUpToolBar();
        setUpTemp();
        setUpCooling();
        setUpPh();
        setUpPump();
    }

/*    private void showNetworkDialog(){
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
    }*/

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
            Source.expiryDate_fetched.add(res.getString(res.getColumnIndex("expiryDate")));
            Source.dateCreated_fetched.add(res.getString(res.getColumnIndex("dateCreated")));
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
        NewAsyncTask newAsyncTask = new NewAsyncTask(this);
        newAsyncTask.execute(Dashboard.this);
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
        } else {
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
        NewAsyncTask newAsyncTask = new NewAsyncTask(this);
        newAsyncTask.execute(Dashboard.this);
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
/*                        if (mode == 0) {
                            pumpDevices.add(new PumpDevice(
                                    id,
                                    name,
                                    mode,
                                    ui.child("Speed").getValue(Integer.class),
                                    ui.child("Direction").getValue(Integer.class),
                                    ui.child("Volume").getValue(Integer.class),
                                    status

                            ));
                        } else {
                            pumpDevices.add(new PumpDevice(
                                    id,
                                    name,
                                    mode,
                                    ui.child("MODE").child("PUMP").child("SPEED").getValue(Integer.class),
                                    ui.child("MODE").child("PUMP").child("DIR").getValue(Integer.class),
                                    null,
                                    status
                            ));
                        }*/

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
                                data.child("TEMP2_VAL").getValue(Integer.class)
                        ));
                        break;
                    }
                    case "PELTIER": {
                        coolingDevices.add(new CoolingDevice(
                                id,
                                name,
                                (Integer) ui.child("TEMP").child("TEMP_VAL").getValue()
                        ));
                        break;
                    }
                }
                if (devicesLoaded.get() == deviceIds.size()) {
                    tempAdapter.notifyDataSetChanged();
                    coolingAdapter.notifyDataSetChanged();
                    phAdapter.notifyDataSetChanged();
                    pumpAdapter.notifyDataSetChanged();
                 /*
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

                    */

                    if (phDevices.size() != 0) {
                        phRecyclerView.setVisibility(View.VISIBLE);
                        phDev.setCardBackgroundColor(Color.GRAY);
                    } else {
                        phRecyclerView.setVisibility(View.GONE);
                    }
                    tempRecyclerView.setVisibility(View.GONE);
                    coolingRecyclerView.setVisibility(View.GONE);
                    pumpRecyclerView.setVisibility(View.GONE);
//                    ecRecyclerView.setVisibility(View.GONE);

                    phDev.setCardBackgroundColor(Color.GRAY);
                    tempDev.setCardBackgroundColor(Color.WHITE);
                    peristalticDev.setCardBackgroundColor(Color.WHITE);
                    IndusDev.setCardBackgroundColor(Color.WHITE);
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

    private void subscriptionChecker() {

        String uid = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).getUid();
        if (uid == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(Dashboard.this)).getReference().child("USERS").child(uid).child("subscription");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    DatabaseReference subscription = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(Dashboard.this)).getReference().child("USERS").child(uid);
                    subscription.child("subscription").setValue("na");

                    subscription.child("subscription").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {

//                            Toast.makeText(Dashboard.this, "Subscription : " + snapshot1.getValue(), Toast.LENGTH_SHORT).show();
                            Dialog dialog = new Dialog(Dashboard.this);
                            dialog.setContentView(R.layout.no_subscription);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.setCancelable(false);
                            dialog.findViewById(R.id.contactWith).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(Dashboard.this, "Contact", Toast.LENGTH_SHORT).show();
                                }
                            });
                            dialog.show();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Dashboard.this, "Failed " + error, Toast.LENGTH_SHORT).show();


                        }
                    });

                } else {
//                    Toast.makeText(Dashboard.this, " Subscribed " + snapshot.getValue(), Toast.LENGTH_SHORT).show();
                    DatabaseReference subscription = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(Dashboard.this)).getReference().child("USERS").child(uid);

                    subscription.child("subscription").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
//                            Toast.makeText(Dashboard.this, "Subscription : " + snapshot1.getValue(), Toast.LENGTH_SHORT).show();
                            if (Objects.equals(snapshot1.getValue(), "na")) {
                                Dialog dialog = new Dialog(Dashboard.this);
                                dialog.setContentView(R.layout.no_subscription);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.setCancelable(false);
                                dialog.findViewById(R.id.contactWith).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        finishAffinity();
                                    }
                                });
                                dialog.show();
                            }
                            if (Objects.equals(snapshot1.getValue(), "non cfr") || Objects.equals(snapshot1.getValue(), "non_cfr")
                                    || Objects.equals(snapshot1.getValue(), "nonCfr") || Objects.equals(snapshot1.getValue(), "noncfr")
                                    || Objects.equals(snapshot1.getValue(), "Non Cfr") || Objects.equals(snapshot1.getValue(), "Non cfr")) {
                                Source.subscription = "nonCfr";
                                setting.setVisibility(View.GONE);
                            }
                            if (Objects.equals(snapshot1.getValue(), "cfr")) {
                                Source.subscription = "cfr";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Dashboard.this, "Failed " + error, Toast.LENGTH_SHORT).show();


                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void CheckForUpdate() {
        try {
            String version = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            databaseReference = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference().child("version").child("v1");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String versionName = (String) dataSnapshot.getValue();

                    if (versionName != null && !versionName.equals(version)) {

                        Dialog dialog = new Dialog(Dashboard.this);
                        dialog.setContentView(R.layout.update_ui);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                runtimeStoragePermission();

                            }
                        });
                        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                Toast.makeText(Dashboard.this, "Please update your app as soon as possible, you are loosing lots of thing without this update", Toast.LENGTH_LONG).show();
                            }
                        });
                        dialog.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runtimeStoragePermission() {
        Dexter.withContext(Dashboard.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!getPackageManager().canRequestPackageInstalls()) {
                                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234);
                            } else {
                                Toast.makeText(Dashboard.this, "Allow the permission to install new update", Toast.LENGTH_SHORT).show();
                            }
                        }

                        databaseReference = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(Dashboard.this)).getReference().child("version").child("latestApkLink");
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String apkUrl = (String) dataSnapshot.getValue();
                                DownloadHandler downloadHandler = new DownloadHandler();
                                downloadHandler.downloadFile(apkUrl, "labApp.apk", Dashboard.this, file);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "There was an error : " + error.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("Dexter", "There was an error: " + error.toString());
                    }
                }).check();
    }



    private class NewAsyncTask extends AsyncTask<Context, Void, Void> {
        private WeakReference<Dashboard> dashboardWeakReference;

        NewAsyncTask(Dashboard dashboard) {
            dashboardWeakReference = new WeakReference<Dashboard>(dashboard);
        }

        private boolean isNetworkAvailable2(Context context) {
            ConnectivityManager manager =
                    (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            boolean isAvailable = false;
            if (networkInfo != null && networkInfo.isConnected()) {
                // Network is present and connected
                isAvailable = true;
            }
            return isAvailable;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Dashboard dashboard = dashboardWeakReference.get();
            if (dashboard == null || dashboard.isFinishing()) {
                return;
            }
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            Dashboard dashboard = dashboardWeakReference.get();
            if (dashboard == null || dashboard.isFinishing()) {
                return null;
            }

            if (isNetworkAvailable2(contexts[0])) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                    urlc.setRequestProperty("User-Agent", "Test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();
                    isConnected = (urlc.getResponseCode() == 200);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isConnected) {
                                dashboard.internetStatus.setText("Active");
                                dashboard.internetStatus.setTextColor(getResources().getColor(R.color.internetActive));
                            } else {
                                dashboard.internetStatus.setText("Inactive");
                                dashboard.internetStatus.setTextColor(getResources().getColor(R.color.internetInactive));
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e("LOG_TAG", "Error: ", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dashboard.internetStatus.setText("Inactive");
                            dashboard.internetStatus.setTextColor(getResources().getColor(R.color.internetInactive));
                        }
                    });
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dashboard.internetStatus.setText("Inactive");
                        dashboard.internetStatus.setTextColor(getResources().getColor(R.color.internetInactive));
                    }
                });
                Log.d("LOG_TAG", "No network present");
            }

            return null;
        }


    }

    public static boolean isConnected;
}