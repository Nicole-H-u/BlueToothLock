package com.FFX.bluedoorlock;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.FFX.bluedoorlock.errorprocess.ErrorProcess;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private static final int REQUES_SELECT_BT_CODE = 0x1001;    //选择设备码
    private BluetoothAdapter mBluetoothAdapter;      //蓝牙适配器
    private TextView tv_title;
    private ImageButton imbtn_open;
    private EditText txt_password;
    private static boolean isopen = false;
    private ErrorProcess errorProcess;
    private Timer timer;
    private static long count = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        //初始化控件并设置监听器
        init();
        errorProcess = new ErrorProcess();
        timer = new Timer();
        // 获得蓝牙匹配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 蓝牙设备不被支持
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "该设备没有蓝牙设备", Toast.LENGTH_LONG).show();
        }
        // 使能蓝牙设备
        mBluetoothAdapter.enable();
        while (!mBluetoothAdapter.isEnabled()) {
            try {
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String address = "98:D3:32:20:54:0A";
        BluetoothDevice AutoDevice = mBluetoothAdapter.getRemoteDevice(address);
        BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_START_CONN_THREAD,
                new Object[]{AutoDevice}));
    }

    private void init() {
        //初始化控件
        Button btn_scan = (Button) findViewById(R.id.button_scan);
        Button btn_send = (Button) findViewById(R.id.button_send);
        Button btn_more = (Button) findViewById(R.id.button_more);
        Button btn_title = (Button) findViewById(R.id.button_title);
        tv_title = (TextView) findViewById(R.id.textView_title);
        imbtn_open = (ImageButton) findViewById(R.id.imageButton_open);
        txt_password = findViewById(R.id.editText_password);
        //设置监听器
        btn_scan.setOnClickListener(listener);
        btn_send.setOnClickListener(listener);
        btn_more.setOnClickListener(listener);
        btn_title.setOnClickListener(listener);
        imbtn_open.setOnClickListener(listener);
    }

    private final OnClickListener listener = new OnClickListener() {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_scan:
                    // 扫描周围蓝牙设备
                    startActivityForResult(new Intent(MainActivity.this, SelectDevice.class), REQUES_SELECT_BT_CODE);
                    break;
                case R.id.button_send:
                    startActivity(new Intent(MainActivity.this, CommandInput.class));
                    break;
                case R.id.button_more:
                    startActivity(new Intent(MainActivity.this, MoreFucntion.class));
                    break;
                case R.id.button_title:
                    BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG,
                            new Object[]{}));
                    break;
                case R.id.imageButton_open:
                    // 将发送消息任务提交给后台服务
                    String msg;
                    String input = txt_password.getText().toString();
                    if (input == null || input.isEmpty()) {
                        if (!isopen) {
                            msg = input;
                            isopen = true;
                            imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
                        } else {
                            msg = "*lock#";
                            isopen = false;
                            imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.lock));
                        }
                        BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG,
                                new Object[]{msg}));
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Password Empty!", Toast.LENGTH_SHORT).show();
                    }
                     break;
            }
        }
    };

    // 当startActivityForResult启动的 画面结束的时候，该方法被回调
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK) {
            //用户选择连接的蓝牙设备
            BluetoothDevice mRemoteDevice = data.getParcelableExtra("DEVICE");
            if (mRemoteDevice == null)
                return;
            // 提交连接用户选择的设备对象，自己作为客户端
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}, mRemoteDevice.getUuids()[7].toString()));
            }
            BluetoothService.start(this, mHandler);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.TASK_SEND_MSG:
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.TASK_RECV_MSG:
                    if (msg.obj.toString().equals("on")) {
                        imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
                    } else if (msg.obj.toString().equals("off")) {
                        imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.lock));
                    }
                    break;
                case BluetoothService.TASK_GET_REMOTE_STATE:
                    tv_title.setText(msg.obj.toString());
                    if (msg.obj.toString().endsWith("[已连接]")) {
                        startTimer(count);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    protected void onDestroy() {
        // 停止服务
        BluetoothService.stop(this);
        mBluetoothAdapter.disable();
        super.onDestroy();
    }

    public void startTimer(long count) {
        count = 1000 * count;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG,
                        new Object[]{}));
            }
        }, count);
    }

    public void resetTimer(long count) {
        timer.cancel();
        count = 1000 * count;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG,
                        new Object[]{}));
            }
        }, count);
    }
}
