package com.cnlaunch.physics.smartlink;

import android.content.Context;
import android.text.TextUtils;

import com.cnlaunch.physics.utils.MLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConfigUtil {
    private static final String TAG = "ConfigUtil";

    public static void getUdpServerInfo(String SN) {
        String url = String.format("https://ait.x431.com/SmartLink/Device/get_udp_server_info?serial_number=%s",  SN);
/*        if(false) {
            url = String.format("http://aittest.x431.com:8000/SmartLink/Device/get_udp_server_info?serial_number=%s", SN);
        }*/

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();//创建Request 对象
            Response response = client.newCall(request).execute();//得到Response 对象
            if (response.isSuccessful()) {
                String data = response.body().string();
                MLog.d(TAG,"get_udp_server_info data=="+data);
                if (!TextUtils.isEmpty(data)) {
                    JSONObject json = new JSONObject(data);
                    int code = json.getInt("code");
                    if (code == 0) {
                        if (json.has("data")) {
                            JSONObject jsonData = json.getJSONObject("data");
                            String udpIp = jsonData.getString("ip");
                            int udpPort = Integer.parseInt(jsonData.getString("port"));
                            PeerTask.getInstance().setUdpIpAndPort(udpIp, udpPort);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static TaskInfo getMyTask(String SN) {
        boolean ret = false;
        //根据B端序列号获取任务信息
        String url = String.format("https://ait.x431.com/SmartLink/Task/get_my_task?serial_number=%s", SN);
/*        if(test) {
            url = String.format("%s/SmartLink/Task/get_my_task?serial_number=%s", CONFIG_SERVER_NAME_TEST,
                    PeerLink.getInstance().peer_sn);
        }*/

        OkHttpClient client = new OkHttpClient();
        int retrySum = 1;
        while (retrySum < 3) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();//创建Request 对象
                Response response = client.newCall(request).execute();//得到Response 对象
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    MLog.d(TAG, "getMyTask = " + data);
                    if (!TextUtils.isEmpty(data)) {
                        JSONObject json = new JSONObject(data);
                        if (json.has("data")) {
                            TaskInfo taskInfo = new TaskInfo();
                            JSONObject dataObj = json.getJSONObject("data");
                            taskInfo.task_id = dataObj.getString("task_id");
                            taskInfo.user_id = dataObj.getString("user_id");
                            taskInfo.sn = dataObj.getString("serial_number");
                            taskInfo.vin = dataObj.getString("vin");
                            taskInfo.accept_time = dataObj.getString("accept_time");
                            taskInfo.brand = dataObj.getString("brand");
                            taskInfo.model = dataObj.getString("models");
                            taskInfo.year = dataObj.getString("year");
                            taskInfo.telephone = dataObj.getString("telephone");
                            if (dataObj.has("task_type")) {
                                String taskType = dataObj.getString("task_type");
                                taskInfo.is_auto = Byte.parseByte(taskType);
                            } else {
                                taskInfo.is_auto = 0;
                            }

                            retrySum = 3;
                            return taskInfo;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            retrySum ++;
        }
        return null;
    }

    public static ArrayList<ServerInfos> request_server_list(Context context) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://ait.x431.com/SmartLink/Device/get_proxy_remote_server_list")
                    .build();//创建Request 对象
            Response response = client.newCall(request).execute();//得到Response 对象
            if (response.isSuccessful()) {
                String data = response.body().string();
                MLog.d(TAG, " requestServerListdata data= " + data);
                if (!TextUtils.isEmpty(data)) {
                    JSONObject json = new JSONObject(data);
                    int code = json.getInt("code");
                    if (code == 0) {
                        ArrayList<ServerInfos> mServerLists = new ArrayList<ServerInfos>();
                        if (json.has("data")) {
                            JSONArray jsonArray = json.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                ServerInfos serverInfo = new ServerInfos();
                                JSONObject dataObj = jsonArray.getJSONObject(i);
                                serverInfo.name = dataObj.getString("name");
                                serverInfo.domain = dataObj.getString("domain_name");
                                serverInfo.port = dataObj.getString("port");
                                mServerLists.add(serverInfo);
                            }
                            //retrySum = 3;
                            return mServerLists;
/*                            //测试服务器指令
                            ArrayList<Long> times = testServerSpeed(context, mServerLists);
                            //发送到B服务
                            UdpSocketControler.getInstance(context).sendTestServerData(times);*/
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
