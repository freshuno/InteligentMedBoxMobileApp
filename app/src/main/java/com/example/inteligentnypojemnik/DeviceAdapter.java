package com.example.inteligentnypojemnik;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList;
    private Context context;
    private boolean showPatientName;

    public DeviceAdapter(Context context, List<Device> deviceList, boolean showPatientName) {
        this.context = context;
        this.deviceList = deviceList;
        this.showPatientName = showPatientName;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);

        holder.deviceName.setText(device.getName());
        holder.deviceNextDose.setText("Następna dawka: " + device.getNextDose());
        holder.deviceMedCount.setText("Leków: " + device.getMedCount());
        holder.deviceStatus.setText(device.getStatus());

        if (showPatientName) {
            holder.patientName.setText(device.getPatientName());
            holder.patientName.setVisibility(View.VISIBLE);
        } else {
            holder.patientName.setVisibility(View.GONE);
        }

        // --- POCZĄTEK NOWEGO KODU ---
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Otwórz nowy ekran harmonogramu
                Intent intent = new Intent(context, DeviceScheduleActivity.class);
                // Przekaż nazwę pudełka do nagłówka
                intent.putExtra("DEVICE_NAME", device.getName());
                context.startActivity(intent);
            }
        });
        // --- KONIEC NOWEGO KODU ---
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceIcon;
        TextView deviceName;
        TextView patientName;
        ImageView deviceArrow;
        TextView deviceNextDose;
        TextView deviceMedCount;
        TextView deviceStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.device_icon);
            deviceName = itemView.findViewById(R.id.device_name);
            patientName = itemView.findViewById(R.id.patient_name_on_card);
            deviceArrow = itemView.findViewById(R.id.device_arrow);
            deviceNextDose = itemView.findViewById(R.id.device_next_dose);
            deviceMedCount = itemView.findViewById(R.id.device_med_count);
            deviceStatus = itemView.findViewById(R.id.device_status);
        }
    }
}