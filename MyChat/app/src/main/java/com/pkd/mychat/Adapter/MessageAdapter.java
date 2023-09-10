package com.pkd.mychat.Adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.pkd.mychat.Model.Chat;
import com.pkd.mychat.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final int MSG_TYPE_ME = 0;
    public static final int MSG_TYPE_FRIEND = 1;

    private Context context;
    private List<Chat> chats;
    private String imageURL;
    private int contextMenuPosition;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> chats, String imageURL){
        this.context = context;
        this.chats = chats;
        this.imageURL = imageURL;
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    public void setContextMenuPosition(int contextMenuPosition) {
        this.contextMenuPosition = contextMenuPosition;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_ME) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_me_layout, parent, false);
            return new MessageAdapter.ViewHolder(view, MSG_TYPE_ME);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_friend_layout, parent, false);
            return new MessageAdapter.ViewHolder(view, MSG_TYPE_FRIEND);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = chats.get(position);
        holder.show_message.setText(chat.getMessage());
        holder.messageId = chat.getId();

        if (imageURL.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(context).load(imageURL).into(holder.profile_image);
        }

//        Toast.makeText(context,"Seen: "+chat.isIsseen(),Toast.LENGTH_SHORT).show();
        if (position == chats.size()-1){
            if(chat.isSeen()){
                holder.msg_info.setText("Seen");
                holder.ic_seen.setVisibility(View.VISIBLE);
                holder.ic_delivered.setVisibility(View.GONE);

            }else {
                holder.msg_info.setText("Delivered");
                holder.ic_delivered.setVisibility(View.VISIBLE);
                holder.ic_seen.setVisibility(View.GONE);

            }
        }else {
            holder.msg_info.setVisibility(View.GONE);
            holder.ic_delivered.setVisibility(View.GONE);
            holder.ic_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public TextView show_message;
        public ImageView profile_image;
        public TextView msg_info;
        public ImageView ic_delivered;
        public ImageView ic_seen;
        public String messageId;


        public ViewHolder(View itemView, int userType){
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            msg_info = itemView.findViewById(R.id.msg_info);
            ic_delivered = itemView.findViewById(R.id.ic_msg_delivered);
            ic_seen = itemView.findViewById(R.id.ic_msg_seen);

            if (userType == MSG_TYPE_ME){
                itemView.setOnCreateContextMenuListener(this);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, view.getId(), 0, "Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    FirebaseDatabase.getInstance().getReference("Chats").child(messageId).removeValue();
                    setContextMenuPosition(getAdapterPosition());
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_ME;
        }else {
            return MSG_TYPE_FRIEND;
        }
    }
}