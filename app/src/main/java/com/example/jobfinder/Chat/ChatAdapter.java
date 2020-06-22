package com.example.jobfinder.Chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;

import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder>{
    private List<ChatObject> chatList;
    private Context context;


    public ChatAdapter(List<ChatObject> chatList, Context context){
        this.chatList = chatList;
        this.context = context;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        ChatViewHolder rcv = new ChatViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        //holder.mMessage.setText(chatList.get(position).getMessage());
        if(chatList.get(position).getCurrentUser()){
            //holder.mMessage.setGravity(Gravity.END);
            holder.mSenderMessage.setText(chatList.get(position).getMessage());
            holder.mSenderContainer.setVisibility(View.VISIBLE);
            holder.mRecievedContainer.setVisibility(View.GONE);

            //holder.mMessage.setTextColor(Color.parseColor("#404040"));
            //holder.mContainer.setBackgroundColor(Color.parseColor("#F4F4F4"));
        }else{
            holder.mReceivedMessage.setText(chatList.get(position).getMessage());
            holder.mRecievedContainer.setVisibility(View.VISIBLE);
            holder.mSenderContainer.setVisibility(View.GONE);
            //holder.mMessage.setGravity(Gravity.START);


            //holder.mMessage.setTextColor(Color.parseColor("#FFFFFF"));
            //holder.mContainer.setBackgroundColor(Color.parseColor("#2DB4C8"));
        }

    }

    @Override
    public int getItemCount() {
        return this.chatList.size();
    }
}
