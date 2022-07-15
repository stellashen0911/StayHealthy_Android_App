package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {
    private Button register_btn;
    private EditText input_email;
    private EditText input_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //link the input and button set up
        register_btn = findViewById(R.id.button_register);
        input_email = findViewById(R.id.editTextTextEmailAddress_register);
        input_password = findViewById(R.id.editTextTextPassword_register);
    }
}