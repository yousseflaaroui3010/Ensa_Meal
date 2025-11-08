package com.example.ensa_meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ensa_meal.database.FavoriteEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * FavoritesAdapter - RecyclerView Adapter for Favorites
 * Displays favorite meals with comments and action buttons
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final ArrayList<FavoriteEntity> favorites;
    private final Context context;
    private final OnFavoriteActionListener listener;

    /**
     * Interface for favorite actions
     */
    public interface OnFavoriteActionListener {
        void onEditComment(int position);
        void onViewDetails(int position);
    }

    public FavoritesAdapter(ArrayList<FavoriteEntity> favorites, Context context,
                            OnFavoriteActionListener listener) {
        this.favorites = favorites;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteEntity favorite = favorites.get(position);

        // Set meal name
        holder.mealName.setText(favorite.getMealName());

        // Set comment or placeholder
        if (favorite.getUserComment() != null && !favorite.getUserComment().isEmpty()) {
            holder.comment.setText(favorite.getUserComment());
            holder.comment.setVisibility(View.VISIBLE);
        } else {
            holder.comment.setText("No comment yet. Tap to add.");
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setAlpha(0.5f); // Make it look like placeholder
        }

        // Set timestamp
        String timeAgo = getTimeAgo(favorite.getAddedTimestamp());
        holder.timestamp.setText(timeAgo);

        // Load image
        Glide.with(context)
                .load(favorite.getMealImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.mealImage);

        // Click to view details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(holder.getAdapterPosition());
            }
        });

        // Edit comment button
        holder.btnEditComment.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditComment(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    /**
     * Calculate time ago from timestamp
     */
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " min" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView mealImage;
        TextView mealName, comment, timestamp;
        ImageButton btnEditComment;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImage = itemView.findViewById(R.id.favoriteMealImage);
            mealName = itemView.findViewById(R.id.favoriteMealName);
            comment = itemView.findViewById(R.id.favoriteComment);
            timestamp = itemView.findViewById(R.id.favoriteTimestamp);
            btnEditComment = itemView.findViewById(R.id.btnEditComment);
        }
    }
}
