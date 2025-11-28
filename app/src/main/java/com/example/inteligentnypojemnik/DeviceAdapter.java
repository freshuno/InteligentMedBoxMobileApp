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

        if (showPatientName) {
            holder.patientName.setText(device.getPatientName());
            holder.patientName.setVisibility(View.VISIBLE);

            holder.userInfoLayout.setVisibility(View.VISIBLE);
            holder.deviceNextDose.setText("Użytkownik: " + device.getNextDose());
        } else {
            holder.patientName.setVisibility(View.GONE);
            holder.userInfoLayout.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DeviceScheduleActivity.class);
                intent.putExtra("DEVICE_NAME", device.getName());
                intent.putExtra("DEVICE_ID", device.getId());
                context.startActivity(intent);
            }
        });
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
        View userInfoLayout;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.device_icon);
            deviceName = itemView.findViewById(R.id.device_name);
            patientName = itemView.findViewById(R.id.patient_name_on_card);
            deviceArrow = itemView.findViewById(R.id.device_arrow);
            deviceNextDose = itemView.findViewById(R.id.device_next_dose);
            userInfoLayout = itemView.findViewById(R.id.user_info_layout);
        }
    }
}