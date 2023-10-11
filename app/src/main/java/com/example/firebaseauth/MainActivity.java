package com.example.firebaseauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    TextView name,email,phone,verifymessage,editProfile;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    String uid;
    Button resetpass,verifybtn;
    FirebaseUser curruser;
    ImageView profilepic;
    StorageReference storageref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name=findViewById(R.id.ename);
        email=findViewById(R.id.pemail);
        phone=findViewById(R.id.ephone);
        resetpass=findViewById(R.id.button2);
        fauth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();
        verifybtn=findViewById(R.id.verifybtn);
        verifymessage=findViewById(R.id.verifymessage);
        profilepic=findViewById(R.id.profilepic);
        editProfile=findViewById(R.id.textView3);

        uid=fauth.getCurrentUser().getUid();
        curruser=FirebaseAuth.getInstance().getCurrentUser();
        storageref= FirebaseStorage.getInstance().getReference();

        StorageReference localstorage=storageref.child("users/"+fauth.getCurrentUser().getUid()+"/profile.jpg");
        localstorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
             Picasso.get().load(uri).into(profilepic);
            }
        });

        if(!curruser.isEmailVerified()){
           verifybtn.setVisibility(View.VISIBLE);
           verifymessage.setVisibility(View.VISIBLE);

           verifybtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                   curruser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void unused) {
                           Toast.makeText(MainActivity.this, "Verification link has been sent", Toast.LENGTH_SHORT).show();
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(MainActivity.this, "Error!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   });
               }
           });

        }

        DocumentReference dr=fstore.collection("users").document(uid);
        dr.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
            phone.setText(value.getString("phone"));
            email.setText(value.getString("email"));
            name.setText(value.getString("name"));
            }
        });

        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText popupwindow=new EditText(view.getContext());
                final AlertDialog.Builder passwordresetDialog=new AlertDialog.Builder(view.getContext());
                passwordresetDialog.setTitle("Reset password ?");
                passwordresetDialog.setMessage("Enter new password..");
                passwordresetDialog.setView(popupwindow);

               passwordresetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       String newpass=popupwindow.getText().toString();
                       curruser.updatePassword(newpass).addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void unused) {
                               Toast.makeText(MainActivity.this, "password reset successfully", Toast.LENGTH_SHORT).show();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(MainActivity.this, "Password reset failed!", Toast.LENGTH_SHORT).show();
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

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent opengallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(opengallery,6969);
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(view.getContext(),edit_profile.class);
                i.putExtra("name",name.getText().toString());
                //i.putExtra("email",email.getText().toString());
                i.putExtra("phone",phone.getText().toString());
                startActivity(i);
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

    public void logout(View view){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),login.class));
            finish();
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
                    Picasso.get().load(uri).into(profilepic);
                    Toast.makeText(MainActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                        }
            });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
            }
        });
        }
}