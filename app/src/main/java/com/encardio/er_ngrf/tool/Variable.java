package com.encardio.er_ngrf.tool;

import com.encardio.er_ngrf.bluetooth.BluetoothService;

public class Variable {


    public static boolean is_device_type_node;

    public static boolean isConnected = false;
    public static boolean gotReply = false;
    public static boolean scanStatus = false;
    public static String tmp_str = "";
    public static boolean isFileFormatCorrupted = false;

    public static String header_date_time = "Date / Time";
    public static String header_frequency = "Frequency(Hz)";
    public static String header_parameter = "Parameter";
    public static String header_temperature = "Temperature";
    public static String header_battery_voltage = "Battery Voltage (V)";

    public static String error_msg = "";

    public static String logger_date = "";
    public static String logger_time = "";
    public static String logger_utc = "";

    public static String logger_Model = "";
    public static String logger_SerialNumber = "";
    public static String logger_ID = "";

    public static String logger_Location = "";
    public static String loggerTopElevation = "";
    public static String loggerFwVer = "";
    public static String loggerFwRevDate = "";

    public static String loggerLogInterval = "";
    public static String loggerScanStartTime = "";
    public static String modem_imei = "";

    public static String loggerSensorModel = "";
    public static String loggerSensorSerialNumber = "";
    public static String loggerMeasuringRange = "";
    public static String temperatureUnit = "";

    public static String loggerSamplesAveraged = "";

    public static String batteryType = "";
    public static String batteryVoltage = "";
    public static String batteryInstallationDate = "";

    public static String logInterval = "";
    public static String scanStartTime = "";
    public static String memoryFullAction = "";
    public static String paraErrorOption = "";
    public static String paraErrorValue = "";
    public static String tempErrorValue = "";
    public static String totalNumberOfRecord = "";
    public static String totalNumberOfRecordFromLastDownload = "";
    public static String totalNumberOfRecordFromLastUpload = "";


    public static int dp_vw_sensor_reading = 3;
    public static int dp_temperature_sensor_reading = 1;
    public static float noise_bar_limit_vw_reading = 10.0f;
    public static float noise_bar_limit_temperature_reading = 10.0f;

    public static boolean isSMS_AlertEnable = false;
    public static int flagBlePr=0;
    public static BluetoothService.AcceptThread mAcceptThread;
    public static BluetoothService.ConnectThread mConnectThread;
    public static BluetoothService.ConnectedThread mConnectedThread;
}
