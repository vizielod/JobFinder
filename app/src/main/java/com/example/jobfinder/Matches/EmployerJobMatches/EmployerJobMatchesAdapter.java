package com.example.jobfinder.Matches.EmployerJobMatches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;

import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployerJobMatchesAdapter extends RecyclerView.Adapter<EmployerJobMatchesViewHolder>{
    private List<MatchesEmployeeObject> matchesList;
    private Context context;


    public EmployerJobMatchesAdapter(List<MatchesEmployeeObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public EmployerJobMatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        EmployerJobMatchesViewHolder rcv = new EmployerJobMatchesViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(EmployerJobMatchesViewHolder holder, int position) {
        holder.mMatchId.setText(matchesList.get(position).getUserId());
        holder.mMatchName.setText(matchesList.get(position).getName());
        if(!matchesList.get(position).getProfileImageUrl().equals("default")){
            Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
        }
        holder.mMatchJobId.setText(matchesList.get(position).getJobId());
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
