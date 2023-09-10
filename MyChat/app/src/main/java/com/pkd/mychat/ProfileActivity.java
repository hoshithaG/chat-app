package com.pkd.mychat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.pkd.mychat.Model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView user_image;
    EditText username, email;
    Button update, updatePassword, deleteAccount;

    DatabaseReference reference;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    private static final int IMAGE_REQUEST_CODE = 1;
    private Uri imageUri;
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,HomeActivity.class);
                startActivity(intent);

            }
        });


        user_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.user_name);
        email = findViewById(R.id.user_email);
        update = findViewById(R.id.btn_update);
        updatePassword = findViewById(R.id.btn_updatePassword);
        deleteAccount = findViewById(R.id.btn_deleteAccount);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                email.setText(firebaseUser.getEmail());

                if (user.getImageURL().equals("default")){
                    user_image.setImageResource(R.mipmap.ic_launcher);

                }else {
                    Glide.with(getBaseContext()).load(user.getImageURL()).into(user_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });



    }

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST_CODE);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading..");
        pd.show();

        if(imageUri!=null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+'.'+getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String uri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL",uri);
                        reference.updateChildren(hashMap);

                        pd.dismiss();
                    }else{
                        Toast.makeText(getApplicationContext(),"Photo Uploading Failed.",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        }else {
            Toast.makeText(getApplicationContext(),"No Photo Selected.",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){

            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(),"Uploading..Please wait..",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }

        }

    }
}
