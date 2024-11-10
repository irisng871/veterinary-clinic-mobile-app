package com.example.veterinaryclinicmobileapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VetAdapter extends RecyclerView.Adapter<VetAdapter.VetViewHolder> {

    private Context context;
    private ArrayList<Vet> vetList;
    private OnVetClickListener onVetClickListener;
    private int selectedPosition = -1;

    public VetAdapter(Context context, ArrayList<Vet> vetList, OnVetClickListener onVetClickListener) {
        this.context = context;
        this.vetList = vetList;
        this.onVetClickListener = onVetClickListener;
    }

    @NonNull
    @Override
    public VetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vet, parent, false);
        return new VetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VetViewHolder holder, int position) {
        Vet vet = vetList.get(position);

        holder.vetNameTextView.setText(vet.getName());
        holder.specialtyAreaTextView.setText(vet.getSpecialtyArea());
        Picasso.get().load(vet.getImageUrl()).into(holder.vetImageView);

        if (holder.getAdapterPosition() == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#d4d4d4"));
        }

        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();

            if (clickedPosition != RecyclerView.NO_POSITION) {
                selectedPosition = clickedPosition;
                notifyDataSetChanged();
                if (onVetClickListener != null) {
                    onVetClickListener.onVetClick(vetList.get(clickedPosition).getName());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return vetList.size();
    }

    public static class VetViewHolder extends RecyclerView.ViewHolder {

        TextView vetNameTextView;
        TextView specialtyAreaTextView;
        ImageView vetImageView;

        public VetViewHolder(@NonNull View itemView) {
            super(itemView);
            vetNameTextView = itemView.findViewById(R.id.vetName);
            specialtyAreaTextView = itemView.findViewById(R.id.specialty_area);
            vetImageView = itemView.findViewById(R.id.vetImage);
        }
    }

    public interface OnVetClickListener {
        void onVetClick(String vetName);
    }
}