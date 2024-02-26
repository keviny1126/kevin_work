package com.cnlaunch.rj45link;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.utils.ByteHexHelper;

/**
 * 测试tcp相关代码
 * Created by zhangshengda on 2016/4/14.
 */
public class RJ45LinkTestActivity extends Activity {
    private Button btnSend;
    private Button btnRecive;
    private Button btnLinkTCP;
    private Button btnLinkUDP;
    private Button btnBreakTCP;
    private EditText etInput;
    private TextView tvShow;
    private final byte[][] reqArray = new byte[][]{{0x00, 0x00, 0x00, 0x05}, {0x00, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xF4, 0x10, 0x22, 0x3F, 0x06}, {0x00, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xF4, 0x10, 0x22, (byte) 0x3F, 0x06}, {0x00, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xF4, 0x10, 0x22, (byte) 0xF1, 0x01}, {0x00, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xF4, 0x40, 0x22, (byte) 0xF1, (byte) 0x90}};
    private String showString = "";
    private boolean isLaunchTCP = false;

    int openTcpConnect()
    {
        isLaunchTCP = false;
        mHandler.obtainMessage(1, "启动TCP连接").sendToTarget();
        RJ45LinkManager.getInstance().connectTCPLink("172.16.165.36", 6801, new TCPHandler() {

            @Override
            public void connectFailed() {
                Log.d("Sanda", "connectFailed");
            }

            @Override
            public void socketTimeOut() {
                Log.d("Sanda", "socketTimeOut");
            }

            @Override
            public void connectSuccess() {
                Log.d("Sanda", "connectSuccess");
                //mHandler.obtainMessage(1, "启动TCP连接成功").sendToTarget();
                isLaunchTCP = true;
            }

            @Override
            public void connectClosed() {
                Log.d("Sanda", "connectClosed");
            }

        });

        if(isLaunchTCP)
            return 0;

        return -1;
    }

    int SendAndReceiveUdpData()
    {
        byte[] data = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x11};
        mHandler.obtainMessage(1, "UDP_Req:" + ByteHexHelper.bytesToHexStrWithSwap(data)).sendToTarget();
//                RJ45LinkManager.getInstance().sendUDPLink("172.16.65.23", new RJ45ReceiveMessage(data), new RJ45LinkManager.UDPDataReceiver() {
        byte[] receive = RJ45LinkManager.getInstance().sendUDPLink(3000, 6811, data,0,data.length);
        if(receive.length > 0)
        {
            String receiveData = ByteHexHelper.bytesToHexStrWithSwap(receive);
            mHandler.obtainMessage(1, "UDP_Ans:" + receiveData).sendToTarget();
        }
        return receive.length;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);
        tvShow = (TextView) findViewById(R.id.tv_show);
        etInput = (EditText) findViewById(R.id.et_input_show);
        //连接TCP
        btnLinkTCP = (Button) findViewById(R.id.btn_link_tcp);
        btnLinkTCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int nRet = openTcpConnect();
                if(nRet == 0)
                {
                    mHandler.obtainMessage(1, "启动TCP连接成功").sendToTarget();
                }
                else
                {
                    mHandler.obtainMessage(1, "启动TCP连接失败").sendToTarget();
                }
//                isLaunchTCP = true;
//                mHandler.obtainMessage(1, "启动TCP连接").sendToTarget();
//                RJ45LinkManager.getInstance().connectTCPLink("172.16.165.36", 6801, new TCPHandler() {
//
//                    @Override
//                    public void connectFailed() {
//                        Log.d("Sanda", "connectFailed");
//                    }
//
//                    @Override
//                    public void socketTimeOut() {
//                        Log.d("Sanda", "socketTimeOut");
//                    }
//
//                    @Override
//                    public void connectSuccess() {
//                        Log.d("Sanda", "connectSuccess");
//                        mHandler.obtainMessage(1, "启动TCP连接成功").sendToTarget();
//                    }
//
//                    @Override
//                    public void connectClosed() {
//                        Log.d("Sanda", "connectClosed");
//                    }
////作废
////                    @Override
////                    public void messageReceived(Object message) {
////                        RJ45ReceiveMessage dataMessage = (RJ45ReceiveMessage) message;
////                        String sendData = ByteHexHelper.bytesToHexStrWithSwap(dataMessage.getData());
////                        mHandler.obtainMessage(1, "TCP_Ans:" + sendData).sendToTarget();
////                    }
//                });
            }
        });
        //断开TCP
        btnBreakTCP = (Button) findViewById(R.id.btn_break_tcp);
        btnBreakTCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLaunchTCP = false;
                mHandler.obtainMessage(1, "断开TCP连接").sendToTarget();
                RJ45LinkManager.getInstance().breakTCPLink();
            }
        });
        //连接UDP并且发送数据
        btnLinkUDP = (Button) findViewById(R.id.btn_link_UDP);
        btnLinkUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendAndReceiveUdpData();
//                byte[] data = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x11};
//                mHandler.obtainMessage(1, "UDP_Req:" + ByteHexHelper.bytesToHexStrWithSwap(data)).sendToTarget();
////                RJ45LinkManager.getInstance().sendUDPLink("172.16.65.23", new RJ45ReceiveMessage(data), new RJ45LinkManager.UDPDataReceiver() {
//                RJ45LinkManager.getInstance().sendUDPLink(3000, 6811, new RJ45ReceiveMessage(data), new RJ45LinkManager.UDPDataReceiver() {
//                    @Override
//                    public void messageReceived(RJ45ReceiveMessage message) {
//                        String receiveData = ByteHexHelper.bytesToHexStrWithSwap(message.getData());
//                        mHandler.obtainMessage(1, "UDP_Ans:" + receiveData).sendToTarget();
//                    }
//
//                    @Override
//                    public void error() {
//                        Log.e("Sanda", "发送或者接受失败");
//                    }
//                });
            }
        });
        //发送TCP数据
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLaunchTCP) {
//                    NToast.shortToast(RJ45LinkTestActivity.this, "需要先连接TCP服务");
                    return;
                }
                String sendText = etInput.getText().toString();
                Integer position = 0;
                if (sendText == null || sendText.isEmpty()) {
                    position = 0;
                } else {
                    position = Integer.parseInt(sendText);

                    if (position == null || position.intValue() >= reqArray.length) {
                        position = reqArray.length - 1;
                    }
                }
                Log.d("Sanda", "postion=" + position);
                String sendData = ByteHexHelper.bytesToHexStrWithSwap(reqArray[position]);
                mHandler.obtainMessage(1, "TCP_Req:" + sendData).sendToTarget();
                RJ45LinkManager.getInstance().clearReceiveData();
                RJ45LinkManager.getInstance().send(reqArray[position],0,reqArray[position].length);
            }
        });
        btnRecive = (Button) findViewById(R.id.btn_recive);
        btnRecive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] receive = RJ45LinkManager.getInstance().recevice(5000);
                        String sendData = ByteHexHelper.bytesToHexStrWithSwap(receive);
                        mHandler.obtainMessage(1, "TCP_ANS:" + sendData).sendToTarget();
                    }
                }).start();
            }
        });

    }

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    tvShow.setText(showString);
                    break;
                case 1:
                    String data = (String) msg.obj;
                    showString = data + "\n" + showString;
                    tvShow.setText(showString);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }


        }
    };
}
