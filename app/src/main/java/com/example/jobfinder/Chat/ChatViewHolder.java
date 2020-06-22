package com.example.jobfinder.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.R;

import org.w3c.dom.Text;

public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mSenderMessage, mReceivedMessage;
    public LinearLayout mSenderContainer, mRecievedContainer;

    public ChatViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mSenderMessage = itemView.findViewById(R.id.senderMessage);
        mSenderContainer = itemView.findViewById(R.id.senderContainer);

        mReceivedMessage = itemView.findViewById(R.id.receivedMessage);
        mRecievedContainer = itemView.findViewById(R.id.receivedContainer);
    }

    @Override
    public void onClick(View view) {
    }
}