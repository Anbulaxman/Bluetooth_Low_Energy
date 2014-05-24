package com.anbu.sensors.util;

import java.util.UUID;

public class Constant {

	public static final String DEVICE_NAME = "SP-10C";

    /* Sensor Service */
    public static final UUID SENSOR_SERVICE = UUID.fromString("01000000-0000-0000-0000-000000000080");
    /* Sensor characteristic */
    public static final UUID SENSOR_DATA_CHAR = UUID.fromString("02000000-0000-0000-0000-000000000080");
    public static final UUID SENSOR_CONFIG_CHAR = UUID.fromString("04000000-0000-0000-0000-000000000080");
    
    public static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    
    /*---- Status Commands ----*/
    public static final byte  BLE_CMD_STATUS=0x30; 
    public static final byte  BLE_CMD_VERSION = 0x34; 
    public static final byte  BLE_CMD_GETCONFIG = 0x35; 

    /*---- Data Logging Commands ----*/
    public static final byte  BLE_CMD_LOGGETSTATUS = 0x58; 
    public static final byte  BLE_CMD_LOGCLEAR = 0x59; 
    public static final byte  BLE_CMD_LOGFIRSTGETRECORD = 0x5a;
    public static final byte  BLE_CMD_LOGGETRECORD = 0x5b;  // 2nd byte: record num (256 byte), modulo 256
    public static final byte  BLE_CMD_LOGGETCONFIG = 0x5c;
    public static final byte  BLE_CMD_LOGENABLE = 0x5e;  // 2nd byte: 0=disable, 1=enable

    /*---- Misc Commands ----*/
    public static final byte  BLE_CMD_SETLED = (byte) 0x80; // 2nd byte: bit0=Green, bit1=Red, bit7=1 to restore internal LED control
    public static final byte  BLE_CMD_LEDGREEN = 0x01;
    public static final byte  BLE_CMD_LEDRED = 0x02;
    public static final byte  BLE_CMD_LEDSYSCONTROL = (byte) 0x80;

    public static final byte  BLE_CMD_SETRTC = (byte) 0x82; // 2nd byte: TBD
    public static final byte  BLE_CMD_GETRTC = (byte) 0x83;
    public static final byte  BLE_CMD_GET_PRESSURE = (byte) 0x86;
    public static final byte  BLE_CMD_GET_TEMPERATURE = (byte) 0x87;


    /*---- Status Commands ----*/
    public static final byte  PDI_CMD_STATUS = 0x30; 
    public static final byte  PDI_CMD_VERSION = 0x34; 
    public static final byte  PDI_CMD_CONFIG = 0x35; 

    /*---- Data Logging Commands ----*/
    public static final byte  PDI_CMD_LOGSTATUS = 0x58; 
    public static final byte  PDI_CMD_LOGRECORD = 0x5a; 
    public static final byte  PDI_CMD_LOGCONFIG = 0x5b; 
    public static final byte  PDI_CMD_LOGGETCONFIG = 0x5c;


    /*---- Misc Commands ----*/
    public static final byte  PDI_CMD_RTC = (byte) 0x83; 
    public static final byte  PDI_CMD_PRESSURE = (byte) 0x86; 
    public static final byte  PDI_CMD_TEMPERATURE = (byte) 0x87;


    /*---- Packet Interface ----*/
    public static final byte  PDI_START_OF_PACKET = (byte) 0xD1;
    public static final byte  PDI_END_OF_PACKET = (byte) 0xDF;
    public static final byte  PDI_BYTE_STUFFING = (byte) 0xDE;

    /*---- Data Streaming Commands ----*/
    public static final byte  BLE_CMD_STREAMGETCONFIG = 0x61;
    public static final byte  BLE_CMD_STREAMSETCONFIG = 0x62;
    public static final byte  BLE_CMD_STREAMENABLE = 0x63;

    /*---- Data Streaming Commands ----*/
    public static final byte PDI_CMD_STREAMRECORD =	0x60;
    public static final byte PDI_CMD_STREAMGETCONFIG = 0x61;
    public static final byte PDI_CMD_STREAMSETCONFIG = 0x62;
    public static final byte PDI_CMD_STREAMENABLE = 0x63;
    
    public enum SensoPlexLEDState {LEDSystemControl, LEDGreen, LEDRed};
    
    // constants for the sensor data options
    public static final short LOGDATA_TIMEDATE       = 0x0001;            // 1
    public static final short LOGDATA_TIMESTAMP      = 0x0002;            // 2
    public static final short LOGDATA_BATTERYVOLTS   = 0x0004;            // 3
    public static final short LOGDATA_BLESTATE       = 0x0008;            // 4
    public static final short LOGDATA_GYROS          = 0x0010;            // 5
    public static final short LOGDATA_ACCELS         = 0x0020;            // 6
    public static final short LOGDATA_QUATERNION     = 0x0040;            // 7
    public static final short LOGDATA_COMPASS        = 0x0080;            // 8
    public static final short LOGDATA_PRESSURE       = 0x0100;            // 9
    public static final short LOGDATA_TEMPERATURE    = 0x0200;            // 10
    public static final short LOGDATA_LINEARACCEL    = 0x0400;            // 11
    public static final short LOGDATA_EULER          = 0x0800;            // 12
    public static final short LOGDATA_RSSI           = 0x1000;            // 13
    public static final short LOGDATA_ROTMATRIX      = 0x2000;            // 14
    public static final short LOGDATA_HEADING        = 0x4000;            // 15

}
