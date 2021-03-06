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
import android.widget.TextView;

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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class smsgateway extends Activity {
    public static final String PREFS_NAME = "com.parawebs.smsgateway";
    public String text;
    public String number;
    public String ts;
    private Switch switch_server;
    private AsyncHttpServer server;
    private SharedPreferences prefs;
    private int port;
    private String ipAddress;
    private static TextView textView_send;
    private static TextView textView_fail;

    private void getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contentEquals("wlan0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            ipAddress = inetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex.toString());
        }
    }


    public void poster(String status, String idm, String detail) {
        String url = "http://requestb.in/tj20vwtj";
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = settings.edit();
//        if (settings.getString("url_callback", "0") == "0") {
//            editor.putString("port", "http://requestb.in/tj20vwtj");
//            editor.commit();
//            url = "http://requestb.in/tj20vwtj";
//        } else {
//            url = settings.getString("url_callback", "http://requestb.in/tj20vwtj");
//        }
        Log.i("URL", url);
        RequestParams params = new RequestParams();
        params.put("status", status);
        params.put("detail", detail);
        params.put("id", idm);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String str = new String(responseBody);
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
    public void send_count(int status)
    {
//        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // TextView textView_send = (TextView) findViewById(R.id.send_count);
        // TextView textView_fail = (TextView) findViewById(R.id.fail_count);

//        int send = db.getCounter(1).getCount();
//        int fail = db.getCounter(2).getCount();

        switch (status) {
            case 1:
                int send = Integer.parseInt(textView_send.getText().toString()) + 1;
                textView_send.setText(String.valueOf(send));
                break;
            case 2:
                int fail = Integer.parseInt(textView_fail.getText().toString()) + 1;
                textView_send.setText(String.valueOf(fail));
                break;
        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsgateway);
        switch_server = (Switch) findViewById(R.id.switch_server);
        textView_send = (TextView) findViewById(R.id.send_count);
        textView_fail = (TextView) findViewById(R.id.fail_count);

        DatabaseHandler db = new DatabaseHandler(this);

        if (db.getCounter(1).getCount() == 0) {
            db.createCounters(1, "send");
            db.createCounters(1, "fail");

        } else {
            Log.i("LOG", "ya existia...");
        }

        switch_server.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                TextView url_current = (TextView) findViewById(R.id.url_dest);
                if (checkNetworkState()) {
                    if (isChecked) {
                        getLocalIpAddress();
                        server = null;
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        if (settings.getInt("port", 0) == 0) {
                            editor.putInt("port", 18080);
                            editor.commit();
                            port = 18080;
                        } else {
                            port = settings.getInt("port", 0);
                        }
                        url_current.setText("http://" + ipAddress + ":" + String.valueOf(port));
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
                                    com.parawebs.smsgateway.SMSLibrary sms = new com.parawebs.smsgateway.SMSLibrary();
                                    sms.sendSMS(getApplicationContext(), number, text, ts);
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
                                response.send("Runing!!!");
                            }
                        });
                        Log.i("LOG", "Current port: " + String.valueOf(port));
                        server.listen(port);
                    } else {
                        AsyncServer.getDefault().stop();
                        Log.i("LOG", "Server stopped!");
                        url_current.setText("Server Offline");
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
