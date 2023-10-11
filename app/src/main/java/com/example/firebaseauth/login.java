package com.example.firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    EditText email,password;
    Button btn;
    FirebaseAuth fauth;
    TextView reghere;
    ProgressBar pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password=findViewById(R.id.password);
        email=findViewById(R.id.email);
        btn=findViewById(R.id.loginbtn);
        reghere=findViewById(R.id.reghere);
        pg=findViewById(R.id.progressBar2);
        fauth=FirebaseAuth.getInstance();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String em = email.getText().toString();
                String ps = password.getText().toString();
                if (TextUtils.isEmpty(em)) {
                    email.setError("Email is empty");
                    return;
                }
                if (TextUtils.isEmpty(ps)) {
                    password.setError("Password is empty");
                    return;
                }
                if (ps.length() < 5) {
                    password.setError("Password length must be greater than 4");
                    return;
                }
                pg.setVisibility(View.VISIBLE);

                // authenticate user
                fauth.signInWithEmailAndPassword(em, ps).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(login.this, "logged in", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(login.this, "error!  " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            pg.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        reghere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),register.class));
            }
        });
    }
}