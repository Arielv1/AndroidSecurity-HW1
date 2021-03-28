package com.example.hw11;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {


    private EditText main_activity_EDT_user_input;
    private Button main_activity_BTN_login;
    BluetoothAdapter bluetoothAdapter;
    CameraManager cameraManager;
    private boolean flashState;
    private static final float BATTERY_CHECK_VALUE = 90.0f;
    private static final String STORAGE_CHECK_VALUE = "5.2 Gb";
    private static final int MIN_CONTACT_NUM = 10;
    private static final int PERMISSION_CONTACTS_REQUEST_CODE = 123;
    private static final int MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE = 124;


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

    checkFlashlight();
        if (checkBluetooth() && checkWifi() && checkDoNotDisturb()  && checkAirplane()
                && checkStorage() && checkChargingState() && checkBatteryValue() && checkNumOfContacts())
        {
            Log.d("pttt", "Can Login");
            Toaster.getInstance().showToast("Perform Login");
            Intent intent = new Intent(getApplicationContext(), Activity_Passed_Login.class);
            startActivity(intent);
        }
    }


    private boolean checkBluetooth() {
        if (!bluetoothAdapter.isEnabled())
            Toaster.getInstance().showToast("Activate Bluetooth");
        Log.d("pttt", "checkBluetooth: " + bluetoothAdapter.isEnabled());
        return bluetoothAdapter.isEnabled();
    }
    private boolean checkWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            Toaster.getInstance().showToast("Activate Wifi");
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

        if (!(batteryPct > BATTERY_CHECK_VALUE))
            Toaster.getInstance().showToast("Battery isn't charged enough");
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

        if (!(isCharging && isAcChargePlugged))
            Toaster.getInstance().showToast("Plug phone to charger");
        Log.d("pttt", "checkChargingState: " + (isCharging && isAcChargePlugged));
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
        if (!(bytesToHuman(bytesAvailable).compareTo(STORAGE_CHECK_VALUE) > 0))
            Toaster.getInstance().showToast("Need to have more than 5.2Gb free space");
        Log.d("pttt", "checkStorage: " + (bytesToHuman(bytesAvailable).compareTo(STORAGE_CHECK_VALUE) > 0));
        return bytesToHuman(bytesAvailable).compareTo(STORAGE_CHECK_VALUE) > 0;
    }

    private boolean checkAirplane()
    {
        Log.d("pttt", "checkAirplane: " + (Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0));
        if (Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 0)
            Toaster.getInstance().showToast("Activate Airplane mode");

        return Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }
    private boolean checkFlashlight() {
        Log.d("pttt", "checkFlashlight: " + (flashState && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)));
        return flashState && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean checkDoNotDisturb()  {
        try {
            if(Settings.Global.getInt(getContentResolver(), "zen_mode") > 0) {
                Log.d("pttt", "checkDoNotDisturb is enabled");
                return true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Toaster.getInstance().showToast("Enable DND mode");
        return false;
    }

    private boolean checkNumOfContacts() {
        if (getNumOfContacts() < MIN_CONTACT_NUM) {
            Toaster.getInstance().showToast("You need to have " + MIN_CONTACT_NUM + " contacts");
            return false;
        }
        return true;
    }

    private int getNumOfContacts() {
        boolean isGranted = checkForPermission();

        if (!isGranted) {
            requestPermission();
            return 0;
        }

        String data = "";
        int numContacts = 0;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {

                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        data += "\n" + name + ": " + phoneNo;
                        numContacts++;
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        Log.d("pttt", "Contracts Number: " + numContacts);
        Log.d("pttt", "Contracts: " + data);
        return numContacts;
    }



    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CONTACTS},
                PERMISSION_CONTACTS_REQUEST_CODE);
    }

    private void requestPermissionWithRationaleCheck() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
            Log.d("pttt", "shouldShowRequestPermissionRationale = true");
            // Show user description for what we need the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_CONTACTS_REQUEST_CODE);
        } else {
            Log.d("pttt", "shouldShowRequestPermissionRationale = false");
            openPermissionSettingDialog();
        }
    }

    private void openPermissionSettingDialog() {
        String message = "Setting screen if user have permanently disable the permission by clicking Don't ask again checkbox.";
        androidx.appcompat.app.AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE) {
            getNumOfContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CONTACTS_REQUEST_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getNumOfContacts();
                } else {
                    requestPermissionWithRationaleCheck();
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    private boolean checkForPermission() {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}