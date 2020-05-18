package com.example.jobfinder.Employer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.R;

public class JobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private static final String LOGTAG = "UserRole";

    public TextView mJobId, mJobTitle, mEmployerId;
    public ImageView mJobImage;
    public JobViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mJobId = (TextView) itemView.findViewById(R.id.JobId);
        mJobTitle = (TextView) itemView.findViewById(R.id.JobTitle);
        mEmployerId = (TextView) itemView.findViewById(R.id.EmployerId);

        mJobImage = (ImageView) itemView.findViewById(R.id.JobImage);
    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), JobMainActivity.class);
        Log.i(LOGTAG, mJobId.getText().toString());
        Bundle b = new Bundle();
        b.putString("jobId", mJobId.getText().toString());
        b.putString("employerId", mEmployerId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}