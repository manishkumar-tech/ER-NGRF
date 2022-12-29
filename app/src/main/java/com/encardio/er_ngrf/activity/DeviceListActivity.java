package com.encardio.er_ngrf.activity;

import static com.encardio.er_ngrf.bluetooth.Communication_Tool.DEFAULT_TEXT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.encardio.er_ngrf.bluetooth.BluetoothService;
import com.encardio.er_ngrf.bluetooth.Communication_Tool;
import com.encardio.er_ngrf.tool.Tool;
import com.encardio.er_ngrf.tool.Variable;

import java.util.Set;

/**
 * @author Sandeep
 */
public class DeviceListActivity extends Activity {

    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if (ActivityCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }
            mBtAdapter.cancelDiscovery();

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            try {

                if (Communication_Tool.bluetoothService != null) {
                    Communication_Tool.bluetoothService.stop();

                    Communication_Tool.BLUETOOTH_ADDRESS = null;
                    Communication_Tool.BLUETOOTH_DEVICE = null;
                }
                Communication_Tool.BLUETOOTH_ADDRESS_FROM_LIST = address;
                Communication_Tool.isNewBluetoothConnection = true;


                //  Intent intent = new Intent();

//                Intent intent = new Intent(DeviceListActivity.this,SplashActivity.class);
//                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
//
//                setResult(Activity.RESULT_OK, intent);
//
//                startActivity(intent);
//                finish();

                setupBluetoothConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    if (ActivityCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                }

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (device.getName().startsWith(Communication_Tool.BT_DEVICE_NAME))
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
    private static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_CONNECT_DEVICE = 1;
    private String m_connectingDeviceName = null;
    private String mConnectedDeviceName = null;
    public static final String DEVICE_NAME = "device_name";
    private Communication_Tool consts;
    // ProgressDialog alertForConnecting;
    Dialog alertForConnecting;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String TOAST = "toast";
    String batteryVoltage;
    String batteryType;
    String batteryInstallationDate;
    ArrayAdapter<String> mPairedDevicesArrayAdapter;


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:

                            batteryVoltage = "";
                            batteryType = "";
                            batteryInstallationDate = "";
                            Variable.logger_ID = "";

                            Variable.isConnected = true;
                            Variable.gotReply = true;

                          //  if (consts.wakeUpDL()) {

                                if (sendConnectionCMD()) {
                                    if (Communication_Tool.BATTERY_DEAD_STATUS == Communication_Tool.sc) {
//                                        img_connection_status.setImageResource(R.drawable.circle_red);
//                                        img_scan_status.setImageResource(R.drawable.circle_red);
//                                        txt_datalogger_id.setText(DEFAULT_TEXT);
//                                        batteryLowDialog();
                                    } else {
                                        try {
//                                            img_connection_status.setImageResource(R.drawable.circle_green);
//                                            if (Variable.scanStatus) {
//                                                img_scan_status.setImageResource(R.drawable.circle_green);
//                                            } else {
//                                                img_scan_status.setImageResource(R.drawable.circle_red);
//                                            }
//                                            txt_datalogger_id.setText(Variable.logger_ID);
                                            Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(DeviceListActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();


                                            if (Communication_Tool.toastFromThread)
                                                Communication_Tool.toastFromThread = false;
                                            Communication_Tool.connectionBreak = false;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                           // }

                            break;
                        case BluetoothService.STATE_CONNECTING:

//                            img_connection_status.setImageResource(R.drawable.circle_red);
//                            img_scan_status.setImageResource(R.drawable.circle_red);
//                            txt_datalogger_id.setText(DEFAULT_TEXT);

                            try{
                                initProgressDialog();
                                alertForConnecting.show();
                            }catch (Exception e){

                                Log.e("exp",e.toString());
                            }

                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:

//                            img_connection_status.setImageResource(R.drawable.circle_red);
//                            img_scan_status.setImageResource(R.drawable.circle_red);
//                            txt_datalogger_id.setText(DEFAULT_TEXT);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    if (Communication_Tool.toastFromThread) {
                        Communication_Tool.toastFromThread = false;
                    }
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
                case MESSAGE_TOAST:
                    if (msg.getData().getBoolean("CONNECTION_FAILED")) {
                        alertForConnecting.dismiss();
                        Variable.isConnected = false;
                    }
                    if (msg.getData().getBoolean("CONNECTION_LOST")) {

//                        img_connection_status.setImageResource(R.drawable.circle_red);
//                        img_scan_status.setImageResource(R.drawable.circle_red);
//                        txt_datalogger_id.setText(DEFAULT_TEXT);
                        Variable.isConnected = false;
                    }
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        setResult(Activity.RESULT_CANCELED);

        consts = new Communication_Tool();
        mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        ListView pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBtAdapter.isEnabled()) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
            }else{


                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);


            }


            }



