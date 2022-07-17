package com.example.stayhealthy_android_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private Button register_btn;
    private EditText input_email;
    private EditText input_password;
    public boolean registered = false;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //link the input and button set up
        register_btn = findViewById(R.id.button_register);
        input_email = findViewById(R.id.editTextTextEmailAddress_register);
        input_password = findViewById(R.id.editTextTextPassword_register);
        mAuth = FirebaseAuth.getInstance();

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
        if(!Patterns.EMAIL_ADDRESS.matcher(email_str).matches()) {
            Context context = getApplicationContext();
            CharSequence text = "Enter the valid email address";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        if(password_str.length() < 6) {
            Context context = getApplicationContext();
            CharSequence text = "Length of the password should be more than 6";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        //add this username and password on firebase
        mAuth.createUserWithEmailAndPassword(email_str,password_str).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,"You are successfully Registered!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this,"Registration error! Please try again!",Toast.LENGTH_SHORT).show();
                }
            }
        });
            //if the authentication is correct open the health record page
        Intent backToLoginIntent = new Intent(this, MainActivity.class);
        startActivity(backToLoginIntent);
    }

}
