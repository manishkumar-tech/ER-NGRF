package com.encardio.er_ngrf.tool;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.encardio.er_ngrf.bluetooth.Communication_Tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Tool {

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

    public static void create_data_folder() {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CSV_FOLDER + Constant.ARCHIVE_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CSV_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CONFIG_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

        } catch (Exception ignored) {
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getSETUPINF_FileName() {
        String fileName = "";
        try {
            fileName = Variable.logger_ID + "_" + Variable.logger_SerialNumber + "_"
                    + new SimpleDateFormat("yyMMdd_HHmmss").format(new java.util.Date())
                    + "_info" + ".txt";
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
            return fileName;
        }
    }

    public static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + c;
    }


    public static String pad(String number, int digit) {
        while (number.length() < digit) {
            number = "0" + number;
        }
        return number;
    }

    public static int HHHMMSSTosecond(String time) {
        int hour;
        int min;
        int sec;

        String temp = time;
        String tempTime = "";
        tempTime = temp.substring(0, temp.indexOf(":"));
        hour = Integer.parseInt(tempTime);
        temp = temp.substring(temp.indexOf(":") + 1, temp.length());
        tempTime = temp.substring(0, temp.indexOf(":"));
        min = Integer.parseInt(tempTime);
        tempTime = temp.substring(temp.indexOf(":") + 1, temp.length());
        sec = Integer.parseInt(tempTime);
        sec = hour * 60 * 60 + (min * 60 + sec);
        return sec;
    }

    public static String getUTC_Offset() {
        String sign;
        long offset = Calendar.getInstance().getTimeZone().getRawOffset();
        if (offset < 0) {
            sign = "-";
            offset = offset * -1;
        } else {
            sign = "+";
        }
        offset = offset / 1000;
        int hour = (int) offset / 3600;
        offset = offset % 3600;
        int min = (int) offset / 60;
        return sign + pad(hour) + ":" + pad(min);
    }

    public static String setDecimalDigits(String paraValue_para1, int decimalPlaces) {
        try {
            Float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f", paraValue);
            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f", paraValue);
            } else if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f", paraValue);
            } else {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "E", paraValue);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return paraValue_para1;
    }

    public static String setDecimalDigitsWithoutE(String paraValue_para1, int decimalPlaces) {
        try {
            Float paraValue = Float.parseFloat(paraValue_para1);
            paraValue_para1 = String.format(Locale.US, "%." + decimalPlaces + "f", paraValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return paraValue_para1;
    }

    // mis-clicking prevention, using threshold of 2000 ms
    public static void controlButtonDebouncing(final Button btn) {
        btn.setClickable(false);
        new Handler().postDelayed(() -> btn.setClickable(true), 2000);
    }

    public static void controlTextViewDebouncing(final TextView tv) {
        tv.setClickable(false);
        new Handler().postDelayed(() -> tv.setClickable(true), 2000);
    }

    public static void controlImageViewDebouncing(final ImageView iv) {
        iv.setClickable(false);
        new Handler().postDelayed(() -> iv.setClickable(true), 2000);
    }

    public static String removeDQ(String text) {
        if ((text != null)) {
            if (text.contains("\"")) {
                text = text.substring(1, text.length() - 1).trim();
            }
            return text;
        }
        return text;
    }

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

    public static void send(String msg) {
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
            Communication_Tool.bluetoothService.write(send);
            commandPass.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean wakeUpDL() {
        try {
            send("\0\0\0\0\0");
            long curmillies = System.currentTimeMillis();
            while ((System.currentTimeMillis() - curmillies) < 300) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String setDecimalDigitsWithE(String paraValue_para1, int decimalPlaces) {
        try {
            Float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else {
                if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) // xxxx.xxxx
                {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "f", paraValue);
                } else {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "E", paraValue);
                }
            }

        } catch (NumberFormatException e) {

            e.printStackTrace();
        }
        return paraValue_para1;
    }

    public void toast(final Context context, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
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

    public void sendCommand(String msg) {
        try {
            Communication_Tool.sc = 9999;
            Variable.gotReply = false;
            msg = "$" + msg;
            Log.e("", "CMD : " + msg);
            send(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] read_file(String file) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder data = new StringBuilder();
            String tempStr = "";
            while ((tempStr = br.readLine()) != null) {
                data.append(tempStr.trim()).append("\n");
            }
            return data.toString().split("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void generateFile() {
        File dir = null;
        String fileName = Tool.getSETUPINF_FileName();
        if (!fileName.equals("")) {

            Communication_Tool.dataDownloadCompleted = false;
            Communication_Tool.downloadData = "";

            Communication_Tool.tempDownloadedData = "";
            removeDQ(sendCMDgetRLY("SETUPINF,\"?\""));

            Communication_Tool.download_Watchdog_Timer = System.currentTimeMillis();
            while (!Communication_Tool.dataDownloadCompleted) { // wait till download complete
                if ((System.currentTimeMillis() - Communication_Tool.download_Watchdog_Timer) > 5000) {

                    Communication_Tool.expInBluetoothConnection = true;
                    String error_msg = "Data couldn't be downloaded from datalogger due to connection error...";
                    break;
                }
            }
            if (Communication_Tool.dataDownloadCompleted) {

                // download = Constants.downloadData;

                if (Communication_Tool.downloadData != null && Communication_Tool.downloadData.length() > 2) {


                    File configFile = new File(dir, fileName);
                    try {
                        FileWriter writer = new FileWriter(configFile);
                        configFile.createNewFile();
                        PrintWriter pw = new PrintWriter(writer);
                        // Write to file for the first row
                        pw.print(Communication_Tool.downloadData);
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
