package com.example.jobfinder.Employer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class JobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private static final String LOGTAG = "UserRole";

    public TextView mJobId, mJobTitle, mEmployerId;
    public ImageView mJobImage;

    public Button mDeleteButton;
    private List<JobObject> jobsList;

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mJobsAdapter;
    private RecyclerView.LayoutManager mJobsLayoutManager;

    private String userRole;

    public JobViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);


        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();



        mJobId = (TextView) itemView.findViewById(R.id.JobId);
        mJobTitle = (TextView) itemView.findViewById(R.id.JobTitle);
        mEmployerId = (TextView) itemView.findViewById(R.id.EmployerId);

        mJobImage = (ImageView) itemView.findViewById(R.id.JobImage);
        mDeleteButton = (Button) itemView.findViewById(R.id.btn_deleteJob);

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