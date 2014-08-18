package com.parawebs.smsgateway;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class smsgateway extends Activity {
    private Switch switch_server;
    private AsyncHttpServer server;
    public String text;
    public String number;
    public String ts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsgateway);
        switch_server = (Switch) findViewById(R.id.switch_server);
        server = null;

        switch_server.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (checkNetworkState()) {
                    if (isChecked) {
                        Log.i("LOG", "Starting server ...");
                        AsyncHttpServer server = new AsyncHttpServer();

                        List<WebSocket> _sockets = new ArrayList<WebSocket>();

                        server.post("/", new HttpServerRequestCallback() {
                            @Override
                            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                                AsyncHttpRequestBody bodyy = request.getBody();
                                JSONObject json = new JSONObject();
                                try {
                                    Object respo = bodyy.get();
                                    text = ((JSONObject) respo).get("text").toString();
                                    number = ((JSONObject) respo).get("number").toString();
                                    Log.i("BD", text);
                                    Log.i("BD", number);
                                    Long tsLong = System.currentTimeMillis() / 1000;
                                    ts = tsLong.toString();
                                    com.parawebs.smsgateway.SMSLibrary sms= new  com.parawebs.smsgateway.SMSLibrary();
                                    sms.sendSMS(getApplicationContext(),number,text,ts);
                                    try {
                                        json.put("status", "accepted");
                                        json.put("id", ts);
                                    } catch (Exception e) {
                                        Log.w("ERROR", e.getMessage());
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.w("BD", "Something goes wrong");
                                    try {
                                        json.put("status", "fail");
                                        json.put("error", "invalid parameters");
                                    } catch (Exception a) {
                                        Log.w("ERROR", a.getMessage());
                                    }
                                }

                                response.setContentType("Content-Type: application/json");
                                response.send(json);

                            }
                        });
                        server.get("/test", new HttpServerRequestCallback() {
                            @Override
                            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                                response.send("test!!!");
                            }
                        });

                        server.listen(5000);
                    } else {
                        AsyncServer.getDefault().stop();
                        Log.i("LOG", "Server stopped!");
                    }
                } else {
                    switch_server.toggle();
                }
            }
        });

    }


    private boolean checkNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        Log.w("LOG", "No active network connections \navailable.");

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.smsgateway, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
