package com.example.firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {
   EditText name,email,password,phone;
   Button btn;
   FirebaseAuth fauth;
   TextView loginhere,forgotpass;
   ProgressBar pg;
   FirebaseFirestore fstore;
   String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name=findViewById(R.id.name);
        password=findViewById(R.id.password);
        email=findViewById(R.id.email);
        phone=findViewById(R.id.phoneNo);
        btn=findViewById(R.id.regbtn);
        loginhere=findViewById(R.id.loginhere);
        forgotpass=findViewById(R.id.forgotpass);

        pg=findViewById(R.id.progressBar);
        fauth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();
        if(fauth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

          btn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              String em=email.getText().toString();
              String ps=password.getText().toString();
              String nm=name.getText().toString();
              String ph=phone.getText().toString();

              if(TextUtils.isEmpty(em)){
                  email.setError("Email is empty");
                  return;
              }
              if(TextUtils.isEmpty(ps)){
                  password.setError("Password is empty");
                  return;
              }
              if(ps.length()<5){
                  password.setError("Password length must be greater than 4");
                  return;
              }
            pg.setVisibility(View.VISIBLE);

              // register
              fauth.createUserWithEmailAndPassword(em,ps).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){

                       // send verification link
                        FirebaseUser fuser=fauth.getCurrentUser();
                        fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(register.this, "Verification link has been sent", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(register.this, "Error!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                       Toast.makeText(register.this, "user created", Toast.LENGTH_SHORT).show();
                       userID=fauth.getCurrentUser().getUid();
                       DocumentReference dr=fstore.collection("users").document(userID);
                       Map<String,Object> user=new HashMap<>();
                       user.put("name",nm);
                       user.put("email",em);
                       user.put("phone",ph);
                       user.put("password",password.getText().toString());
                       dr.set(user);
                       startActivity(new Intent(getApplicationContext(),MainActivity.class));
                   }
                   else{
                       Toast.makeText(register.this, "error!  "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                       pg.setVisibility(View.GONE);
                   }
                  }
              });

          }
      });



        loginhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),login.class));
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetmail=new EditText(view.getContext());
                AlertDialog.Builder passwordresetDialog=new AlertDialog.Builder(view.getContext());
                passwordresetDialog.setTitle("Reset password ?");
                passwordresetDialog.setMessage("Enter your email..");
                passwordresetDialog.setView(resetmail);

                passwordresetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    // extract the email
                        String temp_email=resetmail.getText().toString();
                        fauth.sendPasswordResetEmail(temp_email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(register.this, "reset link sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(register.this, "Error!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordresetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // close the dialog
                    }
                });
                passwordresetDialog.create().show();
            }
        });
    }
}