   showPairedDevices();


    }


    public void showPairedDevices() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        mPairedDevicesArrayAdapter.clear();

        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().startsWith(Communication_Tool.BT_DEVICE_NAME)) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }
            mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("555555","5555555");
    }

    public void setupBluetoothConnection(){

        try {


                   Communication_Tool.bluetoothService = null;

                if (Communication_Tool.bluetoothService == null) {
                    setupBluetoothService();
                }

                if (Communication_Tool.bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {

                    if (Communication_Tool.isNewBluetoothConnection) {

                        Intent intent = new Intent();
                        intent.putExtra(Communication_Tool.EXTRA_DEVICE_ADDRESS,
                                Communication_Tool.BLUETOOTH_ADDRESS_FROM_LIST);
                        onActivityResult(REQUEST_CONNECT_DEVICE,
                                Activity.RESULT_OK, intent);
                        Communication_Tool.isNewBluetoothConnection = false;

                    }

                }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean sendConnectionCMD() {

        if (consts.wakeUpDL()) {
            try {
                Variable.logger_Model = Tool.removeDQ(consts.sendCMDgetRLY("MODEL,\"?\"")).trim();
                //Variable.totalNumberOfRecord = Tool.removeDQ(consts.sendCMDgetRLY("NOOFREC,\"?\"")).trim();
                Log.e("Model", Variable.logger_Model);
               // Log.e("NoOfRec", Variable.totalNumberOfRecord);


                if (Communication_Tool.sc == Communication_Tool.OK_STATUS) {
                    Variable.logger_ID = Tool.removeDQ(consts
                            .sendCMDgetRLY("DLID,\"?\""));

                }

            ////    if (Variable.logger_Model.equalsIgnoreCase(Communication_Tool.DATALOGGER_MODEL)) {
//                    if (Constants.OK_STATUS == Constants.sc) {
//                        String scanStatus = (consts.removeDQ(consts.sendCMDgetRLY("SCAN,\"?\"")));
//                        Variable.scanStatus = !scanStatus.equalsIgnoreCase("STOP");
//                    }
//
//                    if (Constants.OK_STATUS == Constants.sc) {
//                        Variable.logger_ID = consts.removeDQ(consts.sendCMDgetRLY("DLID,\"?\""));
//                    }
//
//                    if (Constants.OK_STATUS == Constants.sc) {
//                        batteryVoltage = consts.sendCMDgetRLY("BATTV,\"?\"");
//                    }
//
//                    if (Constants.OK_STATUS == Constants.sc) {
//                        batteryType = consts.removeDQ(consts.sendCMDgetRLY("BATTYPE,\"?\""));
//                    }
//
//                    if (Constants.OK_STATUS == Constants.sc) {
//                        batteryInstallationDate = consts.sendCMDgetRLY("BATDATE,\"?\"");
//                    }
//                    if (Constants.OK_STATUS == Constants.sc) {
//                        if (Integer.parseInt(consts.removeDQ(batteryInstallationDate.substring(0, 1))) == 0) {
//                            newBatteryFoundDialog(batteryInstallationDate.substring(3,
//                                    batteryInstallationDate.length() - 1));
//                        }
//                    }

//                    if (Constants.OK_STATUS == Constants.sc) {
//                        battery_voltage = Float.parseFloat(batteryVoltage);
//
//
//                        switch (batteryType) {
//                            case "ALKALINE":
//                                if ((battery_voltage > 1.9F) && (battery_voltage < 2.0F)) {
//                                    batteryDialog("Datalogger battery is low !!",
//                                            "            20% remaining."
//                                    );
//                                } else if ((battery_voltage > 1.7F) && (battery_voltage <= 1.9F)) {
//                                    batteryDialog("Datalogger battery is low !!",
//                                            "            10% remaining."
//                                    );
//                                } else if ((battery_voltage > 1.6F) && (battery_voltage <= 1.7F)) {
//                                    batteryDialog("Datalogger battery is too low !!",
//                                            "            5% remaining."
//                                    );
//                                } else if ((battery_voltage > 1.5F) && (battery_voltage <= 1.6F)) {
//                                    batteryDialog("Datalogger battery is too low !!",
//                                            "            2% remaining."
//                                    );
//                                } else if (battery_voltage <= 1.5F) {
//                                    batteryDialog(
//                                            "Datalogger battery is too low !!",
//                                            "Please replace Datalogger battery immediately."
//                                    );
//                                }
//                                break;
//                            case "LITHIUM":
//                                if ((battery_voltage > 6.9F) && (battery_voltage < 7.0F)) {
//                                    batteryDialog("Datalogger battery is low !!",
//                                            "            20% remaining."
//                                    );
//                                } else if ((battery_voltage > 6.8F) && (battery_voltage <= 6.9F)) {
//                                    batteryDialog("Datalogger battery is low !!",
//                                            "            10% remaining."
//                                    );
//                                } else if ((battery_voltage > 6.7F) && (battery_voltage <= 6.8F)) {
//                                    batteryDialog("Datalogger battery is too low !!",
//                                            "            5% remaining."
//                                    );
//                                } else if ((battery_voltage > 6.6F) && (battery_voltage <= 6.7F)) {
//                                    batteryDialog("Datalogger battery is too low !!",
//                                            "            2% remaining."
//                                    );
//                                } else if (battery_voltage <= 6.6F) {
//                                    batteryDialog(
//                                            "Datalogger battery is too low !!",
//                                            "Please replace Datalogger battery immediately."
//                                    );
//                                }
//                                break;
//                            case "EXTERNAL":
//                                if (battery_voltage <= 9.0F) {
//                                    batteryDialog(
//                                            "Datalogger battery is low !!",
//                                            "Please replace Datalogger battery immediately."
//                                    );
//                                }
//                                break;
//                        }
//                    }
         ////       } else {
         ////           alertForConnecting.dismiss();
          ////          showToast("Unable to find Datalogger !!");
            ////        return false;
         ////       }


            } catch (Exception e) {
                alertForConnecting.dismiss();
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Exception occurred " + e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        alertForConnecting.dismiss();
        return true;
    }

    protected void showToast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
    }

//    private void initProgressDialog() {
//        alertForConnecting = new ProgressDialog(this);
//        alertForConnecting.setTitle("Please wait !!");
//        alertForConnecting.setCancelable(true);
//        alertForConnecting.setMessage("Connecting to " + m_connectingDeviceName);
//    }


    private void initProgressDialog() {
        alertForConnecting = new Dialog(DeviceListActivity.this);
        alertForConnecting.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertForConnecting.setContentView(R.layout.dialog_connecting);
        TextView tv=  alertForConnecting.findViewById(R.id.connecting);

        tv.setText("Connecting to " + m_connectingDeviceName);


       // alertForConnecting.setMessage("Connecting to " + m_connectingDeviceName);
    }




    private void setupBluetoothService() {


        Communication_Tool.bluetoothService = new BluetoothService(this, mHandler
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:

                if (resultCode == Activity.RESULT_OK) {

                    String address = data.getExtras().getString(
                            Communication_Tool.EXTRA_DEVICE_ADDRESS);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                    }

                    BluetoothDevice device = mBtAdapter
                            .getRemoteDevice(address);
                    m_connectingDeviceName = device.getName();
                    Communication_Tool.BLUETOOTH_ADDRESS = address;
                    Communication_Tool.BLUETOOTH_DEVICE = m_connectingDeviceName;

                    try {
//                        Communication_Tool.bluetoothService = new BluetoothService(this,
//                                mHandler);
                        Communication_Tool.bluetoothService.connect(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;


            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    //setupBluetoothService();
                    showPairedDevices();
                    Communication_Tool.isBTenabledByApp = true;
                } else {
                    Toast.makeText(this, "Bluetooth connection failure...",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
