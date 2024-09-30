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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdoptablePetAdapter extends RecyclerView.Adapter<AdoptablePetAdapter.AdoptablePetViewHolder> {

    private Context context;
    private List<AdoptablePet> petList;

    public AdoptablePetAdapter(Context context, List<AdoptablePet> petList) {
        this.context = context;
        this.petList = petList;
    }

    @NonNull
    @Override
    public AdoptablePetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pet, parent, false);
        return new AdoptablePetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdoptablePetViewHolder holder, int position) {
        AdoptablePet pet = petList.get(position);

        holder.petName.setText(pet.getName());
        Glide.with(context)
                .load(pet.getImageUrl())
                .into(holder.petImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, staff_pet_profile.class);
            intent.putExtra("id", pet.getId());

            Log.d("AdoptablePetAdapter", "Passing pet ID: " + pet.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class AdoptablePetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImage;
        TextView petName;

        public AdoptablePetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.petImage);
            petName = itemView.findViewById(R.id.petName);
        }
    }
}

