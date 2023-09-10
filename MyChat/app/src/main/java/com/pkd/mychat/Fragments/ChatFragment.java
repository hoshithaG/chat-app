package com.pkd.mychat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pkd.mychat.Adapter.UserAdapter;
import com.pkd.mychat.Model.Chat;
import com.pkd.mychat.Model.User;
import com.pkd.mychat.R;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference userReference;
    private DatabaseReference chatReference;
    private List<User> friends;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        recyclerView = view.findViewById(R.id.recyclerview_friends);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        friends = new ArrayList<>();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");

//      final Query querySendData = userReference.orderByChild("sender").equalTo(authUser.getUid());
//      final Query queryReceiveData = userReference.orderByChild("receiver").equalTo(authUser.getUid());

        getAllFriends();
        return view;
    }

    private void getAllFriends(){
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null && (chat.getSender().equals(authUser.getUid()) || chat.getReceiver().equals(authUser.getUid()))){
                        String friendId = chat.getSender().equals(authUser.getUid()) ? chat.getReceiver() : chat.getSender();
                        DatabaseReference friendUserReference = userReference.child(friendId);
                        friendUserReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                assert user != null;
                                friends.add(user);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                userAdapter  = new UserAdapter(getContext(),friends,true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
