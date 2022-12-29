package com.encardio.er_ngrf.activity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.encardio.er_ngrf.bluetooth.Communication_Tool.DEFAULT_TEXT;
import static com.encardio.er_ngrf.tool.Variable.flagBlePr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.encardio.er_ngrf.bluetooth.BluetoothService;
import com.encardio.er_ngrf.bluetooth.Communication_Tool;
import com.encardio.er_ngrf.tool.Check_LatestSystem;
import com.encardio.er_ngrf.tool.Tool;
import com.encardio.er_ngrf.tool.Variable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.util.Set;

/**
 * @author Sandeep
 */
public class HomeActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    LocationClient mLocationClient;

    ProgressDialog alertForConnecting;
    String batteryVoltage;
    String batteryType;
    String batteryInstallationDate;
    float battery_voltage;
    private TextView txt_datalogger_id;
    private ImageView img_connection_status;
    private ImageView img_scan_status;
    private String m_connectingDeviceName = null;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter;
    private short ftp_testw;
    private long currentTimemill;
    private final Handler handler = new Handler();
    Thread thread;
    private Communication_Tool consts;


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

                            if (consts.wakeUpDL()) {

                                if (sendConnectionCMD()) {
                                    if (Communication_Tool.BATTERY_DEAD_STATUS == Communication_Tool.sc) {
                                        img_connection_status.setImageResource(R.drawable.circle_red);
                                        img_scan_status.setImageResource(R.drawable.circle_red);
                                        txt_datalogger_id.setText(DEFAULT_TEXT);
//                                        batteryLowDialog();
                                    } else {
                                        try {
                                            img_connection_status.setImageResource(R.drawable.circle_green);
                                            if (Variable.scanStatus) {
                                                img_scan_status.setImageResource(R.drawable.circle_green);
                                            } else {
                                                img_scan_status.setImageResource(R.drawable.circle_red);
                                            }
                                            txt_datalogger_id.setText(Variable.logger_ID);
                                            Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                                            if (Communication_Tool.toastFromThread)
                                                Communication_Tool.toastFromThread = false;
                                            Communication_Tool.connectionBreak = false;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            break;
                        case BluetoothService.STATE_CONNECTING:

                            img_connection_status.setImageResource(R.drawable.circle_red);
                            img_scan_status.setImageResource(R.drawable.circle_red);
                            txt_datalogger_id.setText(DEFAULT_TEXT);
                            initProgressDialog();
                            alertForConnecting.show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:

                            img_connection_status.setImageResource(R.drawable.circle_red);
                            img_scan_status.setImageResource(R.drawable.circle_red);
                            txt_datalogger_id.setText(DEFAULT_TEXT);
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

                        img_connection_status.setImageResource(R.drawable.circle_red);
                        img_scan_status.setImageResource(R.drawable.circle_red);
                        txt_datalogger_id.setText(DEFAULT_TEXT);
                        Variable.isConnected = false;
                    }
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;

//                case 101:
//
//                    Toast.makeText(HomeActivity.this, msg.getData().getString("test"), Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    };


    private int totalRecordToDownload = 0;

    @SuppressLint("StaticFieldLeak")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        txt_datalogger_id = findViewById(R.id.txt_datalogger_id);
        img_connection_status = findViewById(R.id.img_connection_status);
        img_scan_status = findViewById(R.id.img_scan_status);

        img_connection_status.setImageResource(R.drawable.circle_red);

        img_scan_status.setImageResource(R.drawable.circle_red);
        txt_datalogger_id.setText(DEFAULT_TEXT);
        consts = new Communication_Tool();


    }


    public void setup_wizard(View view) {
        Intent intent;
        if (Variable.is_device_type_node) {
            intent = new Intent(view.getContext(),
                    NodeWizardActivity.class);
        } else {
            intent = new Intent(view.getContext(),
                    GatewayWizardActivity.class);
        }
        startActivity(intent);
    }

    public void detail_configuration(View view) {
//        Intent intent;
//        if (Variable.is_device_type_node) {
//            intent = new Intent(view.getContext(),
//                    NodeConfigActivity.class);
//        } else {
//            intent = new Intent(view.getContext(),
//                    GatewayConfigActivity.class);
//        }
//        startActivity(intent);
//        initProgressDialog();
//        alertForConnecting.show();

//        thread = new Thread(HomeActivity.this);
//        thread.start();

//       if(consts.wakeUpDL()){
//
//        new Thread(new MyThread()).start();
//
//
//        }
        consts.wakeUpDL();
        if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {
            Variable.totalNumberOfRecord = Tool.removeDQ(consts
                    .sendCMDgetRLY("NOOFREC,\"?\""));
            Log.e("cmd", Variable.totalNumberOfRecord);
        }


        if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {
            String fwver = Tool.removeDQ(consts
                    .sendCMDgetRLY("FWVER,\"?\""));
            Log.e("cmd", fwver);
        }

        if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {

            new Thread(new MyThread()).start();

        }

    }


    class MyThread implements Runnable {

        @Override
        public void run() {

            int temp1, temp;

            Communication_Tool.countRecords = 0;
            temp = totalRecordToDownload / 90;
            if (temp == 0) {
                temp = 1;
            }

//                if(consts.wakeUpDL()){
//                    Variable.totalNumberOfRecord = Tool.removeDQ(consts
//                            .sendCMDgetRLY("NOOFREC,\"?\""));
//                    Log.e("NoofRec",Variable.totalNumberOfRecord);
//                }

            //  if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {
//                        Variable.totalNumberOfRecord = Tool.removeDQ(communication_tool
//                                .sendCMDgetRLY("NOOFREC,\"?\""));
            //  }

//Log.e("NoofRec",Variable.totalNumberOfRecord);
            Tool.removeDQ(consts
                    .sendCMDgetFullRLY("GETDATA,\"?\""));


            if (Communication_Tool.MODEM_COMM_MODE_GPRS_STATUS == Communication_Tool.sc) {
//                        if (dialog.isShowing())
//                            dialog.dismiss();
//                        Looper.prepare();
                //showDialogGPRS_Mode();
            } else {
                if (Variable.gotReply) {
                    try {
                        Communication_Tool.delay(100);
                        if (Communication_Tool.dataDownloadUnavailble)
                            return;
                        if (Communication_Tool.dataDownloadAvailble) {
                            Communication_Tool.download_Watchdog_Timer = System
                                    .currentTimeMillis();
                            while (!Communication_Tool.dataDownloadCompleted) { // wait till
                                // download
                                // complete
                                if ((System.currentTimeMillis() - Communication_Tool.download_Watchdog_Timer) > 30000) {
                                    Communication_Tool.expInBluetoothConnection = true;
                                    //  error_msg = "Data couldn't be downloaded from datalogger due to connection error...";
                                    break;
                                } else {
                                    // show progress for downloaded data
                                    temp1 = Communication_Tool.countRecords / temp;
                                    if (Communication_Tool.progress != temp1) {
                                        Communication_Tool.progress = temp1;
                                        // dialog.setProgress(Communication_Tool.progress);
                                    }
                                }

                            }
                            if (Communication_Tool.dataDownloadCompleted) {
                                String downloadData = Communication_Tool.downloadData;
                                if (downloadData != null) {

                                    Log.e("cmd", downloadData);
                                    // generateCsvFile(downloadData);
                                    Communication_Tool.progress = Communication_Tool.progress + 5;
                                    //  dialog.setProgress(Communication_Tool.progress);
                                }
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                } else {
//                            if (dialog.isShowing())
//                                dialog.dismiss();
//                            Looper.prepare();
                    // showDialog();
                }
            }

        }
    }

    public void system_information(View view) {
        Intent intent;
        if (Variable.is_device_type_node) {
            intent = new Intent(view.getContext(),
                    NodeInfoActivity.class);
        } else {
            intent = new Intent(view.getContext(),
                    GatewayInfoActivity.class);
        }

        startActivity(intent);
    }

    public void view_data(View view) {
        Intent intent = new Intent(view.getContext(),
                ViewDataActivity.class);
        startActivity(intent);
    }

    public void upload_data(View view) {
        Intent intent = new Intent(view.getContext(),
                UploadDataActivity.class);
        startActivity(intent);
    }

    private void datalogger_not_connected_dialog() {
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("Logger Not Connected !!")
                .setIcon(R.drawable.info)
                .setMessage(
                        "Logger is not connected. Please connect the Logger.")
                .setPositiveButton(
                        "OK",
                        (dialog, which) -> {
                        }).show();
    }


    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(HomeActivity.this)
                .setMessage("You need to allow access to these permissions")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void sendFTPTest() {
        consts.wakeUpDL();
        consts.sendCMDgetRLYforMonPara("TESTFTP");
        ftp_testw = (short) Communication_Tool.sc;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Tab OK to go to setting screen for turning On GPS.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(
                            final DialogInterface dialog,
                            final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        mLocationClient.connect();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkScanStatus() {
        consts.wakeUpDL();
        String reply = consts.sendCMDgetRLY("SCAN,\"?\"");
        if (Communication_Tool.OK_STATUS == Communication_Tool.sc)
            if (reply.trim().equalsIgnoreCase("ON")
                    || reply.trim().equalsIgnoreCase("START")) {
                Variable.scanStatus = true;
            }
    }

    private void stopScan() {
        consts.wakeUpDL();
        consts.sendCMDgetFullRLY("SCAN,\"STOP\"");
        if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {
            Variable.scanStatus = false;
        }
    }

    public void onStart() {
        super.onStart();


        if (Communication_Tool.toastFromThread) {

            img_connection_status.setImageResource(R.drawable.circle_red);
            img_scan_status.setImageResource(R.drawable.circle_red);
            txt_datalogger_id.setText(DEFAULT_TEXT);

        }


    }




    private boolean sendConnectionCMD() {

        if (consts.wakeUpDL()) {
            try {
                Variable.logger_Model = Tool.removeDQ(consts.sendCMDgetRLY("MODEL,\"?\"")).trim();
                Log.e("Model", Variable.logger_Model);
                if (Variable.logger_Model.equalsIgnoreCase(Communication_Tool.DATALOGGER_MODEL)) {
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
                } else {
                    alertForConnecting.dismiss();
                    showToast("Unable to find Datalogger !!");
                    return false;
                }
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

//    @SuppressLint("SetTextI18n")
//    private void newBatteryFoundDialog(String batteryID) {
//        try {
//            final Dialog mdialog = new Dialog(HomeActivity.this);
//            mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            mdialog.setContentView(R.layout.new_battery);
//            final Button buttonDate = mdialog.findViewById(R.id.buttonDate);
//            Button buttonOk = mdialog.findViewById(R.id.buttonOK);
//            Button buttonCancel = mdialog.findViewById(R.id.buttonCancel);
//            buttonDate.setText("" + batteryID);
//
//            buttonDate.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    final Calendar myCalendar = Calendar.getInstance();
//                    DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
//                        @Override
//                        public void onDateSet(DatePicker view, int year,
//                                              int monthOfYear, int dayOfMonth) {
//                            myCalendar.set(Calendar.YEAR, year);
//                            myCalendar.set(Calendar.MONTH, monthOfYear);
//                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                            try {
//                                buttonDate.setText(parseDate(myCalendar.getTime()
//                                        .toString()));
//                            } catch (Exception ignored) {
//                            }
//                        }
//                    };
//
//                    new DatePickerDialog(HomeActivity.this, datePicker, myCalendar
//                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
//                }
//            });
//            buttonOk.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String date = buttonDate.getText().toString().trim();
//                    consts.wakeUpDL();
//                    consts.sendCMDgetRLY("BATDATE,\"" + date + "\"");
//
//                    if (Constants.sc != Constants.OK_STATUS) {
//                        Toast.makeText(getApplicationContext(), "Invalid date...",
//                                Toast.LENGTH_LONG).show();
//                    }
//                    mdialog.dismiss();
//                }
//            });
//            buttonCancel.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mdialog.dismiss();
//                }
//            });
//            mdialog.show();
//        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Exception occurred " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }

    private String parseDate(String date) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] dateArr = date.split(" ");
        int mnth = -1;
        String month;
        String day;
        String year;
        for (int i = 0; i < 12; i++) {
            if (months[i].equalsIgnoreCase(dateArr[1])) {
                mnth = i + 1;
                break;
            }
        }
        month = Tool.pad(mnth).trim();
        day = dateArr[2].trim();
        year = dateArr[5].trim();

        return year + "/" + month + "/" + day;
    }

    private void batteryLowDialog() {

        Communication_Tool.battery_low = false;
        new AlertDialog.Builder(HomeActivity.this).setIcon(R.drawable.ic_battery)
                .setTitle(Communication_Tool.responseMsg)
                .setMessage("Please replace battery immediately !!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();
    }


    private void batteryDialog(String title, String message) {
        new AlertDialog.Builder(HomeActivity.this).setTitle(title).setIcon(R.drawable.ic_battery)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                }).show();
    }


    private void initProgressDialog() {
        alertForConnecting = new ProgressDialog(this);
        alertForConnecting.setTitle("Please wait !!");
        alertForConnecting.setCancelable(true);
        alertForConnecting.setMessage("Connecting to " + m_connectingDeviceName);
    }

    protected void showToast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (Variable.isConnected) {
            Log.e("111111", "111111");
            txt_datalogger_id.setText(Variable.logger_ID);
            img_connection_status.setImageResource(R.drawable.circle_green);
            if (Variable.scanStatus) {
                img_scan_status.setImageResource(R.drawable.circle_green);
            } else {
                img_scan_status.setImageResource(R.drawable.circle_red);
            }


        } else {

            Log.e("2222222", "22222222");
            txt_datalogger_id.setText(DEFAULT_TEXT);
            img_scan_status.setImageResource(R.drawable.circle_red);
            img_connection_status.setImageResource(R.drawable.circle_red);
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
      //  mLocationClient.disconnect();
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_quit)
                    .setTitle("QUIT")
                    .setMessage(R.string.really_quit)
                    .setPositiveButton(R.string.yes,
                            (dialog, which) -> {
                                Communication_Tool.tag = 0;
                                Communication_Tool.tag1 = 0;
                                quitFromApp();
                            }).setNegativeButton(R.string.no, null).show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void quitFromApp() {
        Variable.isConnected = false;
        if (Communication_Tool.bluetoothService != null) {
            Communication_Tool.bluetoothService.stop();
            Communication_Tool.bluetoothService = null;
        }

        if (Communication_Tool.isBTenabledByApp) {

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

            mBluetoothAdapter.disable();
        } else {
            Toast.makeText(
                    HomeActivity.this,
                    "Bluetooth is not being turned off !!\nIt was enabled by another application...",
                    Toast.LENGTH_LONG).show();
        }
        Communication_Tool.isBTenabledByApp = false;
        mBluetoothAdapter = null;
        HomeActivity.this.finish();
    }



    @SuppressLint("StaticFieldLeak")
    public class asyncTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;

        public asyncTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(HomeActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Wait.....");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            checkScanStatus();
            if (Variable.scanStatus)
                stopScan();
            consts.wakeUpDL();
            consts.sendCMDgetFullRLY("SHDN");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {
                showToast("Datalogger shutdown successfully...");
                img_connection_status.setImageResource(R.drawable.circle_red);
                img_scan_status.setImageResource(R.drawable.circle_red);
                txt_datalogger_id.setText(DEFAULT_TEXT);
                quitFromApp();
            } else if (Communication_Tool.battery_low)
                batteryLowDialog();
            else if (Communication_Tool.toastFromThread)
                datalogger_not_connected_dialog();
            else
                showToast(Communication_Tool.sc + ": Datalogger doesn't get shutdown...");
        }
    }
}