package com.example.jobfinder.Cards;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Employee.EmployeeMainActivity;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MyJobCardArrayAdapter extends ArrayAdapter<JobCard> {
    Context context;

    public MyJobCardArrayAdapter(Context context, int resourceId, List<JobCard> items){
        super(context, resourceId, items);
    }
    public View getView(int position, View convertView, final ViewGroup parent){
        final JobCard jobCard_item = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView age = (TextView) convertView.findViewById(R.id.age);
        TextView category = (TextView) convertView.findViewById(R.id.subtitle);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        name.setText(jobCard_item.getJobTitle());
        category.setText(jobCard_item.getJobCategory());
        age.setText("");
        //Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);
        //image.setImageResource(R.mipmap.ic_launcher);
        switch(jobCard_item.getJobImageUrl()){
            case "default":
                Glide.with(convertView.getContext()).load(R.drawable.placeholder_img).into(image);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(jobCard_item.getJobImageUrl()).into(image);
                break;
        }

        return convertView;

    }

}
