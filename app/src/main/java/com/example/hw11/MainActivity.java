package com.example.hw11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {


    private EditText main_activity_EDT_user_input;
    private Button main_activity_BTN_login;
    BluetoothAdapter bluetoothAdapter;
    CameraManager cameraManager;
    private boolean flashState;
    private final float BATTERY_CHECK_VALUE = 90.0f;
    private final String STORAGE_CHECK_VALUE = "5.2 Gb";
    //private boolean isBluetoothActive, isWifiActive, isCharging, isDoNotDisturb

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("pttt", "onCreate called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        setUpViews();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);


        CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeUnavailable(String cameraId) {
                super.onTorchModeUnavailable(cameraId);
            }

            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                flashState = enabled;
            }
        };
        cameraManager.registerTorchCallback(torchCallback, null);// (callback, handler)





        main_activity_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

    }

    private void setUpViews() {

        main_activity_EDT_user_input = findViewById(R.id.main_activity_EDT_user_input);
        main_activity_BTN_login = findViewById(R.id.main_activity_BTN_login);

    }

    private void checkLogin() {

        /*checkBluetooth();
        checkWifi();
        checkDoNotDisturb();
        checkFlashlight();
        checkAirplane();
        checkStorage();
        checkChargingState();
        checkBatteryValue();*/

        if (checkBluetooth() && checkWifi() && checkDoNotDisturb()  && checkAirplane()
                && checkStorage() && checkChargingState() && checkBatteryValue())
        {
            Log.d("pttt", "Can Login");
        }
    }


    private boolean checkBluetooth() {
        Log.d("pttt", "checkBluetooth: " + bluetoothAdapter.isEnabled());
        return bluetoothAdapter.isEnabled();
    }
    private boolean checkWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Log.d("pttt", "checkWifi: " + wifiManager.isWifiEnabled());
        return wifiManager.isWifiEnabled();

    }



    private boolean checkBatteryValue()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;
        Log.d("pttt", "checkBatteryValue: " + (batteryPct > BATTERY_CHECK_VALUE));
        return batteryPct > BATTERY_CHECK_VALUE;

    }

    private boolean checkChargingState()
    {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, intentFilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||   status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean isAcChargePlugged = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        //Log.d("pttt", "checkChargingState: " + (isCharging && isAcChargePlugged));

        return isCharging && isAcChargePlugged;
    }
    public static String floatForm (double d)
    {
        return new DecimalFormat("#.##").format(d);
    }


    public static String bytesToHuman (long size)
    {
        long Kb = 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        if (size < Kb)   return floatForm(size) + " byte";
        if (size < Mb)    return floatForm((double)size / Kb) + " Kb";
        if (size < Gb)    return floatForm((double)size / Mb) + " Mb";
        if (size < Tb)    return floatForm((double)size / Gb) + " Gb";
        if (size < Pb)    return floatForm((double)size / Tb) + " Tb";
        if (size < Eb)    return floatForm((double)size / Pb) + " Pb";
        return floatForm((double)size / Eb) + " Eb";

    }
    private boolean checkStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        Log.d("pttt", "checkStorage: " + (bytesToHuman(bytesAvailable).compareTo(STORAGE_CHECK_VALUE) > 0));
        return bytesToHuman(bytesAvailable).compareTo(STORAGE_CHECK_VALUE) > 0;
    }

    private boolean checkAirplane()
    {
        Log.d("pttt", "checkAirplane: " + (Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0));
        return Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }
    private boolean checkFlashlight() {
        Log.d("pttt", "checkFlashlight: " + (flashState && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)));
        return flashState && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean checkDoNotDisturb()  {
        try {
            if(Settings.Global.getInt(getContentResolver(), "zen_mode") > 0)
            {
                Log.d("pttt", "checkDoNotDisturb is enabled");
                return true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d("pttt", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d("pttt", "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        Log.d("pttt", "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("pttt", "onResume");
        super.onResume();
    }



    @Override
    protected void onStart() {
        Log.d("pttt", "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d("pttt", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("pttt", "onDestroy");
        super.onDestroy();
    }
}