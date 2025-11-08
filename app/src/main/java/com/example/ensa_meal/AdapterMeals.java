package com.example.ensa_meal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * AdapterMeals - RecyclerView Adapter for displaying meal categories
 *
 * Responsibilities:
 * - Bind meal data to ViewHolder
 * - Handle click events to navigate to Instructions activity
 * - Handle long-click events to edit meals
 * - Load images using Glide library
 *
 * ViewHolder Pattern: Caches view references for performance
 */
public class AdapterMeals extends RecyclerView.Adapter<AdapterMeals.Holder> {
    private final ArrayList<Plat> plats;
    private final Context context;
    private final OnItemClickListener clickListener;

    /**
     * Interface for handling click events
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onAddToFavoritesClick(int position);
    }

    /**
     * Constructor
     * @param plats List of meals
     * @param context Activity context
     * @param clickListener Listener for click events
     */
    public AdapterMeals(ArrayList<Plat> plats, Context context, OnItemClickListener clickListener) {
        this.plats = plats;
        this.context = context;
        this.clickListener = clickListener;
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

        // Click listener - Open detail screen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });

        // Add to favorites button listener
        holder.btnAddToFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onAddToFavoritesClick(holder.getAdapterPosition());
                }
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
        ImageButton btnAddToFavorites;
        public Holder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.imageView);
            tId=itemView.findViewById(R.id.modelId);
            tName=itemView.findViewById(R.id.modelName);
            btnAddToFavorites = itemView.findViewById(R.id.btnAddToFavorites);
        }
    }
}
