package com.example.veterinaryclinicmobileapplication;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class BreedAdapter extends RecyclerView.Adapter<BreedAdapter.BreedViewHolder> {

    private Context context;
    private ArrayList<Breed> breedList;
    private OnBreedClickListener onBreedClickListener;

    public BreedAdapter(Context context, ArrayList<Breed> breedList, OnBreedClickListener onBreedClickListener) {
        this.context = context;
        this.breedList = breedList;
        this.onBreedClickListener = onBreedClickListener;
    }

    @Override
    public BreedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.breed, parent, false);
        return new BreedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreedViewHolder holder, int position) {
        Breed breed = breedList.get(position);

        holder.breedName.setText(breed.getName());
        Glide.with(context)
                .load(breed.getImageUrl())
                .into(holder.breedImage);

        holder.itemView.setOnClickListener(v -> {
            if (onBreedClickListener != null) {
                onBreedClickListener.onBreedClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return breedList.size();
    }

    public static class BreedViewHolder extends RecyclerView.ViewHolder {
        TextView breedName;
        ImageView breedImage;
        RelativeLayout breedSelection;

        public BreedViewHolder(View itemView) {
            super(itemView);
            breedName = itemView.findViewById(R.id.breedName);
            breedImage = itemView.findViewById(R.id.breedImage);
            breedSelection = itemView.findViewById(R.id.breedSelection);
        }
    }

    public String getBreedNameAt(int position) {
        return breedList.get(position).getName();
    }

    public interface OnBreedClickListener {
        void onBreedClick(int position);
    }
}