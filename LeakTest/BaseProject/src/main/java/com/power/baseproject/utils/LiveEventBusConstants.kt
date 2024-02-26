package com.power.baseproject.utils

class LiveEventBusConstants {
    companion object {

        const val READ_DEVICEINFO_FUNC = "read_deviceinfo_func"
        const val INIT_DEVICE_DATA = "init_device_data"//下位机断电，重置界面数据显示

        const val SEND_REQUEST_PERMISSION = "send_request_permission"//蓝牙权限检查结果
        const val REQUEST_PERMISSION_RESULT = "request_permission_result"//蓝牙权限检查结果
        const val USB_CONNECT_RESULT = "usb_connect_result"
        const val SERIAL_NEED_CONNECT = "need_connect_serial"//需要重连串口

        const val SERIAL_PORT_RECEIVER_DATA = "serial_port_receiver_data"
        const val REFRESH_ON_ITEM_CLICK_VIEW = "refresh_on_item_click_view"
        const val DOWNLOAD_APP_SUCCESS = "download_app_success"

        const val SETTING_TITLE_NAME = "setting_title_name"
        const val CLOSE_FACTORY_TOOL = "close_factory_tool"
        const val SERVICE_STOP_INIT = "service_stop_init"
        const val CLEAR_ADC_RESULT = "clear_adc_result"
        const val SET_SERIAL_NUM_RESULT = "set_serial_num_result"
        const val UPDATE_FIRMWARE = "update_firmware"
        const val CHECK_UPLOAD_DATA = "check_upload_data"
    }
}