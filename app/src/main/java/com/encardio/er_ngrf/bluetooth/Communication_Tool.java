package com.encardio.er_ngrf.bluetooth;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.encardio.er_ngrf.tool.Constant;
import com.encardio.er_ngrf.tool.Tool;
import com.encardio.er_ngrf.tool.Variable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * @author Sandeep
 */

public class Communication_Tool extends Application {


    public static final String BT_DEVICE_NAME = "ESCL";
    public static final String DATALOGGER_MODEL = "ER-NGRF";
    public static final String DEFAULT_TEXT = "-----";
    public static final int SHOW_MSG = 1;
    public static final int SHOW_PARAMETER = 2;
    public static final String SHOW_MSG_KEY = "SHOW_MSG";

    public static final int INVALID_COMMAND_ERROR = 1;
    public static final int STRING_FORMAT_ERROR = 2;
    public static final int DATA_SIZE_ERROR = 3;
    public static final int COMMAND_PACKET_SIZE_ERROR = 4;
    public static final int DATA_NULL_ERROR = 5;
    public static final int RTC_DATE_TIME_ERROR = 6;
    public static final int INVALID_DATA_ERROR = 7;
    public static final int MODEM_SIGNAL_ERROR = 8;
    public static final int MODEM_BAUDRATE_SET_ERROR = 9;
    public static final int LOG_INTERVAL_UNDERFLOW_ERROR = 10;
    public static final int DATA_OUT_OF_RANGE_ERROR = 11;
    public static final int DATA_RECORD_CORRUPTED_ERROR = 12;
    public static final int MODEM_TRAP_MODE_ERROR = 13;
    public static final int MODEM_POWER_ON_ERROR = 14;
    public static final int FTP_PARAMETER_NOT_SET = 15;
    public static final int UNOPEN_FTP_SOCKET = 16;
    public static final int FTP_DATA_FAIL = 17;

    public static final int MODEM_CME_MSG_FORMAT_ERROR = 50;
    public static final int NETWORK_FORMAT_ERROR = 51;
    public static final int MODEM_FLOW_CONTROL_ERROR = 52;
    public static final int MODEM_CHAR_SET_ERROR = 53;
    public static final int MODEM_MSG_SERVICE_ERROR = 54;
    public static final int SMS_OVER_FLOW_ERROR = 55;
    public static final int SMS_STORAGE_ERROR = 56;
    public static final int SMS_MSG_FORMAT_ERROR = 57;
    public static final int SMS_TEXT_PARA_ERROR = 58;
    public static final int SAVE_SETTINGS_ERROR = 59;

    public static final int OK_STATUS = 1000;
    public static final int MODEM_DISABLE_STATUS = 1001;
    public static final int MODEM_SIM_UNAVAILABLE_STATUS = 1002;
    public static final int MODEM_OPERATING_MODE_OFF_STATUS = 1003;
    public static final int BAROMETER_DISABLE_STATUS = 1004;
    public static final int BATTERY_DEAD_STATUS = 1005;
    public static final int MODEM_COMM_MODE_GPRS_STATUS = 1006;
    public static final int MODEM_OPERATING_MODE_ON_STATUS = 1007;
    public static final int RECORD_AVAILABLE_STATUS = 1010;
    public static final int RECORD_DOWNLOADING_COMPLETED_STATUS = 1011;
    public static final int RECORD_UNAVAILABLE_STATUS = 1012;


    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static boolean connectionBreak = false;
    public static int countRecords;
    public static int tempcount;
    public static int progress = 0;
    public static String tempDownloadedData = "";
    public static Integer tag = 0;
    public static Integer tag1 = 0;

    public static float graph_max;
    public static float graph_min;

    public static String minDate;
    public static String maxDate;
    public static String Date_min;
    public static String Date_max;
    @SuppressLint("StaticFieldLeak")
    public static BluetoothService bluetoothService;
    public static String BLUETOOTH_ADDRESS = "";
    public static String BLUETOOTH_DEVICE = "";
    public static boolean isNewBluetoothConnection = false;
    public static String BLUETOOTH_ADDRESS_FROM_LIST;
    public static int monitorInterval = 2;
    public static String endWithNewLine = "\r";
    public static String checkEndChar = endWithNewLine;
    public static boolean exception = false;
    public static boolean toastFromThread;
    public static long modemTurnOFFTimer = 0;

    public static boolean isBTenabledByApp = false;
    public static boolean expInBluetoothConnection;
    public static String reply = "";
    public static String parameterUnit = "None";
    public static boolean headerstat = false;
    public static String head_para = "Parameter";
    public static int sc = -1;
    public static String downloadData = "";
    public static String responseMsg = "";
    public static boolean downloadingDataCommand = false;
    public static boolean dataDownloadAvailble = false;
    public static boolean dataDownloadCompleted = false;
    public static boolean dataDownloadUnavailble = false;
    public static long download_Watchdog_Timer = 0;
    public static boolean mdmPwr = false;
    public static boolean mdmstatus = false;
    public static String file_Text;
    public static int graph_length;
    public static String[] graph_date_time;
    public static float[] graph_data;
    public static boolean battery_low = false;

