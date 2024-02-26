package com.cnlaunch.physics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;
import com.power.baseproject.utils.EasyPreferences;
import com.power.baseproject.utils.SystemPropertiesInvoke;
import com.power.baseproject.widget.NToast;

/**
 * 处理pad3 支持DoIP协议引入的DHCP机制用户提醒
 *
 * @author xiefeihong
 */

public class PAD3DHCPForDoIP {
    public final static String TAG = PAD3DHCPForDoIP.class.getSimpleName();

    /**
     * DHCP服务状态广播
     */

    public final static String PAD3_DHCP_START_SERVICE_STATUS_ACTION_FOR_DOIP = "com.bsk.broadcast.eth.start.service.status";
    public final static String PAD3_DHCP_STOP_SERVICE_STATUS_ACTION_FOR_DOIP = "com.bsk.broadcast.eth.stop.service.status";
    /**
     * DHCP服务状态广播状态参数
     */
    public final static String PAD3_DHCP_START_SERVICE_STATUS_ACTION_PARAMETER_FOR_DOIP = "startEthStatus";
    public final static String PAD3_DHCP_STOP_SERVICE_STATUS_ACTION_PARAMETER_FOR_DOIP = "stopEthStatus";
    /**
     * DHCP服务状态广播状态参数值
     */
    public final static String PAD3_DHCP_SERVICE_STATUS_ACTION_PARAMETER_FAIL_VALUE_FOR_DOIP = "fail";
    public final static String PAD3_DHCP_SERVICE_STATUS_ACTION_PARAMETER_SUCCESS_VALUE_FOR_DOIP = "success";
    /**
     * 网线状态侦测广播
     */
    public final static String PAD3_DHCP_CABLE_ACTION_FOR_DOIP = "com.bsk.broadcast.eth.cable.status";
    /**
     * 网线状态侦测广播参数
     */
    public final static String PAD3_DHCP_CABLE_ACTION_PARAMETER_FOR_DOIP = "cableStatus";
    /**
     * 网线状态侦测广播参数值
     */
    public final static String PAD3_DHCP_CABLE_ACTION_PARAMETER_LINKUP_VALUE_FOR_DOIP = "linkup";
    public final static String PAD3_DHCP_CABLE_ACTION_PARAMETER_DOWN_VALUE_FOR_DOIP = "down";
    /**
     * DHCP分配IP广播
     */
    public final static String PAD3_DHCP_SERVICE_IP_ALLOCATION_ACTION_FOR_DOIP = "com.bsk.broadcast.eth.service.ip";
    /**
     * DHCP分配IP广播参数
     */
    public final static String PAD3_DHCP_SERVICE_IP_ALLOCATION_ACTION_PARAMETER_FOR_DOIP = "ethServiceIP";
    private Context mContext;
    private boolean isDHCPSupport;

    public PAD3DHCPForDoIP(Context context) {
        mContext = context;
        isDHCPSupport = SystemPropertiesInvoke.getBoolean("ro.support_lan_dhcp", false);
    }

