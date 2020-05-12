package com.example.jobfinder.Cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;

import java.util.List;

public class MyJobCardArrayAdapter extends ArrayAdapter<JobCard> {

    Context context;
    public MyJobCardArrayAdapter(Context context, int resourceId, List<JobCard> items){
        super(context, resourceId, items);
    }
    public View getView(int position, View convertView, ViewGroup parent){
        JobCard jobCard_item = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        name.setText(jobCard_item.getJobTitle());
        //Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);
        //image.setImageResource(R.mipmap.ic_launcher);
        switch(jobCard_item.getJobImageUrl()){
            case "default":
                Glide.with(convertView.getContext()).load(R.mipmap.ic_launcher).into(image);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(jobCard_item.getJobImageUrl()).into(image);
                break;
        }


        return convertView;

    }

}
