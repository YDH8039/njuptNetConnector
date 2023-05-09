package com.yang.njuptnet;

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

public class NetAction {
    //以下是Get、Post以及search函数
    static HttpURLConnection connection;
    static String line;
    static String pattern;
    static String param;
    static String results;
    static int responseCode;

    //get的用法，用String获取get的访问值来满足对ip和状态的确定
    public static String sendGet(String ip) {
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
    public static void sendPost(String ip, String pa) {
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
    private static String getStringByStream(InputStream inputStream) {
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
    public static String re_search(String pat, String li) {
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
