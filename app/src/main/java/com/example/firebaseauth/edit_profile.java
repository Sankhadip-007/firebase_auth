package com.example.firebaseauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class edit_profile extends AppCompatActivity {
   EditText ename,ephone;
   ImageView profilePic;
   FirebaseAuth fauth;
   FirebaseFirestore fstore;
   Button savebtn;
   FirebaseUser curruser;
    StorageReference storageref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data=getIntent();
        String name=data.getStringExtra("name");
        String phone=data.getStringExtra("phone");

        ename=findViewById(R.id.ename);
        ephone=findViewById(R.id.ephone);
        profilePic=findViewById(R.id.profilepic);
        savebtn=findViewById(R.id.edit_save);

        ename.setText(name);
        ephone.setText(phone);

        fauth=FirebaseAuth.getInstance();
        fstore= FirebaseFirestore.getInstance();
        curruser=fauth.getCurrentUser();
        storageref= FirebaseStorage.getInstance().getReference();

        StorageReference localstorage=storageref.child("users/"+fauth.getCurrentUser().getUid()+"/profile.jpg");
        localstorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profilePic);
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent opengallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(opengallery,6969);
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nm=ename.getText().toString();
                String ph=ephone.getText().toString();

                if(TextUtils.isEmpty(nm)){
                    ename.setError("Name is empty");
                    return;
                }
                if(TextUtils.isEmpty(ph)){
                    ephone.setError("Phone No. is empty");
                    return;
                }
                DocumentReference dr=fstore.collection("users").document(curruser.getUid());
                Map<String,Object> edited=new HashMap<>();
                edited.put("name",nm);
                edited.put("phone",ph);
                dr.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(edit_profile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        //finish();
                        startActivity(new Intent(edit_profile.this,MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(edit_profile.this, "Failed!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==6969){
            if(resultCode== Activity.RESULT_OK){
                Uri imageuri=data.getData();
                //profilepic.setImageURI(imageuri);


                uploadImage(imageuri);
            }
        }
    }
    public void uploadImage(Uri imageuri){
        //upload image to firebase storage
        StorageReference fileref=storageref.child("users/"+fauth.getCurrentUser().getUid()+"/profile.jpg");
        fileref.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(MainActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profilePic);
                        Toast.makeText(edit_profile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(edit_profile.this, "Failed to upload", Toast.LENGTH_SHORT).show();
            }
        });
    }
}