package com.example.jobfinder.Matches.EmployeeMatches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesViewHolder;
import com.example.jobfinder.R;

import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployeeMatchesAdapter extends RecyclerView.Adapter<EmployeeMatchesViewHolder>{
    private List<MatchesJobObject> matchesList;
    private Context context;


    public EmployeeMatchesAdapter(List<MatchesJobObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public EmployeeMatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        EmployeeMatchesViewHolder rcv = new EmployeeMatchesViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(EmployeeMatchesViewHolder holder, int position) {
        holder.mMatchJobId.setText(matchesList.get(position).getJobId());
        holder.mMatchJobTitle.setText(matchesList.get(position).getJobTitle());
        if(!matchesList.get(position).getJobImageUrl().equals("default")){
            Glide.with(context).load(matchesList.get(position).getJobImageUrl()).into(holder.mMatchJobImage);
        }
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
