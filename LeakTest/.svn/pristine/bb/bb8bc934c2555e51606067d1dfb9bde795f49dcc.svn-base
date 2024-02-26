package com.cnlaunch.physics.utils.remote;

import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;

import java.util.Arrays;

/**
 * 诊断数据包处理函数
 * Created by xiefeihong on 2018/11/21.
 */

public class DiagnoseDataPackage {
    private static final String TAG = DiagnoseDataPackage.class.getSimpleName();
    public static final byte SPI_TRANS_MODE = 1;                            /* SPI传输模式 */
    public static final byte UART_TRANS_MODE = 0;                            /* 串口传输模式 */
    public static final byte BT_TRANS_MODE = 0;                          /* 蓝牙传输模式 */



/* SPI或者蓝牙传输模式帧头 */

    public static final byte SPI_OR_BT_FRAME_HEAD = (byte) 0xaa;
    /* SPI或者蓝牙传输模式帧尾 */
    public static final byte SPI_OR_BT_FRAME_TAIL = 0x55;


/* 串口传输模式帧头 */

    public static final byte UART_FRAME_HEAD = 0x01;
    /* 串口传输模式帧尾 */
    public static final byte UART_FRAME_TAIL = 0x02;


    /* 命令类型 */
    public static final byte CMD_7816 = 0x00;                             /* 7816命令 */
    public static final byte CMD_MCU = 0x01;                              /* MCU命令 */
    public static final byte CMD_HWCHK = 0x02;                             /* 硬件检测升级命令 */
    public static final byte CMD_STATUS = 0x03;                             /* 状态查询命令 */




    /* 命令模式 */
    public static final byte PLAIN_LRC = 0x00;                               /* 明文传输+LRC */
    public static final byte CIPHER_LRC = 0x01;                           /* 密文传输+LRC */
    public static final byte PLAIN_HMAC = 0x02;                             /* 明文传输+HMAC */
    public static final byte CIPHER_HMAC = 0x03;                            /* 密文传输+HAMC */



    /*安全级别*/
    public static final byte SECURITY_LEVEL_BRIDGES = 0x00;                               /* 透传命令 */
    public static final byte SECURITY_LEVEL_AUTHORIZATION = 0x01;                           /* 授权命令 */
    public static final byte SECURITY_LEVEL_STRICTLY_CONTROL = 0x02;                             /* 严控命令 */


    public static final byte FRAME_EXTRA_LENGTH = 10;                        /* 帧除了数据域的额外长度 */
    private byte head;                                       /* 帧头 */
    private int pduOffset;
    private int pduLength;                                  /* 两个字节代表pdu的长度,高字节在前 */
    private byte type;                                       /* 命令类型 */
    private byte mode;                                       /* 命令模式 */
    private byte inc;                                        /* 单调循环递增计数器 */
    private byte rfu;                                        /* 安全级别 */
    private byte[] pduBytes;                           /* 帧数据单元 frame data unit */
    private int check;                                   /* 两个字节校验值：LRC 或者 HMAC ,高字节在前*/
    private byte tail;                                       /* 帧尾 */
    private boolean isPackageSuccess;

    public DiagnoseDataPackage() {
        pduBytes = null;
        isPackageSuccess = false;
        check = 0;
    }

    public boolean isPackageSuccess() {
        return isPackageSuccess;
    }

    public boolean framePackageForMCU(byte[] cmd_data,int cmd_data_offset,int cmd_data_length,byte cmd_inc){
        return framePackage(cmd_data, cmd_data_offset,cmd_data_length,CMD_MCU, PLAIN_LRC, cmd_inc, SECURITY_LEVEL_BRIDGES);
    }
    /**
     * @param cmd_data:       数据
     * @param cmd_data_length:       数据
     * @param cmd_type:       帧类型
     * @param cmd_mode:       帧模式
     * @param cmd_inc:        帧单调递增循环计数器
     * @param security_level: 安全级别
     * @brief 组帧函数
     * @retval 组帧是否成功
     */
    public boolean framePackage(byte[] cmd_data, int cmd_data_offset,int cmd_data_length,byte cmd_type, byte cmd_mode, byte cmd_inc, byte security_level) {
        if (!isCommandType(cmd_type)) {
            if (MLog.isDebug) {
                MLog.d(TAG, "cmd_type error!");
            }
            isPackageSuccess = false;
            return isPackageSuccess;
        }
        if (!isCommandMode(cmd_mode)) {
            if (MLog.isDebug) {
                MLog.d(TAG, "cmd_type error!");
            }
            isPackageSuccess = false;
            return isPackageSuccess;
        }

        head = SPI_OR_BT_FRAME_HEAD;
        tail = SPI_OR_BT_FRAME_TAIL;
        pduOffset = cmd_data_offset;
        pduLength = cmd_data_length;
        type = cmd_type;
        mode = cmd_mode;
        inc = cmd_inc;
        rfu = security_level;
        pduBytes = cmd_data;
        isPackageSuccess = true;
        return isPackageSuccess;
    }

    @Override
    public String toString() {
        return "DiagnoseDataPackage{" +
                "head=" + head +
                ", pduOffset=" + pduOffset +
                ", pduLength=" + pduLength +
                ", type=" + type +
                ", mode=" + mode +
                ", inc=" + inc +
                ", rfu=" + rfu +
                ", pduBytes=" + ByteHexHelper.bytesToHexStringWithSearchTable(pduBytes,0,pduLength) +
                ", check=" + check +
                ", tail=" + tail +
                ", isPackageSuccess=" + isPackageSuccess +
                '}';
    }

    public byte[] toBytes() {
        if(MLog.isDebug){
            MLog.d(TAG,"DiagnoseDataPackage toString()="+ toString());
        }
        byte[] frame = new byte[FRAME_EXTRA_LENGTH + pduLength];
        int index = 0;
        frame[index++] = head;
        frame[index++] = (byte) ((pduLength >> 8) & 0xFF);
        frame[index++] = (byte) (pduLength & 0xFF);
        frame[index++] = type;
        frame[index++] = mode;
        frame[index++] = inc;
        frame[index++] = rfu;
        System.arraycopy(pduBytes, pduOffset, frame, index, pduLength);
        if (mode == PLAIN_LRC || mode == CIPHER_LRC) {
                /* 计算LRC */
            check = checksumCalculate(frame, 1, pduLength + 6);
        } else {
                /* 计算HMAC */

        }
        index = index + pduLength;
        frame[index++] = (byte) ((check >> 8) & 0xFF);
        frame[index++] = (byte) (check & 0xFF);
        frame[index] = tail;
        if(MLog.isDebug){
            MLog.d(TAG,"DiagnoseDataPackage toBytes="+ ByteHexHelper.bytesToHexString(frame));
        }
        return frame;
    }

    /**
     *  命令类型参数检查
     */
    boolean isCommandType(byte commandType) {
        return ((commandType == CMD_7816) || (commandType == CMD_MCU) ||
                (commandType == CMD_HWCHK) || (commandType == CMD_STATUS));
    }
    /**
     *  命令模式参数检查
     */
    boolean isCommandMode(byte mode) {
        return ((mode == PLAIN_LRC) || (mode == CIPHER_LRC) ||
                (mode == PLAIN_HMAC) || (mode == CIPHER_HMAC));
    }
    /**
     * @param src:需要验证的数据
     * @brief checksumCalculate, 获取校验码
     * @retval 生成的校验码
     */
    public static int checksumCalculate(byte[] src, int start, int length) {
        int checksum = 0;
        for (int i = start; i < start + length; i++) {
            checksum += src[i];
        }
        return checksum;
    }
}
