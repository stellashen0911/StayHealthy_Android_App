package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private Button Login_btn;
    private Button register_btn;
    private EditText login_email;
    private EditText login_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set up the buttons
        Login_btn = findViewById(R.id.button_login);
        register_btn = findViewById(R.id.button_sign_up);
        login_email = findViewById(R.id.editTextTextEmailAddress);
        login_password = findViewById(R.id.editTextTextPassword);

        //open the register page
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
            }
        });
    }

    public void openRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}