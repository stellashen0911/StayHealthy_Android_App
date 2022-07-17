package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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




        //click the login button
        Login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        //click the sign up button and open the register page
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
            }
        });
    }
    public void openLoginActivity() {
        String email_str = login_email.getText().toString();
        String password_str = login_password.getText().toString();
        //make sure that the password and username is not empty
        if (email_str.isEmpty() || password_str.isEmpty()) {
            Context context = getApplicationContext();
            CharSequence text = "Username and password cannot be empty!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        //validate the password and username in the firebase authentication


        //if the authentication is correct open the health record page
        Intent healthRecordIntent = new Intent(this, HealthRecordActivity.class);
        startActivity(healthRecordIntent);
    }

    public void openRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}