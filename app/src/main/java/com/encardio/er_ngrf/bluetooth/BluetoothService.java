package com.encardio.er_ngrf.bluetooth;

import static com.encardio.er_ngrf.tool.Variable.mAcceptThread;
import static com.encardio.er_ngrf.tool.Variable.mConnectedThread;
import static com.encardio.er_ngrf.tool.Variable.mConnectThread;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.encardio.er_ngrf.tool.Variable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/*
 * @author Sandeep
 */
public class BluetoothService extends Service {

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    private static final String NAME = "BluetoothAdpt";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static int mState;
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    Context context;


    public BluetoothService(Context context, Handler handler
    ) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        this.context = context;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void setState(int state) {

        mState = state;
        mHandler.obtainMessage(BluetoothAdpt.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    public synchronized void start() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
           mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device) {

        try {
            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING) {
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }
            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }
            // Start the thread to connect with the given device
            try {
                mConnectThread = new ConnectThread(device);
                mConnectThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setState(STATE_CONNECTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Cancel the accept thread because we only want to connect to one
        // device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothAdpt.DEVICE_NAME, device.getName());
        bundle.putBoolean("DEVICE_CONNECTING", true);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }

    /**
     * Stop all threads.
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        try {
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (mState != STATE_CONNECTED)
                    return;
                r = mConnectedThread;
            }
            // Perform the write unsynchronized
            r.write(out);
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            // bundle.putBoolean("CONNECTION_FAILED", true);
            bundle.putString(BluetoothAdpt.TOAST,
                    "Bluetooth Communication Failed...");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
        // Constants.bluetoothService = null;
        Communication_Tool.BLUETOOTH_ADDRESS = null;
        Communication_Tool.BLUETOOTH_DEVICE = null;
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putBoolean("CONNECTION_FAILED", true);
        bundle.putString(BluetoothAdpt.TOAST, "Unable to connect to device...");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        Variable.isConnected = false;
        setState(STATE_LISTEN);
        Communication_Tool.bluetoothService = null;
        Communication_Tool.BLUETOOTH_ADDRESS = null;
        Communication_Tool.BLUETOOTH_DEVICE = null;
        Communication_Tool.isNewBluetoothConnection = false;
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Bluetooth connection lost...");
        bundle.putBoolean("CONNECTION_LOST", true);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    public class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }


        public void run() {

            setName("AcceptThread");
            BluetoothSocket socket;
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {

                try {

                    if (mmServerSocket != null) {
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket.accept();
                    } else
                        break;
                } catch (IOException e) {

                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                                Communication_Tool.bluetoothService = null;
                                Communication_Tool.BLUETOOTH_ADDRESS = null;
                                Communication_Tool.BLUETOOTH_DEVICE = null;
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }

        }


        public void cancel() {

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }


        public void run() {

            setName("ConnectThread");

            mAdapter.cancelDiscovery();

            try {

                mmSocket.connect();

            } catch (IOException e) {
                e.printStackTrace();
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                    BluetoothService.this.start();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }

                return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;

        private final InputStream mmInStream;

        private final OutputStream mmOutStream;

        public int tempcount = 0;
        boolean replyReceived = false;
        String temp;

        boolean isCompleteMsgRcvd = false;

        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            StringBuilder replyMsgBuffer = new StringBuilder();

            String replyMsg;
            int len;


            try {
                len = mmInStream.read(buffer);

                while (((len > -1)) || (isCompleteMsgRcvd)) {

                    temp = "";
                    if (!isCompleteMsgRcvd) {
                        replyMsg = new String(buffer, 0, len);

                        replyMsgBuffer.append(replyMsg);
                    }
                    isCompleteMsgRcvd = replyMsgBuffer.toString().contains(
                            Communication_Tool.checkEndChar);
                    if (isCompleteMsgRcvd) {
                        len = -1;
                        String tempReply;
                        tempReply = replyMsgBuffer
                                .substring(
                                        0,
                                        replyMsgBuffer
                                                .indexOf(Communication_Tool.checkEndChar));

                        temp = replyMsgBuffer.substring(replyMsgBuffer
                                        .indexOf(Communication_Tool.checkEndChar) + 1,
                                replyMsgBuffer.length());
                        if (tempReply.contains("$")) {
                            Communication_Tool.sc = Integer
                                    .parseInt(tempReply.substring(
                                            tempReply.indexOf("$") + 1,
                                            tempReply.indexOf("$") + 5));
                            Log.d("", "STC : " + Communication_Tool.sc);

                            if (Communication_Tool.sc == Communication_Tool.RECORD_AVAILABLE_STATUS) {
                                Communication_Tool.countRecords++;
                                Communication_Tool.download_Watchdog_Timer = System
                                        .currentTimeMillis();
                            }

                            Communication_Tool.reply = "";
                            if ((tempReply.substring(
                                    tempReply.indexOf("$") + 1).length()) > 4) {

                                tempReply = tempReply.replace("\n", "")
                                        .trim();

                                Communication_Tool.reply = (tempReply.substring(
                                        tempReply.indexOf("$") + 6)).trim();

                                Log.d("", "RLY : " + Communication_Tool.reply);

                                if (Communication_Tool.sc == Communication_Tool.RECORD_AVAILABLE_STATUS) {
                                    char postchar_CR, postchar_LF;
                                    postchar_CR = 0x0D;
                                    postchar_LF = 0x0A;
                                    Communication_Tool.reply = (Communication_Tool.reply
                                            + postchar_CR + postchar_LF);
                                    Communication_Tool.dataDownloadAvailble = true;
                                    tempcount++;
                                    if (tempcount >= 1000) {
                                        Communication_Tool.downloadData = Communication_Tool.downloadData
                                                + Communication_Tool.tempDownloadedData + Communication_Tool.reply;
                                        tempcount = 0;
                                        Communication_Tool.tempDownloadedData = "";
                                    } else {
                                        Communication_Tool.tempDownloadedData = Communication_Tool.tempDownloadedData
                                                + Communication_Tool.reply;
                                    }
                                }
                            }

                            if (Communication_Tool.sc == Communication_Tool.RECORD_DOWNLOADING_COMPLETED_STATUS) {
                                Communication_Tool.downloadData = Communication_Tool.downloadData
                                        + Communication_Tool.tempDownloadedData;
                            }


                            if (Communication_Tool.sc == Communication_Tool.RECORD_AVAILABLE_STATUS) {
                                Communication_Tool.dataDownloadAvailble = true;
                            }
                            if (Communication_Tool.sc == Communication_Tool.RECORD_DOWNLOADING_COMPLETED_STATUS) {
                                Communication_Tool.dataDownloadCompleted = true;
                                Communication_Tool.sc = Communication_Tool.OK_STATUS;
                            }
                            if (Communication_Tool.sc == Communication_Tool.RECORD_UNAVAILABLE_STATUS) {
                                Communication_Tool.dataDownloadUnavailble = true;
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Record not found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.INVALID_COMMAND_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Invalid command found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.STRING_FORMAT_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Invalid String format found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.DATA_SIZE_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Invalid data size found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.COMMAND_PACKET_SIZE_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Invalid packet size found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.DATA_NULL_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Data size found NULL found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.RTC_DATE_TIME_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": RTC error found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.INVALID_DATA_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + " :Invalid data found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_SIGNAL_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Unable to read modem signal !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_BAUDRATE_SET_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + "Unable to set modem baud rate !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.LOG_INTERVAL_UNDERFLOW_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Log interval less than 5 sec !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.DATA_OUT_OF_RANGE_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Data value out of range !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.DATA_RECORD_CORRUPTED_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Correpted record found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_TRAP_MODE_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Unable to change Modem trap mode !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_POWER_ON_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Unable to turn on Modem power !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_CME_MSG_FORMAT_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT+CMEE=1 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.NETWORK_FORMAT_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + " Modem command AT+CREG=1 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_FLOW_CONTROL_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT\\Q3 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_CHAR_SET_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": modem command AT+CSCS=\"GSM\" !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_MSG_SERVICE_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT+CSMS=1 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.SMS_OVER_FLOW_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT^SMGO=1 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.SMS_STORAGE_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT+CPMS=\"MT\",\"MT\",\"MT\" !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.SMS_MSG_FORMAT_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT+CMGF=1 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.SMS_TEXT_PARA_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT+CSDH=1 !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.SAVE_SETTINGS_ERROR) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem command AT&W !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_DISABLE_STATUS) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem not found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_SIM_UNAVAILABLE_STATUS) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Sim card not found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_OPERATING_MODE_OFF_STATUS) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem operating mode OFF !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.BAROMETER_DISABLE_STATUS) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Barometer not found !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.BATTERY_DEAD_STATUS) {
                                Communication_Tool.battery_low = true;
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Battery voltage low !! Replace battery immediately";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_COMM_MODE_GPRS_STATUS) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Datalogger is busy in uploading data to Remote FTP server. Please retry after few minutes!!";
                                toastToast(context, "" + Communication_Tool.responseMsg);
                            }
                            if (Communication_Tool.sc == Communication_Tool.MODEM_OPERATING_MODE_ON_STATUS) {
                                Communication_Tool.responseMsg = Communication_Tool.sc + ": Modem operating mode ON !!";
                                toastToast(context, "" + Communication_Tool.responseMsg);

                            }
                            Variable.gotReply = true;
                            replyReceived = true;
                        }
                        replyMsgBuffer.delete(0,
                                replyMsgBuffer.length());
                        replyMsgBuffer.setLength(0);
                        replyMsgBuffer.append(temp);

                    } else {
                        len = mmInStream.read(buffer);
                    }
                }
            } catch (Exception e) {
                Variable.gotReply = false;
                try {
                    connectionLost();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                BluetoothService.this.start();
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(BluetoothAdpt.MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void toastToast(final Context context, final String text) {
            try {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception ignored) {
            }

        }
    }
}
