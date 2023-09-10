package com.pkd.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pkd.mychat.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username,email, password;
    Button register;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        username = (MaterialEditText)findViewById(R.id.txt_username);
        email = (MaterialEditText)findViewById(R.id.txt_email);
        password = (MaterialEditText)findViewById(R.id.txt_password);
        register = (Button)findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String eml = email.getText().toString();
                String pass = password.getText().toString();

                if (user.isEmpty() || eml.isEmpty() || pass.isEmpty()){
                    Toast.makeText(getApplicationContext(),"All Fields are Required.",Toast.LENGTH_SHORT).show();
                }else if(pass.length()<6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 Characters",Toast.LENGTH_SHORT).show();
                }else{
                    getRegister(user,eml,pass);
                }
            }
        });

    }

    public void getRegister(final String user, String email, String pass){

        auth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username",user);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", user.toLowerCase());

                            reference.setValue(hashMap);

                            // Read from the database
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User value = dataSnapshot.getValue(User.class);
                                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    System.out.println(">>> error: " + databaseError);
                                }
                            });

//                            reference.addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()){
//                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        startActivity(intent);
//                                        finish();
//
//                                    }
//                                }
//                            });

                        }else{
                            Toast.makeText(getApplicationContext(),"Registration Error with email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