    public void registerBoardcastReciver() {
        //pad3设备加入新的以太网编程支持方式
        //3.	判断是否支持该服务属性
        //ro.support_lan_dhcp = true
        if (isDHCPSupport) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(PAD3_DHCP_START_SERVICE_STATUS_ACTION_FOR_DOIP);
            filter.addAction(PAD3_DHCP_STOP_SERVICE_STATUS_ACTION_FOR_DOIP);
            filter.addAction(PAD3_DHCP_CABLE_ACTION_FOR_DOIP);
            filter.addAction(PAD3_DHCP_SERVICE_IP_ALLOCATION_ACTION_FOR_DOIP);
            if (MLog.isDebug) {
                MLog.d(TAG, "ro.support_lan_dhcp is true");
                MLog.d(TAG, "pad3DHCPBroadcastReceiver registerReceiver=." + pad3DHCPBroadcastReceiver.toString());
            }
            try {
                if (mContext != null) {
                    mContext.registerReceiver(pad3DHCPBroadcastReceiver, filter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterBoardcasetReciver() {
        if (isDHCPSupport) {
            if (MLog.isDebug) {
                MLog.d(TAG, "ro.support_lan_dhcp is true");
                MLog.d(TAG, "pad3DHCPBroadcastReceiver  unregisterBoardcasetReciver" + pad3DHCPBroadcastReceiver.toString());
            }
            try {
                if (mContext != null) {
                    mContext.unregisterReceiver(pad3DHCPBroadcastReceiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    BroadcastReceiver pad3DHCPBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MLog.isDebug) {
                MLog.d(TAG, "mBroadcastReceiver action=" + action);
            }
            if (action.equals(PAD3_DHCP_START_SERVICE_STATUS_ACTION_FOR_DOIP)) {
                String parameterValue = intent.getStringExtra(PAD3_DHCP_START_SERVICE_STATUS_ACTION_PARAMETER_FOR_DOIP);
                if (MLog.isDebug) {
                    MLog.d(TAG, String.format("pad3DHCPBroadcastReceiver action=%s,parameter=%s ,vlaue=%s",
                            action, PAD3_DHCP_START_SERVICE_STATUS_ACTION_PARAMETER_FOR_DOIP, parameterValue));
                }
                if (parameterValue == null) {
                    return;
                }
                if (parameterValue.equalsIgnoreCase(PAD3_DHCP_SERVICE_STATUS_ACTION_PARAMETER_SUCCESS_VALUE_FOR_DOIP)) {
                    if (mContext != null) {
                        //NToast.shortToast(mContext, mContext.getString(R.string.msg_dhcp_start_service_status_success));
                    }
                } else if (parameterValue.equalsIgnoreCase(PAD3_DHCP_SERVICE_STATUS_ACTION_PARAMETER_FAIL_VALUE_FOR_DOIP)) {
                    if (mContext != null) {
                        //NToast.shortToast(mContext, mContext.getString(R.string.msg_dhcp_start_service_status_fail));
                    }
                }
                return;
            }
            if (action.equals(PAD3_DHCP_STOP_SERVICE_STATUS_ACTION_FOR_DOIP)) {
                String parameterValue = intent.getStringExtra(PAD3_DHCP_STOP_SERVICE_STATUS_ACTION_PARAMETER_FOR_DOIP);
                if (MLog.isDebug) {
                    MLog.d(TAG, String.format("pad3DHCPBroadcastReceiver action=%s,parameter=%s ,vlaue=%s",
                            action, PAD3_DHCP_STOP_SERVICE_STATUS_ACTION_PARAMETER_FOR_DOIP, parameterValue));
                }
                if (parameterValue == null) {
                    return;
                }
                if (parameterValue.equalsIgnoreCase(PAD3_DHCP_SERVICE_STATUS_ACTION_PARAMETER_SUCCESS_VALUE_FOR_DOIP)) {
                    if (mContext != null) {
                        //NToast.shortToast(mContext, mContext.getString(R.string.msg_dhcp_stop_service_status_success));
                    }
                } else if (parameterValue.equalsIgnoreCase(PAD3_DHCP_SERVICE_STATUS_ACTION_PARAMETER_FAIL_VALUE_FOR_DOIP)) {
                    if (mContext != null) {
                        //NToast.shortToast(mContext, mContext.getString(R.string.msg_dhcp_stop_service_status_fail));
                    }
                }
                return;
            }
            if (action.equals(PAD3_DHCP_CABLE_ACTION_FOR_DOIP)) {
                String parameterValue = intent.getStringExtra(PAD3_DHCP_CABLE_ACTION_PARAMETER_FOR_DOIP);
                if (MLog.isDebug) {
                    MLog.d(TAG, String.format("pad3DHCPBroadcastReceiver action=%s,parameter=%s ,vlaue=%s",
                            action, PAD3_DHCP_CABLE_ACTION_FOR_DOIP, parameterValue));
                }
                if (parameterValue == null) {
                    return;
                }
                if (parameterValue.equalsIgnoreCase(PAD3_DHCP_CABLE_ACTION_PARAMETER_LINKUP_VALUE_FOR_DOIP)) {
                    if (mContext != null) {
                        //NToast.shortToast(mContext, mContext.getString(R.string.msg_dhcp_cable_status_linkup));
                    }
                } else if (parameterValue.equalsIgnoreCase(PAD3_DHCP_CABLE_ACTION_PARAMETER_DOWN_VALUE_FOR_DOIP)) {
                    if (mContext != null) {
                        //NToast.shortToast(mContext, mContext.getString(R.string.msg_dhcp_cable_status_down));
                    }
                    //停止dhcp services 经大量测试发现，不能以网线断开判断，该逻辑移除到独立的方法。 xfh2019/04/03 add
					/*if(isDHCPSupport){
						if(MLog.isDebug) {
							MLog.d(TAG, "ro.support_lan_dhcp is true");
							MLog.d(TAG, "com.bsk.broadcast.stop.eth.service send");
						}
						Intent intentDHCPServiceStop = new Intent("com.bsk.broadcast.stop.eth.service");
						context.sendBroadcast(intentDHCPServiceStop);
					}*/
                }
                return;
            }
            if (action.equals(PAD3_DHCP_SERVICE_IP_ALLOCATION_ACTION_FOR_DOIP)) {
                String parameterValue = intent.getStringExtra(PAD3_DHCP_SERVICE_IP_ALLOCATION_ACTION_PARAMETER_FOR_DOIP);
                if (MLog.isDebug) {
                    MLog.d(TAG, String.format("pad3DHCPBroadcastReceiver action=%s,parameter=%s ,vlaue=%s",
                            action, PAD3_DHCP_SERVICE_IP_ALLOCATION_ACTION_PARAMETER_FOR_DOIP, parameterValue));
                }
                if (parameterValue == null) {
                    return;
                }
                if (mContext != null) {
                    if (TextUtils.isEmpty(parameterValue)) {
                        NToast.INSTANCE.shortToast(mContext, mContext.getString(R.string.msg_dhcp_service_ip_allocation_status_fail));
                    } else {
                        NToast.INSTANCE.shortToast(mContext, String.format(mContext.getString(R.string.msg_dhcp_service_ip_allocation_status_success), parameterValue));
                    }
                }
                return;
            }
        }
    };

    /**
     * 停止dhcp services
     */
    public void stopPAD3DHCPForDoIPServices() {
        //停止dhcp services 经大量测试发现，不能以网线断开判断，该逻辑移除到独立的方法。 xfh2019/04/03 add
        if (isDHCPSupport) {
            if (MLog.isDebug) {
                MLog.d(TAG, "ro.support_lan_dhcp is true");
                MLog.d(TAG, "com.bsk.broadcast.stop.eth.service send");
            }
            String serialNo = DeviceFactoryManager.getInstance().getDeviceName();
            if (TextUtils.isEmpty(serialNo)) {
                serialNo = EasyPreferences.Companion.getInstance().get("serialNo", "");
            }
            if (Tools.isMatchSmartbox30SupportSerialnoPrefix(mContext, serialNo)) {
                //Smartbox30系列接头无需发送停止广播
            } else {
                Intent intentDHCPServiceStop = new Intent("com.bsk.broadcast.stop.eth.service");
                //xx项目需要加入服务类型,后期需要加入autoip支持
                //逻辑变更为停止当前服务，因为dhcp，autoip服务不可能同时运行。
                //if(SystemPropertiesInvoke.getBoolean("ro.support_xx_project",false)){
                //	intentDHCPServiceStop.putExtra("serviceType", "dhcp");
                //}
                if (mContext != null) {
                    mContext.sendBroadcast(intentDHCPServiceStop);
                }
            }
        }
    }
}
