package com.example.veterinaryclinicmobileapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecommendPetAdapter extends RecyclerView.Adapter<RecommendPetAdapter.RecommendedPetViewHolder> {

    private Context context;
    private List<RecommendPet> petList;

    public RecommendPetAdapter(Context context, List<RecommendPet> petList) {
        this.context = context;
        this.petList = petList;
    }

    @NonNull
    @Override
    public RecommendedPetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recommend, parent, false);
        return new RecommendedPetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedPetViewHolder holder, int position) {
        RecommendPet pet = petList.get(position);
        holder.petName.setText(pet.getName());

        Glide.with(context)
                .load(pet.getImageUrl())
                .into(holder.petImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, recommend_adoptable_pet_details.class);
            intent.putExtra("id", pet.getId());
            Log.d("RecommendedPetAdapter", "Passing pet ID: " + pet.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class RecommendedPetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImage;
        TextView petName;

        public RecommendedPetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.petImage);
            petName = itemView.findViewById(R.id.petName);
        }
    }
}
