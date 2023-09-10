package com.pkd.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pkd.mychat.Adapter.MessageAdapter;
import com.pkd.mychat.Model.Chat;
import com.pkd.mychat.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    Intent intent;
    TextView username;
    TextView btnSend;
    EditText typingBox;
    RecyclerView recyclerView;
    CircleImageView profileImage;
    ValueEventListener seenListener;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    MessageAdapter messageAdapter;
    List<Chat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.tbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.user);
        btnSend = findViewById(R.id.send);
        typingBox = findViewById(R.id.msg);

        recyclerView = findViewById(R.id.recycle_view_chats);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        final String userId = intent.getStringExtra("userId");
        loadUserTitleAndImage(userId);

        btnSend.setOnClickListener(view -> {

            String msg = typingBox.getText().toString();
            if (!msg.equals("")){
                sendMessage(firebaseUser.getUid(),userId, msg);
                btnSend.setBackgroundResource(R.drawable.msg_delivered_bg);
            }else {
                Toast.makeText(getApplicationContext(),"Cannot Send Empty Message.",Toast.LENGTH_SHORT).show();
                btnSend.setBackgroundResource(R.drawable.msg_info_bg);
            }
            typingBox.setText("");
        });

        seenMessage(userId);

    }

    private void loadUserTitleAndImage(String userId) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());

                if (user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profileImage);
                }

                readMessage(firebaseUser.getUid(),userId, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String userId){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen",true);
                     //   chat.setSean(true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(String sender, String receiver, String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isSeen",false);
        hashMap.put("lastSeenAt", ServerValue.TIMESTAMP);

        reference.child("Chats").push().setValue(hashMap);
    }

    public void readMessage(final String myId, final String userId, final String imageURL){

        chats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chats.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    chat.setId(ds.getKey());
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        chats.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, chats, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                    btnSend.setBackgroundResource(R.drawable.msg_info_bg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            int position = messageAdapter.getContextMenuPosition();
            chats.remove(position);
            messageAdapter.notifyItemRemoved(position);
            return true;
        }catch (Exception e){
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }


}
