package com.parawebs.smsgateway;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsView extends Activity {

    private EditText editText_port;
    private EditText editText_url;
    private Button button_accept;
    private Button button_cancel;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        editText_port = (EditText) findViewById(R.id.editText_port);
        editText_url = (EditText) findViewById(R.id.editText_url);
        button_accept = (Button) findViewById(R.id.button_accept);
        button_cancel = (Button) findViewById(R.id.button_cancel);

        SharedPreferences prefs = this.getSharedPreferences("com.parawebs.smsgateway", Context.MODE_PRIVATE);
        editor = prefs.edit();

        button_accept.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                try{
                    int port = Integer.parseInt(editText_port.getText().toString());
                    String url = editText_url.getText().toString();
                    if (port < 1 || port > 65535) showAlert("Enter a valid port number.");
                    else {
                        editor.putInt("port", port);
                        editor.putString("url_callback", url);
                        editor.apply();
                        showAlert("Callback updated to " + url);
                        showAlert("Port number updated to " + port);
                        finish();
                    }
                } catch (NumberFormatException e) {
                    showAlert("Enter a valid port number.");
                }
            }
        });

        button_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    private void showAlert(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}
