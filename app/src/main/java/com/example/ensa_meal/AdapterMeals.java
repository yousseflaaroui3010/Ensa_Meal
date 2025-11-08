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
import java.util.HashSet;
import java.util.Set;

/**
 * AdapterMeals - RecyclerView Adapter for displaying meal categories
 *
 * Responsibilities:
 * - Bind meal data to ViewHolder
 * - Handle click events to navigate to Instructions activity
 * - Handle long-click events to edit meals
 * - Load images using Glide library
 */
public class AdapterMeals extends RecyclerView.Adapter<AdapterMeals.Holder> {
    private final ArrayList<Plat> plats;
    private final Context context;
    private final OnItemClickListener clickListener;
    private Set<String> favoriteMealIds;

    /**
     * Interface for handling click events
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onToggleFavoriteClick(int position);
    }

    /**
     * Constructor
     * @param plats List of meals
     * @param context Activity context
     * @param clickListener Listener for click events
     * @param favoriteMealIds Set of favorite meal IDs
     */
    public AdapterMeals(ArrayList<Plat> plats, Context context, OnItemClickListener clickListener, Set<String> favoriteMealIds) {
        this.plats = plats;
        this.context = context;
        this.clickListener = clickListener;
        this.favoriteMealIds = favoriteMealIds;
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

        // Set favorite icon state
        if (favoriteMealIds.contains(p.getId())) {
            holder.favoriteIcon.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.favoriteIcon.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Click listener - Open detail screen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });

        // Toggle favorite button listener
        holder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onToggleFavoriteClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return plats.size();
    }

    /**
     * Update the set of favorite meal IDs and refresh the adapter.
     * @param favoriteMealIds The new set of favorite meal IDs.
     */
    public void setFavoriteMealIds(Set<String> favoriteMealIds) {
        this.favoriteMealIds = favoriteMealIds;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView tId,tName;
        ImageView favoriteIcon; // Changed from ImageButton to ImageView
        public Holder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.imageView);
            tId=itemView.findViewById(R.id.modelId);
            tName=itemView.findViewById(R.id.modelName);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon); // Changed ID
        }
    }
}
