package com.example.jobfinder.Employer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;

import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class JobsAdapter extends RecyclerView.Adapter<JobViewHolder>{
    private List<JobObject> jobsList;
    private Context context;


    public JobsAdapter(List<JobObject> jobsList, Context context){
        this.jobsList = jobsList;
        this.context = context;
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        JobViewHolder rcv = new JobViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, int position) {
        holder.mJobId.setText(jobsList.get(position).getJobId());
        holder.mJobTitle.setText(jobsList.get(position).getJobTitle());
        if(!jobsList.get(position).getJobImageUrl().equals("default")){
            Glide.with(context).load(jobsList.get(position).getJobImageUrl()).into(holder.mJobImage);
        }
        else{
            Glide.with(context).load(R.mipmap.ic_launcher).into(holder.mJobImage);
        }
        holder.mEmployerId.setText(jobsList.get(position).getEmployerId());
    }

    @Override
    public int getItemCount() {
        return this.jobsList.size();
    }
}
