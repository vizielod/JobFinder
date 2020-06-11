package com.example.jobfinder.Matches.EmployeeMatches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.R;

public class EmployeeMatchesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mMatchJobId, mMatchEmployerId, mMatchJobTitle, mMatchJobCategory;
    public ImageView mMatchJobImage;
    public Button mGetFileButton;
    public EmployeeMatchesViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchJobId = (TextView) itemView.findViewById(R.id.MatchId);
        mMatchJobTitle = (TextView) itemView.findViewById(R.id.MatchName);
        mMatchJobCategory = (TextView) itemView.findViewById(R.id.MatchProfession);
        mMatchEmployerId = (TextView) itemView.findViewById(R.id.EmployerId);

        mMatchJobImage = (ImageView) itemView.findViewById(R.id.MatchImage);
        mGetFileButton = (Button) itemView.findViewById(R.id.btn_getFile);
        mGetFileButton.setText("Info");

    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putString("matchId", mMatchJobId.getText().toString());
        b.putString("employerId", mMatchEmployerId.getText().toString());
        b.putString("userRole", "Employee");
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }


}