package com.encardio.er_ngrf.activity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.encardio.er_ngrf.bluetooth.Communication_Tool;
import com.encardio.er_ngrf.tool.Variable;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllPermissionsActivity extends AppCompatActivity {


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    Button ok;

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (Environment.isExternalStorageManager()) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){

                            requestBlePermission();
                        }

                          isStoragePermissionGranted = true;
                        cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));

                    } else {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                    Uri.parse("package:" + "com.encardio.er_ngrf.activity"));
                            //  startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                            activityResultLaunch.launch(intent);


                        } catch (Exception e) {

                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            //    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                            activityResultLaunch.launch(intent);

                        }
                    }
                }
            });


    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            BLUETOOTH_SCAN,
            BLUETOOTH_CONNECT
    };






    ImageView cbsp,cbbp, cblp, cbpp, cbsmsp;
    private int PERMISSION_REQUEST_CODE = 200;
    private int BLE_PERMISSION_REQUEST_CODE = 101;
    private int STORAGE_PERMISSION_REQUEST_CODE = 111;
    private int LOCATION_PERMISSION_REQUEST_CODE = 222;
    private int PHONE_PERMISSION_REQUEST_CODE = 333;
    private int SMS_PERMISSION_REQUEST_CODE = 444;

    boolean isStoragePermissionGranted,isBluetoothPermissionGranted,isLocationPermissionGranted, isPhonePermissionGranted, isSMSPermissionGranted;
    private BluetoothAdapter mBtAdapter;
    private static final int REQUEST_ENABLE_BT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_file_permission);


        cbsp = findViewById(R.id.cbsp);
        cbbp = findViewById(R.id.cbbp);
        cblp = findViewById(R.id.cblp);
        cbpp = findViewById(R.id.cbpp);
        cbsmsp = findViewById(R.id.cbsmsp);





        checkAllPermissionGranted(null);

        //30 = 11




        if(Variable.flagStoragePr<=2){
            requestStoragePermission();
        }

