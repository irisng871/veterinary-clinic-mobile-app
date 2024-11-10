package com.example.veterinaryclinicmobileapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder> {

    private List<MedicalRecord> medicalRecordList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String appointmentId);
    }

    public MedicalRecordAdapter(Context context, List<MedicalRecord> medicalRecordList, OnItemClickListener listener) {
        this.context = context;
        this.medicalRecordList = medicalRecordList;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public MedicalRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointment, parent, false);
        return new MedicalRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalRecordViewHolder holder, int position) {
        MedicalRecord medicalRecord = medicalRecordList.get(position);
        holder.appointmentId.setText(medicalRecord.getAppointmentId());
        holder.petName.setText(medicalRecord.getPetName());
        holder.date.setText(medicalRecord.getDate());
        holder.time.setText(medicalRecord.getTime());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(medicalRecord.getAppointmentId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicalRecordList.size();
    }

    public static class MedicalRecordViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentId, petName, date, time;

        public MedicalRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentId = itemView.findViewById(R.id.appointmentId);
            petName = itemView.findViewById(R.id.petName);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
        }
    }
}
