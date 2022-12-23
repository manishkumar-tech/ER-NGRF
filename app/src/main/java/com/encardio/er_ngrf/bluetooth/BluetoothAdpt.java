package com.encardio.er_ngrf.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.er_ngrf.activity.DeviceListActivity;
import com.encardio.er_ngrf.activity.R;

/**
 * @author Sandeep
 */
public class BluetoothAdpt extends AppCompatActivity {
    // Debugging
    /**
     * Message types sent from the BluetoothService Handler.
     */
    public static final int MESSAGE_STATE_CHANGE = 1;
    /**
     * The Constant MESSAGE_WRITE.
     */
    public static final int MESSAGE_WRITE = 3;
    /**
     * The Constant MESSAGE_DEVICE_NAME.
     */
    public static final int MESSAGE_DEVICE_NAME = 4;
    /**
     * The Constant MESSAGE_TOAST.
     */
    public static final int MESSAGE_TOAST = 5;
    /**
     * Key names received from the BluetoothService Handler
     */
    public static final String DEVICE_NAME = "device_name";
    /**
     * The Constant TOAST.
     */
    public static final String TOAST = "toast";
    /**
     * The Constant REQUEST_CONNECT_DEVICE.
     */
    public static final int REQUEST_CONNECT_DEVICE = 1;
    /**
     * The Constant TAG.
     */
    private static final String TAG = "BluetoothChat";
    // Intent request codes
    /**
     * The Constant D.
     */
    private static final boolean D = true;
    /**
     * The Constant REQUEST_ENABLE_BT.
     */
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available...", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)

            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            }  /*if(Constants.bluetoothService == null)
				setupChat();*/
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)


            if (Communication_Tool.bluetoothService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't
                // started already
                if (Communication_Tool.bluetoothService.getState() == BluetoothService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    Communication_Tool.bluetoothService.start();
                }
            }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (Communication_Tool.bluetoothService != null)
            Communication_Tool.bluetoothService.stop();

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    try {
                        Communication_Tool.bluetoothService.connect(device);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    // finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return false;
    }
}