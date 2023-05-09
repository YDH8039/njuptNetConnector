package com.yang.njuptnet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yang.njuptnet.dataclass.Person;


public class MainActivity extends AppCompatActivity {
    String ip, text, result, content, name, code;
    String ip1 = "http://p.njupt.edu.cn/";
    String ip2 = "http://p.njupt.edu.cn:801/eportal/?c=ACSetting&a=checkScanIP&wlanuserip=";
    String ip3 = "http://p.njupt.edu.cn:801/eportal/?c=ACSetting&a=Login&wlanacip=10.255.252.150";
    Person person = new Person();
    boolean debugTag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //给两个按钮设置监听
        Button btn1 = findViewById(R.id.modify_button);
        btn1.setOnClickListener(new BtnClickListener());
        Button btn2 = findViewById(R.id.submit_button);
        btn2.setOnClickListener(new BtnClickListener());


        NetworkChecker networkCallback = new NetworkChecker();
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest request = builder.build();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            connMgr.registerNetworkCallback(request, networkCallback);
        }
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        int id = view.getId();
        if (checked) {
            if (id == R.id.cmcc_selector) person.setType("@cmcc");
            if (id == R.id.cnnet_selector) person.setType("@njxy");
            if (id == R.id.njupt_selector) person.setType("校园网");
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.see_code) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认打开浏览器？");
            builder.setPositiveButton("确认", (dialog, arg1) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/YDH8039/njuptNetConnector"));
                startActivity(intent);
                dialog.dismiss();
            });
            builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }
        if (id == R.id.help_menu) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        }
        return true;
    }


    //按钮的监听
    class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Integer id = v.getId();
            if (id.equals(R.id.modify_button)) saveInfo();
            if (id.equals(R.id.submit_button)) connect();
        }
    }

    public void getWifiName() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String SSID = wifiInfo.getSSID();


        Log.d("debug", SSID);

    }


    //连接的步骤
    public void connect() {
        RadioButton radioButton = findViewById(R.id.cmcc_selector);
        EditText txtCode = findViewById(R.id.password_textview);
        EditText txtName = findViewById(R.id.account_textview);

        //读取存储的内容
        SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
        name = read.getString("name", "");
        code = read.getString("code", "");
        txtName.setText(name);
        txtCode.setText(code);

        switch (read.getString("type", "")) {
            case "校园网": {
                radioButton = findViewById(R.id.njupt_selector);
                break;
            }
            case "@cmcc": {
                radioButton = findViewById(R.id.cmcc_selector);
                break;
            }
            case "@njxy": {
                radioButton = findViewById(R.id.cnnet_selector);
                break;
            }
        }
        radioButton.setChecked(true);

        person.set(name, code, read.getString("type", ""));
        //将两行输入框设置为我们现在用的学号和密码
        //因为选择校园网的时候后缀为空但这里不能直接设置为空，所以我们加个判断来区分选择校园网的时候
        if (person.getType().equals("校园网")) {
            content = "DDDDD=,0," + person.getId() + "&upass=" + person.getPassword();
        } else {
            content = "DDDDD=,0," + person.getId() + person.getType() + "&upass=" + person.getPassword();
        }
        //开启子线程
        new Thread(() -> {
            //先做个连接，因为有时候这个ip还不是对应自己的
            NetAction.sendPost(ip3, content);
            //做个计数
            int count = 1;
            while (true) {
                try {
                    //每20秒进行一次的循环
                    Thread.sleep(20000);
                    text = NetAction.sendGet(ip1);
                    ip = NetAction.re_search("v46ip='([^']*)'", text);
                    ip2 += ip;
                    text = NetAction.sendGet(ip2);
                    result = NetAction.re_search("\"result\":\"([^\"]*)\"", text);
                    if (!result.equals("ok")) {
                        //不是ok的时候发起连接
                        NetAction.sendPost(ip3, content);
                        Log.d("tag", content + count);
                    } else {
                        //已经连上时弹出消息框已经连接
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                "connected", Toast.LENGTH_SHORT).show());
                        Log.d("tag", String.valueOf(count));
                    }
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //修改学号密码
    public void saveInfo() {
        //读取输入框的值
        EditText txtName = findViewById(R.id.account_textview);
        EditText txtCode = findViewById(R.id.password_textview);

        name = txtName.getText().toString().trim();
        code = txtCode.getText().toString().trim();

        if (("".equals(name)) | ("".equals(code)))
            Toast.makeText(getApplicationContext(), "没有输入账户信息哦/_ \\", Toast.LENGTH_SHORT).show();
        else {
            //永久性存储数据
            SharedPreferences.Editor editor = getSharedPreferences("lock", MODE_PRIVATE).edit();
            editor.putString("name", name);
            editor.putString("code", code);
            editor.putString("type", person.getType());
            editor.apply();

            //提示消息设置成功
            Toast.makeText(getApplicationContext(), "保存成功ヾ(≧▽≦*)o", Toast.LENGTH_SHORT).show();
        }
    }
}