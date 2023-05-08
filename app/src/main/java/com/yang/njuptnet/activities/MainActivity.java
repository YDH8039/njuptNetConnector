package com.yang.njuptnet.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.yang.njuptnet.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        //RadioButton 监听
//        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
//        radioGroup.setOnClickListener(new RadioGroup.);

        //给下拉框设置监听
//        Spinner spinner = findViewById(R.id.spinner);
//        spinner.setOnItemSelectedListener(new ProvOnItemSelectedListener());
    }

    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
//        String type = (String) ((RadioButton) view).getText();
        int id = view.getId();
        if (checked) {
            if (id == R.id.cmcc_selector) person.setType("@cmcc");
            if (id == R.id.cnnet_selector) person.setType("@njxy");
            if (id == R.id.njupt_selector) person.setType("校园网");
            if (debugTag)
                Log.d("debug", (String) ((RadioButton) view).getText());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //创建选项菜单
//        Menu menu = findViewById(R.id.m)
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.see_code) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认打开浏览器？");
            //                @Override
            builder.setPositiveButton("确认", (dialog, arg1) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/YDH8039/njuptNetConnector"));
                startActivity(intent);
                dialog.dismiss();

            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
        }
        if (id == R.id.help_menu) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
//          Toast.makeText(getApplicationContext(), "Help Todo", Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    //按钮的监听
    class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Integer id = v.getId();
//            if (debugTag)
//                Log.d("debug",)
            if (id.equals(R.id.modify_button)) {
                saveInfo();
//                if (debugTag) {
//                    Log.d("debug", person.id + person.type);
//                    Log.d("debug", "modify");
//                }
            }
            if (id.equals(R.id.submit_button)) {
                connect();
//                if (debugTag) {
//                    Log.d("debug", person.id + person.type);
//                    Log.d("debug", "submit");
//                }
            }
        }
    }

    //下拉框的监听
//    private class ProvOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
//        @Override
//        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
//            String sInfo = adapter.getItemAtPosition(position).toString();
//            if (sInfo.equals("中国移动")) person.setType("@cmcc");
//            if (sInfo.equals("中国电信")) person.setType("@njxy");
//            if (sInfo.equals("校园网")) person.setType("校园网");
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> arg0) {
//            String sInfo = "null";
//        }
//    }

    //连接的步骤
    public void connect() {
        RadioButton radioButton = findViewById(R.id.cmcc_selector);

        EditText txtName = findViewById(R.id.account_textview);
        EditText txtCode = findViewById(R.id.password_textview);

        //读取存储的内容
        SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
        name = read.getString("name", "");
        code = read.getString("code", "");

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
        txtName.setText(name);
        txtCode.setText(code);
        //因为选择校园网的时候后缀为空但这里不能直接设置为空，所以我们加个判断来区分选择校园网的时候
        if (person.getType().equals("校园网")) {
            content = "DDDDD=,0," + person.getId() + "&upass=" + person.getPassword();
        } else {
            content = "DDDDD=,0," + person.getId() + person.getType() + "&upass=" + person.getPassword();
        }
        //开启子线程
        new Thread(() -> {
            //先做个连接，因为有时候这个ip还不是对应自己的
            sendPost(ip3, content);
            //做个计数
            int count = 1;
            while (true) {
                try {
                    //每20秒进行一次的循环
                    Thread.sleep(20000);
                    text = sendGet(ip1);
                    ip = re_search("v46ip='([^']*)'", text);
                    ip2 += ip;
                    text = sendGet(ip2);
                    result = re_search("\"result\":\"([^\"]*)\"", text);
                    if (!result.equals("ok")) {
                        //不是ok的时候发起连接
                        sendPost(ip3, content);
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

    //以下是Get、Post以及search函数
    HttpURLConnection connection;
    String line, pattern, param, results;
    int responseCode;

    //get的用法，用String获取get的访问值来满足对ip和状态的确定
    public String sendGet(String ip) {
        try {
            connection = null;
            URL url = new URL(ip);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code" + responseCode);
            }
            results = getStringByStream(connection.getInputStream());
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    //post 因为这里用不到返回值所以void就行
    public void sendPost(String ip, String pa) {
        try {
            connection = null;
            URL url = new URL(ip);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            param = pa;
            dos.writeBytes(param);
            responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code" + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //用于返回值的处理
    private String getStringByStream(InputStream inputStream) {
        Reader reader;
        StringBuilder buffer;
        try {
            reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] rawBuffer = new char[512];
            buffer = new StringBuilder();
            int length;
            while ((length = reader.read(rawBuffer)) != -1) {
                buffer.append(rawBuffer, 0, length);
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //模拟python中的re.search
    public String re_search(String pat, String li) {
        line = li;
        pattern = pat;
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        if (m.find()) {
            return m.group(1);
        } else {
            return "null";
        }
    }
}