package com.parawebs.smsgateway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
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
    private SharedPreferences prefs;
    private int port;



    public void poster(String status,String idm,String detail)
    {

        RequestParams params = new RequestParams();
        params.put("status", status);
        params.put("detail",detail);
        params.put("id",idm);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://requestb.in/tj20vwtj", params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String str =new String(responseBody);
                Log.w("LOG", str);
                // called when response HTTP status is "200 OK"
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsgateway);
        switch_server = (Switch) findViewById(R.id.switch_server);
        server = null;
        prefs = this.getSharedPreferences("com.parawebs.smsgateway",Context.MODE_PRIVATE);
        if (prefs.getInt("port",0) == 0) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("port", 18080);
            editor.commit();
            port = 18080;
        }
        Log.i("PORTINFO",String.valueOf(prefs.getInt("port",0)));
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
                        Log.i("ERROR",String.valueOf(port));
                        server.listen(port);
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
        switch (item.getItemId()) {
            case R.id.item_setting:
                startActivity(new Intent(this, SettingsView.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
