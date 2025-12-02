package com.example.inteligentnypojemnik;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList;
    private Context context;
    private boolean showPatientName;
    private OnDeviceDeleteListener deleteListener;

    public interface OnDeviceDeleteListener {
        void onDeleteClick(int deviceId, String deviceName);
    }

    public DeviceAdapter(Context context, List<Device> deviceList, boolean showPatientName, OnDeviceDeleteListener deleteListener) {
        this.context = context;
        this.deviceList = deviceList;
        this.showPatientName = showPatientName;
        this.deleteListener = deleteListener;
    }

    public DeviceAdapter(Context context, List<Device> deviceList, boolean showPatientName) {
        this(context, deviceList, showPatientName, null);
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

            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deviceArrow.setVisibility(View.GONE);
        } else {
            holder.patientName.setVisibility(View.GONE);
            holder.userInfoLayout.setVisibility(View.GONE);

            holder.deleteButton.setVisibility(View.GONE);
            holder.deviceArrow.setVisibility(View.VISIBLE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(device.getId(), device.getName());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DeviceScheduleActivity.class);
            intent.putExtra("DEVICE_NAME", device.getName());
            intent.putExtra("DEVICE_ID", device.getId());
            context.startActivity(intent);
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
        ImageButton deleteButton;
        TextView deviceNextDose;
        View userInfoLayout;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.device_icon);
            deviceName = itemView.findViewById(R.id.device_name);
            patientName = itemView.findViewById(R.id.patient_name_on_card);
            deviceArrow = itemView.findViewById(R.id.device_arrow);
            deleteButton = itemView.findViewById(R.id.button_delete_device);
            deviceNextDose = itemView.findViewById(R.id.device_next_dose);
            userInfoLayout = itemView.findViewById(R.id.user_info_layout);
        }
    }
}