package com.example.ensa_meal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdapterMeals extends RecyclerView.Adapter<AdapterMeals.Holder> {
    ArrayList<Plat> plats;
    Context context;

    public AdapterMeals(ArrayList<Plat> plats, Context context) {
        this.plats = plats;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterMeals.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.model_plat,parent,false);


        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMeals.Holder holder, int position) {
     Plat p=plats.get(position);
     holder.tId.setText(p.getId());
     holder.tName.setText(p.getName());
     Glide.with(context).load(p.getImageURL()).into(holder.image);

   holder.itemView.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
           Intent intent=new Intent(context,Instructions.class);
           Bundle bundle=new Bundle();
           bundle.putSerializable("MEAL",p);
           intent.putExtras(bundle);
           context.startActivity(intent);
       }
   });

    }

    @Override
    public int getItemCount() {
        return plats.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView tId,tName;
        public Holder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.imageView);
            tId=itemView.findViewById(R.id.modelId);
            tName=itemView.findViewById(R.id.modelName);

        }
    }
}