    public static void delay(long time) {
        long curmillies = System.currentTimeMillis();
        while ((System.currentTimeMillis() - curmillies) < time) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
        }
    }

    public static void waitForReply() {
        long currentTimemill = System.currentTimeMillis();
        while (!Variable.gotReply) {
            if (System.currentTimeMillis() - currentTimemill >= 10000) {
                Communication_Tool.toastFromThread = true;
                Communication_Tool.connectionBreak = true;
                break;
            }
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
        }
    }

    public static void waitForReplyforMonPara() {
        long currentTimemill = System.currentTimeMillis();
        while (!Variable.gotReply) {
            if (System.currentTimeMillis() - currentTimemill >= 70000) {
                Communication_Tool.toastFromThread = true;
                Communication_Tool.connectionBreak = true;
                break;
            }
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
        }
    }

    public static String setDecimalDigits(String paraValue_para1, int decimalPlaces) {
        try {
            float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else {
                if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "f", paraValue);
                } else
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "E", paraValue);
            }
            if (Float.isNaN(paraValue)) {
                Variable.isFileFormatCorrupted = true;
            }
        } catch (NumberFormatException e) {
            Variable.isFileFormatCorrupted = true;
        }
        return paraValue_para1;
    }

    public static String setDecimalDigits(String paraValue_para1) {
        try {
            float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) {
                paraValue_para1 = String.format(Locale.US, "%1." + 4 + "f",
                        paraValue);
            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) {
                paraValue_para1 = String.format(Locale.US, "%1." + 4 + "f",
                        paraValue);
            } else {
                if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + 4 + "f", paraValue);
                } else
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + 4 + "E", paraValue);
            }
            if (Float.isNaN(paraValue)) {
                Variable.isFileFormatCorrupted = true;
            }
        } catch (NumberFormatException e) {
            Variable.isFileFormatCorrupted = true;
            e.printStackTrace();
        }
        return paraValue_para1;
    }


    public static boolean validateFTP_Password(String str) {

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            return (c >= '0' && c <= '9') || ((c >= 'a' && c <= 'z')) || ((c >= 'A' && c <= 'Z'));
        }
        return false;
    }

    public static boolean validateFTP_URL(String str) {

        for (int i = 0; i < str.trim().length(); i++) {
            char c = str.charAt(i);
            return (c >= '0' && c <= '9') || ((c == '.'));
        }
        return false;
    }

    public void toast(final Context context, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public String sendCMDgetRLY(String command) {
        try {
            sendCommand(command);
            waitForReply();
            if (Variable.gotReply) {
                return Communication_Tool.reply;
            } else {
                Variable.error_msg = "Connection break !!   Datalogger not replying";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return " ";
    }

    public String sendCMDgetRLYforMonPara(String command) {
        try {
            sendCommand(command);
            waitForReplyforMonPara();
            if (Variable.gotReply) {
                return Communication_Tool.reply;
            } else {
                Variable.error_msg = "Connection break !!   Datalogger not replying";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return " ";
    }

    public String sendCMDgetFullRLY(String command) {
        try {
            sendCommand(command);
            waitForReply();
            if (Variable.gotReply) {
                String replyMsg;
                replyMsg = Communication_Tool.reply;
                replyMsg = replyMsg.trim();

                return replyMsg;
            } else {
                Variable.error_msg = "Connection break !!   Datalogger not replying";
            }
        } catch (Exception ignored) {
        }

        return " ";
    }

    public boolean scanStart_Stop(String status) {
        try {
            wakeUpDL();
            sendCMDgetFullRLY("SCAN,\"" + status + "\"");
            if (Communication_Tool.OK_STATUS == Communication_Tool.sc) {
                Variable.scanStatus = !status.equalsIgnoreCase("STOP");
                generateFile();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void sendCommand(String msg) {
        try {
            sc = 9999;
            Variable.gotReply = false;
            msg = "$" + msg;
            Log.e("", "CMD : " + msg);
            send(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        try {
            char postchar_CR, postchar_LF;
            postchar_CR = 0x0D;
            postchar_LF = 0x0A;
            StringBuilder commandPass = new StringBuilder();
            commandPass.append(msg);
            commandPass.append(postchar_CR);
            commandPass.append(postchar_LF);

            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }

            byte[] send = (commandPass.toString()).getBytes();
            bluetoothService.write(send);
            commandPass.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean wakeUpDL() {
        try {
            send("\0\0\0\0\0");
            long curmillies = System.currentTimeMillis();
            while ((System.currentTimeMillis() - curmillies) < 300) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void generateFile() {

        String fileName = Tool.getSETUPINF_FileName();
        if (!fileName.equals("")) {

            dataDownloadCompleted = false;
            Communication_Tool.downloadData = "";

            Communication_Tool.tempDownloadedData = "";
            Tool.removeDQ(sendCMDgetRLY("SETUPINF,\"?\""));

            Communication_Tool.download_Watchdog_Timer = System.currentTimeMillis();
            while (!dataDownloadCompleted) { // wait till download complete
                if ((System.currentTimeMillis() - download_Watchdog_Timer) > 5000) {

                    Communication_Tool.expInBluetoothConnection = true;
                    String error_msg = "Data couldn't be downloaded from datalogger due to connection error...";
                    break;
                }
            }
            if (dataDownloadCompleted) {

                // download = Constants.downloadData;

                if (downloadData != null && downloadData.length() > 2) {


                    File configFile = new File(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CONFIG_FOLDER), fileName);
                    try {
                        FileWriter writer = new FileWriter(configFile);
                        configFile.createNewFile();
                        PrintWriter pw = new PrintWriter(writer);
                        // Write to file for the first row
                        pw.print(downloadData);
                        // Flush the output to the file
                        pw.flush();
                        // Close the Print Writer
                        pw.close();
                        // Close the File Writer
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
