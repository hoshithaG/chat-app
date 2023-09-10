package com.pkd.mychat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pkd.mychat.MessageActivity;
import com.pkd.mychat.Model.User;
import com.pkd.mychat.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context context;
    private List<User> users;
    private  boolean isOnline;

    public UserAdapter(Context context, List<User> users, boolean isOnline){
        this.context = context;
        this.users = users;
        this.isOnline = isOnline;
    }

    public UserAdapter(Context context, List<User> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = users.get(position);
        holder.username.setText(user.getUsername());

        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context).load(user.getImageURL()).into(holder.profile_image);
        }

        if (isOnline){
            if (user.getStatus().equals("online")){
                holder.online.setVisibility(View.VISIBLE);
                holder.offline.setVisibility(View.GONE);
            }else{
                holder.offline.setVisibility(View.VISIBLE);
                holder.online.setVisibility(View.GONE);
            }
        }else {
            holder.online.setVisibility(View.GONE);
            holder.offline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView online;
        private ImageView offline;

        public ViewHolder(View itemView){
            super(itemView);

            username = itemView.findViewById(R.id.item_user);
            profile_image = itemView.findViewById(R.id.item_profile_image);
            online = itemView.findViewById(R.id.online);
            offline = itemView.findViewById(R.id.offline);

        }

    }

}
