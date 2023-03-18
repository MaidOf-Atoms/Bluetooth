package com.example.bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviseListActivity extends AppCompatActivity {
    private ListView listPairedDevices, listAvailableDevices;

    private ProgressBar progressScanDevices;
    private final int BLUETOOTH_PERMISSON = 99;

    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;
        init();
    }

    private void init() {

        listAvailableDevices = findViewById(R.id.lsit_availabele_devices);
        listPairedDevices = findViewById(R.id.lsit_paired_devices);

        progressScanDevices = findViewById(R.id.progress_scan_devices);
        adapterPairedDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length()-17);

                Intent intent = new Intent();
                intent.putExtra("deviceAddress",address);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(DeviseListActivity.this,
                        Manifest.permission.BLUETOOTH_CONNECT)) {
                    new AlertDialog.Builder(context)
                            .setTitle("Bluetooth permission")
                            .setMessage("This app needs bluetooth permission please grant")
                            .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                            BLUETOOTH_PERMISSON);
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            BLUETOOTH_PERMISSON);
                }
                return;
            }
        }else{
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(DeviseListActivity.this,
                        Manifest.permission.BLUETOOTH)) {
                    new AlertDialog.Builder(context)
                            .setTitle("Bluetooth permission")
                            .setMessage("This app needs bluetooth permission please grant")
                            .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                                            new String[]{Manifest.permission.BLUETOOTH},
                                            BLUETOOTH_PERMISSON);
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            BLUETOOTH_PERMISSON);
                }
                return;
            }
        }


        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());

            }
        }
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener, intentFilter1);
    }

    private BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DeviseListActivity.this,
                                Manifest.permission.BLUETOOTH_CONNECT)) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Bluetooth permission")
                                    .setMessage("This app needs bluetooth permission please grant")
                                    .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            ActivityCompat.requestPermissions(DeviseListActivity.this,
                                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                                    BLUETOOTH_PERMISSON);
                                        }
                                    })
                                    .create()
                                    .show();
                        } else {
                            ActivityCompat.requestPermissions(DeviseListActivity.this,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                    BLUETOOTH_PERMISSON);
                        }
                        return;
                    }
                }else{
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DeviseListActivity.this,
                                Manifest.permission.BLUETOOTH)) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Bluetooth permission")
                                    .setMessage("This app needs bluetooth permission please grant")
                                    .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            ActivityCompat.requestPermissions(DeviseListActivity.this,
                                                    new String[]{Manifest.permission.BLUETOOTH},
                                                    BLUETOOTH_PERMISSON);
                                        }
                                    })
                                    .create()
                                    .show();
                        } else {
                            ActivityCompat.requestPermissions(DeviseListActivity.this,
                                    new String[]{Manifest.permission.BLUETOOTH},
                                    BLUETOOTH_PERMISSON);
                        }
                        return;
                    }
                }

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progressScanDevices.setVisibility(View.GONE);
                if(adapterAvailableDevices.getCount()== 0){
                    Toast.makeText(context,"no new devices found",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context,"click on the device to start the chat",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_devices:
                scanDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scanDevices() {
        progressScanDevices.setVisibility(View.VISIBLE);
        adapterAvailableDevices.clear();
        Toast.makeText(context, "Scanning", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(DeviseListActivity.this,
                        Manifest.permission.BLUETOOTH_SCAN)) {
                    new AlertDialog.Builder(context)
                            .setTitle("Bluetooth permission")
                            .setMessage("This app needs bluetooth permission please grant")
                            .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                            BLUETOOTH_PERMISSON);
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            BLUETOOTH_PERMISSON);
                }
                return;
            }
        }else{
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(DeviseListActivity.this,
                        Manifest.permission.BLUETOOTH)) {
                    new AlertDialog.Builder(context)
                            .setTitle("Bluetooth permission")
                            .setMessage("This app needs bluetooth permission please grant")
                            .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                                            new String[]{Manifest.permission.BLUETOOTH},
                                            BLUETOOTH_PERMISSON);
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(DeviseListActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            BLUETOOTH_PERMISSON);
                }
                return;
            }
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }
}