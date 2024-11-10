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

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private Context context;
    private List<Patient> patientList;

    public PatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.petId.setText(patient.getId());
        holder.petName.setText(patient.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, vet_patient_details.class);
            intent.putExtra("petId", patient.getId());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView petId, petName;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            petId = itemView.findViewById(R.id.petId);
            petName = itemView.findViewById(R.id.petName);
        }
    }
}
