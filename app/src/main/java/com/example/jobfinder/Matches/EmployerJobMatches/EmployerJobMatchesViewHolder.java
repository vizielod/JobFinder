package com.example.jobfinder.Matches.EmployerJobMatches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.R;

public class EmployerJobMatchesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mMatchId, mMatchJobId, mMatchName;
    public ImageView mMatchImage;

    public EmployerJobMatchesViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = (TextView) itemView.findViewById(R.id.Matchid);
        mMatchName = (TextView) itemView.findViewById(R.id.MatchName);
        mMatchJobId = (TextView) itemView.findViewById(R.id.JobId);

        mMatchImage = (ImageView) itemView.findViewById(R.id.MatchImage);

    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putString("matchId", mMatchId.getText().toString());
        b.putString("jobId", mMatchJobId.getText().toString());
        b.putString("userRole", "Employer");
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}