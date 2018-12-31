package com.qiezh.ucon;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText emailText;
    private EditText pwdText;
    private Intent serviceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.login_button);
        emailText = findViewById(R.id.email_text);
        pwdText = findViewById(R.id.pwd_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                Log.d("OnClick", email);
                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                MyUploadService.InitUploadService();
                registerPhoneStateListener();
                startLocalService();
            }
        });
    }
    private void registerPhoneStateListener() {
        Intent intent = new Intent(this, PhoneListenService.class);
        intent.setAction(PhoneListenService.ACTION_REGISTER_LISTENER);
        startService(intent);
    }

    private void startLocalService() {
        Intent intent = new Intent(this, MyLocalService.class);
        startService(intent);
    }
}
