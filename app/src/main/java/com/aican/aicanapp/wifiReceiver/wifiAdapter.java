package com.aican.aicanapp.wifiReceiver;

import static android.content.Context.WIFI_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;

import java.util.Arrays;
import java.util.List;

public class wifiAdapter extends RecyclerView.Adapter<wifiViewHolder> {

    private static final String TAG = "WIFI_CONNECTION";
    Context context;
    List<ScanResult> resultList;

    public wifiAdapter(Context context, List<ScanResult> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    @NonNull
    @Override
    public wifiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_wifi, parent, false);
        return new wifiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull wifiViewHolder holder, int position) {
        String ssid = resultList.get(position).SSID;
        String capabilities = resultList.get(position).capabilities;
        holder.ssid.setText(ssid.toString());
//        holder.itemView.setVisibility(View.GONE);
//        if (ssid.contains("AICAN") || ssid.contains("Aican")|| ssid.contains("aican")) {
//            holder.itemView.setVisibility(View.VISIBLE);
//        } else {
//            holder.itemView.setVisibility(View.GONE);
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isWifiEnabled()) {
                    Toast.makeText(context, "Turning WiFi ON...", Toast.LENGTH_LONG).show();
                    wifiManager.setWifiEnabled(true);
                } else {
                    boolean connected = connectToNetwork(ssid, "12345678",capabilities);
                    if (connected) {
//                        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(context, ConnectDeviceActivity.class);
//                        intent.putExtra("ssid", ssid);
//                        intent.putExtra("pass", "12345678");
//                        intent.putExtra("flag","c");
//                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Not connected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

    private boolean connect(String ssid, String pass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", pass);

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
//remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        return wifiManager.reconnect();
    }

    private boolean connectToNetwork(String networkSSID, String networkPass, String networkCapabilities) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        if (networkCapabilities.toUpperCase().contains("WEP")) { // WEP Network.
            Toast.makeText(context, "WEP Network", Toast.LENGTH_SHORT).show();

            wifiConfig.wepKeys[0] = String.format("\"%s\"", networkPass);
            wifiConfig.wepTxKeyIndex = 0;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (networkCapabilities.toUpperCase().contains("WPA")) { // WPA Network
            Toast.makeText(context, "WPA Network", Toast.LENGTH_SHORT).show();
            wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        } else { // OPEN Network.
            Toast.makeText(context, "OPEN Network", Toast.LENGTH_SHORT).show();
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }


        int netId = wifiManager.addNetwork(wifiConfig);//
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        return wifiManager.reconnect();

    }

    public boolean connectToNetworkWPA(String networkSSID, String password) {
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain SSID in quotes

            conf.preSharedKey = "\"" + password + "\"";

            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            Log.d("connecting", conf.SSID + " " + conf.preSharedKey);

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);

            Log.d("after connecting", conf.SSID + " " + conf.preSharedKey);


//remember id
            int netId = wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
//            } else {
//                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//                for (WifiConfiguration i : list) {
//                    if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
//                        wifiManager.disconnect();
//                        wifiManager.enableNetwork(i.networkId, true);
//                        wifiManager.reconnect();
//                        Log.d("re connecting", i.SSID + " " + conf.preSharedKey);
//                        Toast.makeText(context, "reconnecting...", Toast.LENGTH_SHORT).show();
//                        break;
//                    }
//                }
//            }

            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }

    public boolean ConnectToNetworkWEP(String networkSSID, String password) {
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain SSID in quotes
            conf.wepKeys[0] = "\"" + password + "\""; //Try it with quotes first

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedGroupCiphers.set(WifiConfiguration.AuthAlgorithm.SHARED);


            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int networkId = wifiManager.addNetwork(conf);

            if (networkId == -1) {
                //Try it again with no quotes in case of hex password
                conf.wepKeys[0] = password;
                networkId = wifiManager.addNetwork(conf);
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    break;
                }
            }

            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }
}
