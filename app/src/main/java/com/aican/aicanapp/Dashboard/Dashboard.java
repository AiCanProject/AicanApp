package com.aican.aicanapp.Dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.AddDevice.AddDeviceOption;
import com.aican.aicanapp.Authentication.LoginActivity;
import com.aican.aicanapp.FirebaseAccounts.DeviceAccount;
import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.FirebaseAccounts.SecondaryAccount;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.CoolingAdapter;
import com.aican.aicanapp.adapters.PhAdapter;
import com.aican.aicanapp.adapters.PumpAdapter;
import com.aican.aicanapp.adapters.TempAdapter;
import com.aican.aicanapp.dataClasses.CoolingDevice;
import com.aican.aicanapp.dataClasses.PhDevice;
import com.aican.aicanapp.dataClasses.PumpDevice;
import com.aican.aicanapp.dataClasses.TempDevice;
import com.aican.aicanapp.dialogs.EditNameDialog;
import com.aican.aicanapp.specificactivities.ConnectDeviceActivity;
import com.aican.aicanapp.utils.DashboardListsOptionsClickListener;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    public static final String KEY_DEVICE_ID = "device_id";
    public static final int GRAPH_PLOT_DELAY = 15000;
    public static final String DEVICE_TYPE_PH = "PHMETER";
    public static final String DEVICE_TYPE_PUMP = "P_PUMP";
    public static final String DEVICE_TYPE_TEMP = "TEMP_CONTROLLER";
    public static final String DEVICE_TYPE_COOLING = "PELTIER";

    DatabaseReference primaryDatabase;
    String mUid;

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

    private DrawerLayout drawerLayout;
    //Recyclerviews-------------------------------------------------------------------
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    //---------------------------------------------------------------------------------
    private RecyclerView tempRecyclerView, coolingRecyclerView, phRecyclerView, pumpRecyclerView;
    private FloatingActionButton addNewDevice;
    private TextView tvTemp,tvCooling, tvPump, tvPh, tvName, tvConnectDevice;
    private ImageView ivLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        addNewDevice = findViewById(R.id.add_new_device);
        tempRecyclerView = findViewById(R.id.temp_recyclerview);
        coolingRecyclerView = findViewById(R.id.cooling_recyclerview);
        phRecyclerView = findViewById(R.id.ph_recyclerview);
        pumpRecyclerView = findViewById(R.id.pump_recyclerview);
        tvTemp = findViewById(R.id.tvTemp);
        tvCooling = findViewById(R.id.tvCooling);
        tvPh = findViewById(R.id.tvPh);
        tvPump = findViewById(R.id.tvPump);
        tvName = findViewById(R.id.tvName);
        ivLogout = findViewById(R.id.ivLogout);
        tvConnectDevice = findViewById(R.id.tvConnectDevice);
        

        mUid = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).getUid();
        primaryDatabase = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference()
                .child("USERS").child(mUid);
        deviceIds = new ArrayList<>();
        phDevices = new ArrayList<>();
        pumpDevices = new ArrayList<>();
        coolingDevices = new ArrayList<>();
        tempDevices = new ArrayList<>();
        deviceTypes = new HashMap<>();
        deviceIdIds = new HashMap<>();
        deviceNames = new HashMap<>();

        addNewDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddDevice = new Intent(Dashboard.this, AddDeviceOption.class);
                startActivity(toAddDevice);
            }
        });
        ivLogout.setOnClickListener(v->{
            FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        tvConnectDevice.setOnClickListener(v->{
            startActivity(new Intent(this, ConnectDeviceActivity.class));
        });

        //----------------------------------------
        setUpNavDrawer();
        setUpToolBar();
        setUpTemp();
        setUpCooling();
        setUpPh();
        setUpPump();
        //----------------------------------------

    }

    private void setUpNavDrawer() {
        String uid = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).getUid();
        FirebaseFirestore.getInstance(PrimaryAccount.getInstance(this))
                .collection("NAMES").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    tvName.setText(documentSnapshot.get("NAME",String.class));
                });
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

    private void getDeviceIds() {
        primaryDatabase.child("DEVICES").get().addOnSuccessListener(dataSnapshot -> {
            //Connect to devices account and get device details
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
            }).addOnFailureListener(exception->{
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
                        int mode = ui.child("MODE").child("MODE_VAL").getValue(Integer.class);
                        int status = ui.child("STATUS").getValue(Integer.class);
                        if (mode == 0) {
                            pumpDevices.add(new PumpDevice(
                                    id,
                                    name,
                                    mode,
                                    ui.child("MODE").child("DOSE").child("SPEED").getValue(Integer.class),
                                    ui.child("MODE").child("DOSE").child("DIR").getValue(Integer.class),
                                    ui.child("MODE").child("DOSE").child("VOL").getValue(Integer.class),
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
                        }

                        break;
                    }
                    case "TEMP_CONTROLLER": {
                        tempDevices.add(new TempDevice(
                                id,
                                name,
                                ui.child("TEMP").child("TEMP_VAL").getValue(Integer.class)
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
                    if(tempDevices.size()==0){
                        tvTemp.setVisibility(View.GONE);
                    }else{
                        tvTemp.setVisibility(View.VISIBLE);
                    }
                    if(coolingDevices.size()==0){
                        tvCooling.setVisibility(View.GONE);
                    }else{
                        tvCooling.setVisibility(View.VISIBLE);
                    }
                    if(phDevices.size()==0){
                        tvPh.setVisibility(View.GONE);
                    }else{
                        tvPh.setVisibility(View.VISIBLE);
                    }
                    if(pumpDevices.size()==0){
                        tvPump.setVisibility(View.GONE);
                    }else{
                        tvPump.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
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
                                refresh();
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