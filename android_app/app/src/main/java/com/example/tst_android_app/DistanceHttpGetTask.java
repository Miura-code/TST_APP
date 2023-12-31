package com.example.tst_android_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;


public class DistanceHttpGetTask extends AsyncTask<Void, Void, String> {
    private TextView mTextView;
    private Activity mParentActivity;
    private ProgressDialog mDialog = null;
    private String distance;
    private String DEFAULTUAL = "http://192.168.11.24/~pi/distance_sensor.php?";
    private Handler handler = new Handler();

    public DistanceHttpGetTask(Activity parentActivity, TextView textView, String ip){
        this.mParentActivity = parentActivity;
        this.mTextView = textView;
        this.DEFAULTUAL = "http://" + ip + "/~pi/distance_sensor.php?";
    }

    @Override
    protected void onPreExecute(){
        mDialog = new ProgressDialog(mParentActivity);
        mDialog.setMessage("");
//        mDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String ret;
        for(int i=0; i<1000; i++) {
            Log.d("Distance", String.valueOf(i));
            if(isCancelled() && i > 0){
                break;
            }
            ret = exec_get();
            String finalRet = ret;
            Float floatRet = Float.valueOf(finalRet);
            String command = "";
            if(floatRet > 30){
                command = "近づけ！("+finalRet+" cm)";
            }
            else if(floatRet < 20){
                command = "離れろ！("+finalRet+" cm)";
            }
            else{
                command = "撃て！("+finalRet+" cm)";
            }
            String finalCommand = command;
            Log.d("Distance", finalCommand);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(finalCommand);
                }
            });
        }
        return "";
    }

    @Override
    protected void onPostExecute(String string){
        mDialog.dismiss();
        distance = "measurement error";
        try{
            JSONObject rootJSON = new JSONObject(string);
            distance = rootJSON.getString("distance");
        }catch (JSONException e){
            e.printStackTrace();
        }
        this.mTextView.setText(distance);
    }

    private String exec_get() {
        HttpURLConnection urlConnection;
        InputStream inputStream;
        String result = "";
        String str = "";
        try {
            URL url = new URL(DEFAULTUAL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.addRequestProperty("User-Agent", "Android");
            urlConnection.addRequestProperty("Accept-Language", Locale.getDefault().toString());
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);

            urlConnection.connect();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                result = bufferedReader.readLine();
                while (result != null) {
                    str += result;
                    result = bufferedReader.readLine();
                }
                bufferedReader.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