//        if(Variable.flagBlePr<=2){
//            requestBlePermission();
//        }
//
//        if(Variable.flagLocationPr<=2){
//            requestLocationPermission();
//        }
//
//        if(Variable.flagPhonePr<=2){
//            requestPhonePermission();
//        }
//
//        if(Variable.flagSMSPr<=2){
//            requestSMSPermission();
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//
//            if (Environment.isExternalStorageManager()) {
//
//               cbsp.setImageDrawable(getDrawable(R.drawable.checked_checkbox));
//
//            }
//        }else {
//
//            if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//
//            }
//
//
//            if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//
//            }
//
//
//
//
//
//
//        }




        ok = findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.e("grantedpermisssion", getGrantedPermissions(getPackageName()).toString());

                checkAllPermissionGranted(v);
            }
        });



    }


    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.allowpermissionsteps);

        Button ok= bottomSheetDialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSetting();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }


    List<String> getGrantedPermissions(final String appPackage) {
        List<String> granted = new ArrayList<String>();
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(appPackage, PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    granted.add(pi.requestedPermissions[i]);
                }
            }
        } catch (Exception e) {
        }
        return granted;
    }


    public void checkAllPermissionGranted(View v){
        List<String> grantedPermissions =  getGrantedPermissions(getPackageName());



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (grantedPermissions.contains("android.permission.BLUETOOTH_CONNECT") && grantedPermissions.contains("android.permission.BLUETOOTH_SCAN") && grantedPermissions.contains("android.permission.ACCESS_FINE_LOCATION") && grantedPermissions.contains("android.permission.ACCESS_COARSE_LOCATION") && grantedPermissions.contains("android.permission.READ_PHONE_STATE") && grantedPermissions.contains("android.permission.SEND_SMS") && Environment.isExternalStorageManager()) {


                    Intent intent = new Intent(AllPermissionsActivity.this, DeviceListActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    if(v != null){

                        showBottomSheetDialog();

                    }

                }
            }else  if (grantedPermissions.contains("android.permission.ACCESS_FINE_LOCATION") && grantedPermissions.contains("android.permission.ACCESS_COARSE_LOCATION") && grantedPermissions.contains("android.permission.READ_PHONE_STATE") && grantedPermissions.contains("android.permission.SEND_SMS") && Environment.isExternalStorageManager()) {

                Intent intent = new Intent(AllPermissionsActivity.this, DeviceListActivity.class);
                startActivity(intent);
                finish();

            }else{
                if(v != null) {
                    showBottomSheetDialog();

                }
            }

        }else {

            if (grantedPermissions.contains("android.permission.ACCESS_FINE_LOCATION") && grantedPermissions.contains("android.permission.ACCESS_COARSE_LOCATION") && grantedPermissions.contains("android.permission.READ_PHONE_STATE") && grantedPermissions.contains("android.permission.SEND_SMS") && grantedPermissions.contains("android.permission.WRITE_EXTERNAL_STORAGE") && grantedPermissions.contains("android.permission.READ_EXTERNAL_STORAGE")) {

                Intent intent = new Intent(AllPermissionsActivity.this, DeviceListActivity.class);
                startActivity(intent);
                finish();

            } else {

                if(v != null) {
                    showBottomSheetDialog();

                }
            }

        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(AllPermissionsActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, INTERNET,  BLUETOOTH_ADMIN, BLUETOOTH, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_PHONE_STATE,SEND_SMS}, PERMISSION_REQUEST_CODE);
    }





    public void requestStoragePermission(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (!Environment.isExternalStorageManager()) {

                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + "com.encardio.er_ngrf.activity"));
                    //  startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                    activityResultLaunch.launch(intent);


                } catch (Exception e) {

                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    //    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                    activityResultLaunch.launch(intent);


                }
            }

        }else{
            ActivityCompat.requestPermissions(AllPermissionsActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_REQUEST_CODE);

        }
    }

    public void requestBlePermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            ActivityCompat.requestPermissions(AllPermissionsActivity.this, new String[]{BLUETOOTH_SCAN, BLUETOOTH_CONNECT}, BLE_PERMISSION_REQUEST_CODE);
        }

    }

    public void requestLocationPermission(){

        ActivityCompat.requestPermissions(AllPermissionsActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);

    }

    public void requestPhonePermission(){

        ActivityCompat.requestPermissions(AllPermissionsActivity.this, new String[]{READ_PHONE_STATE},PHONE_PERMISSION_REQUEST_CODE);

    }


    public void requestSMSPermission(){

        ActivityCompat.requestPermissions(AllPermissionsActivity.this, new String[]{SEND_SMS},SMS_PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.e("permisssion", Arrays.toString(permissions));
        Log.e("permisssiongrantResults", Arrays.toString(grantResults));


        if(requestCode == STORAGE_PERMISSION_REQUEST_CODE) {

            if(grantResults[0] == 0 && grantResults[1] == 0){

                requestLocationPermission();
                cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
            }else{

                cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
                Variable.flagStoragePr++;

                if(Variable.flagStoragePr <= 2){
                    requestStoragePermission();
                }else{
                    requestLocationPermission();
                }


            }

        }


        if(requestCode == BLE_PERMISSION_REQUEST_CODE){

            if(grantResults[0] == 0 && grantResults[1] == 0){

                isBluetoothPermissionGranted = true;
                cbbp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
                //requestPermission();
                requestLocationPermission();

            }else{

                isBluetoothPermissionGranted = false;
                cbbp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));

                Variable.flagBlePr++;

                if(Variable.flagBlePr <= 2){
                    requestBlePermission();
                }else{
                    requestLocationPermission();
                }




            }

        }



        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){

            if(grantResults[0] == 0 && grantResults[1] == 0){


                cblp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
                //requestPermission();
                requestPhonePermission();

            }else{


                cblp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));

                //requestBlePermissions(AllPermissionsActivity.this,BLE_PERMISSION_REQUEST_CODE);


                Variable.flagLocationPr++;

                if(Variable.flagLocationPr <= 2){
                    requestLocationPermission();
                }else{
                    requestPhonePermission();
                }

            }

        }


        if(requestCode == PHONE_PERMISSION_REQUEST_CODE){

            if(grantResults[0] == 0){


                cbpp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
                //requestPermission();
                requestSMSPermission();

            }else{


                cbpp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));

                //requestBlePermissions(AllPermissionsActivity.this,BLE_PERMISSION_REQUEST_CODE);

                Variable.flagPhonePr++;


                if(Variable.flagPhonePr <= 2){
                    requestPhonePermission();
                }else{
                    requestSMSPermission();
                }

            }

        }


        if(requestCode == SMS_PERMISSION_REQUEST_CODE){

            if(grantResults[0] == 0){


                cbsmsp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
                //requestPermission();


            }else{


                cbsmsp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));

                //requestBlePermissions(AllPermissionsActivity.this,BLE_PERMISSION_REQUEST_CODE);

                Variable.flagSMSPr++;


                if(Variable.flagSMSPr <= 2){
                    requestSMSPermission();
                }

            }

        }

    }


    public void goToSetting(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
       // Toast.makeText(getApplicationContext(), "Please allow all permissions from setting.", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//
//                isBluetoothPermissionGranted = true;
//                cbbp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//            }else{
//                isBluetoothPermissionGranted = false;
//                cbbp.setImageDrawable(getResources().getDrawable(R.drawable.unchecked_checkbox));
//            }
//        } else {
//            isBluetoothPermissionGranted = true;
//            cbbp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (Environment.isExternalStorageManager()) {
//
//                isStoragePermissionGranted = true;
//                cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//            }else{
//                isStoragePermissionGranted = false;
//                cbsp.setImageDrawable(getResources().getDrawable(R.drawable.unchecked_checkbox));
//            }
//        }else{
//            if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(AllPermissionsActivity.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//                isStoragePermissionGranted = true;
//                cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//            }else{
//                isStoragePermissionGranted = false;
//                cbsp.setImageDrawable(getResources().getDrawable(R.drawable.unchecked_checkbox));
//            }
//        }
//
//
//        if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(AllPermissionsActivity.this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            isLocationPermissionGranted = true;
//            cblp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//        }else{
//            isLocationPermissionGranted = false;
//            cblp.setImageDrawable(getResources().getDrawable(R.drawable.unchecked_checkbox));
//        }
//
//
//        if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ) {
//
//            isPhonePermissionGranted = true;
//            cbpp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//        }else{
//            isPhonePermissionGranted = false;
//            cbpp.setImageDrawable(getResources().getDrawable(R.drawable.unchecked_checkbox));
//        }
//
//
//        if (ContextCompat.checkSelfPermission(AllPermissionsActivity.this, SEND_SMS) == PackageManager.PERMISSION_GRANTED ) {
//
//            isPhonePermissionGranted = true;
//            cbpp.setImageDrawable(getResources().getDrawable(R.drawable.checked_checkbox));
//        }else{
//            isPhonePermissionGranted = false;
//            cbpp.setImageDrawable(getResources().getDrawable(R.drawable.unchecked_checkbox));
//        }


           List<String> grantedPermissions =  getGrantedPermissions(getPackageName());

           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
               if(grantedPermissions.contains("android.permission.BLUETOOTH_CONNECT") && grantedPermissions.contains("android.permission.BLUETOOTH_SCAN")){
                   cbbp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
               }else{
                   cbbp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));
               }
           }else{
               cbbp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
           }


           if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
               if(grantedPermissions.contains("android.permission.WRITE_EXTERNAL_STORAGE") && grantedPermissions.contains("android.permission.READ_EXTERNAL_STORAGE")){
                   cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
               }else{
                   cbsp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));
               }
           }else{
               if (Environment.isExternalStorageManager()) {
                   cbsp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
               }else{
                   cbsp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));
               }
           }


        if(grantedPermissions.contains("android.permission.ACCESS_FINE_LOCATION") && grantedPermissions.contains("android.permission.ACCESS_COARSE_LOCATION")){
            cblp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
        }else{
            cblp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));
        }

        if(grantedPermissions.contains("android.permission.READ_PHONE_STATE")){
            cbpp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
        }else{
            cbpp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));
        }


        if(grantedPermissions.contains("android.permission.SEND_SMS")){
            cbsmsp.setImageDrawable(getResources().getDrawable(R.drawable.checkcircle));
        }else{
            cbsmsp.setImageDrawable(getResources().getDrawable(R.drawable.crosscircle));
        }

    }


}