package com.example.jobfinder.Cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;

import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<Cards> {
    Context context;

    public MyArrayAdapter(Context context, int resourceId, List<Cards> items){
        super(context, resourceId, items);
    }
    public View getView(int position, View convertView, ViewGroup parent){
        Cards card_item = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView age = (TextView) convertView.findViewById(R.id.age);
        TextView profession = (TextView) convertView.findViewById(R.id.subtitle);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        name.setText(card_item.getName() + ",");
        age.setText(card_item.getAge());
        profession.setText(card_item.getProfession());
        //Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);
        //image.setImageResource(R.mipmap.ic_launcher);
        switch(card_item.getProfileImageUrl()){
            case "default":
                Glide.with(convertView.getContext()).load(R.drawable.placeholder_img).into(image);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);
                break;
        }

        return convertView;

    }

}
