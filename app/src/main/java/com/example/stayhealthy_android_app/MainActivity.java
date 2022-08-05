package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private Button Login_btn;
    private Button register_btn;
    private EditText login_email;
    private EditText login_password;
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set up the buttons
        Login_btn = findViewById(R.id.button_login);
        register_btn = findViewById(R.id.button_sign_up);
        login_email = findViewById(R.id.edittext_text_email_address);
        login_password = findViewById(R.id.edittext_text_password);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
        String email_str = login_email.getText().toString().trim();
        String password_str = login_password.getText().toString().trim();
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
        //validate the password and username in the firebase authentication
        mAuth.signInWithEmailAndPassword(email_str,password_str).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    mDatabase.child("users").child(user.getUid()).child("email").setValue(user.getEmail());
                }
                startActivity(new Intent(this, HealthRecordActivity.class));
            } else {
                Toast.makeText(this,
                        "Please Check Your login Credentials",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}