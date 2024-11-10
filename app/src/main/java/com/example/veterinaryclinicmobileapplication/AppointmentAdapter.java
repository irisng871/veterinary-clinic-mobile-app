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

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.BookingViewHolder> {
    private Context context;
    private List<Appointment> appointmentList;
    private String pageType;

    public AppointmentAdapter(Context context, List<Appointment> appointmentList, String pageType) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.pageType = pageType;
    }

    @Override
    public BookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookingViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.appointmentId.setText(appointment.getAppointmentId());
        holder.petName.setText(appointment.getPetName());
        holder.date.setText(appointment.getDate());
        holder.time.setText(appointment.getTime());

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            switch (pageType) {
                case "vet":
                    intent = new Intent(context, vet_appointment_details.class);
                    break;
                case "history":
                    intent = new Intent(context, appointment_history_details.class);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + pageType);
            }

            intent.putExtra("appointmentId", appointment.getAppointmentId());
            intent.putExtra("petName", appointment.getPetName());
            intent.putExtra("date", appointment.getDate());
            intent.putExtra("time", appointment.getTime());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentId, petName, date, time;

        public BookingViewHolder(View itemView) {
            super(itemView);
            appointmentId = itemView.findViewById(R.id.appointmentId);
            petName = itemView.findViewById(R.id.petName);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
        }
    }
}