package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        //set the listener for the register button
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_user();
            }
        });

    }

    public void register_user() {
        String email_str = input_email.getText().toString();
        String password_str = input_password.getText().toString();
        //make sure that the password and username is not empty
        if (email_str.isEmpty() || password_str.isEmpty()) {
            Context context = getApplicationContext();
            CharSequence text = "Username and password cannot be empty!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        //add this username and password on firebase



        //if the authentication is correct open the health record page
        Intent healthRecordIntent = new Intent(this, HealthRecordActivity.class);
        startActivity(healthRecordIntent);
    }

}