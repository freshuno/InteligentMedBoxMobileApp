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
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import java.util.List;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.WeekdayViewHolder> {

    private final List<String> weekdayList;
    private final Context context;
    private final String deviceName;
    private final int deviceId;
    private DeviceDetailsResponse.Configuration fullConfiguration;
    private Gson gson = new Gson();

    public interface OnDayActiveChangedListener {
        void onDayActiveChanged(String dayKey, boolean isActive, DeviceDetailsResponse.Configuration newConfig);
    }
    private OnDayActiveChangedListener activeChangedListener;

    // Pomocnicza klasa-wrapper dla GSON
    private static class DeviceDetailsWrapper {
        public DeviceDetailsResponse.Configuration configuration;
        public DeviceDetailsWrapper(DeviceDetailsResponse.Configuration config) {
            this.configuration = config;
        }
    }

    public WeekdayAdapter(Context context, List<String> weekdayList,
                          String deviceName, int deviceId, String deviceJson,
                          OnDayActiveChangedListener listener) {
        this.context = context;
        this.weekdayList = weekdayList;
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.fullConfiguration = gson.fromJson(deviceJson, DeviceDetailsResponse.class).configuration;
        this.activeChangedListener = listener;
    }

    @NonNull
    @Override
    public WeekdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_weekday, parent, false);
        return new WeekdayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekdayViewHolder holder, int position) {
        String dayName = weekdayList.get(position);
        String dayKey = getDayKeyFromName(dayName);
        holder.dayName.setText(dayName);

        DeviceDetailsResponse.DayConfig dayConfig = fullConfiguration.get(dayKey);

        holder.activeSwitch.setOnCheckedChangeListener(null);
        holder.activeSwitch.setChecked(dayConfig != null && dayConfig.active);

        holder.activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceDetailsResponse.DayConfig config = fullConfiguration.get(dayKey);
            if (config != null) {
                config.active = isChecked;
                if (activeChangedListener != null) {
                    activeChangedListener.onDayActiveChanged(dayKey, isChecked, fullConfiguration);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CompartmentScheduleActivity.class);

            intent.putExtra("DEVICE_NAME", deviceName);
            intent.putExtra("DEVICE_ID", deviceId);
            intent.putExtra("DEVICE_JSON", gson.toJson(new DeviceDetailsWrapper(fullConfiguration)));

            intent.putExtra("DAY_NAME", dayName);
            intent.putExtra("DAY_KEY", dayKey);

            // Używamy startActivityForResult, aby móc odświeżyć ten ekran po powrocie
            if (context instanceof DeviceScheduleActivity) {
                ((DeviceScheduleActivity) context).startActivity(intent);
            } else {
                context.startActivity(intent);
            }
        });
    }

    public void updateConfiguration(DeviceDetailsResponse.Configuration newConfig) {
        this.fullConfiguration = newConfig;
        notifyDataSetChanged();
    }

    private String getDayKeyFromName(String dayName) {
        String[] keysPl = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        String[] keysEn = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (int i = 0; i < keysPl.length; i++) {
            if (keysPl[i].equals(dayName)) {
                return keysEn[i];
            }
        }
        return "monday";
    }

    @Override
    public int getItemCount() {
        return weekdayList.size();
    }

    public static class WeekdayViewHolder extends RecyclerView.ViewHolder {
        TextView dayName;
        SwitchMaterial activeSwitch;
        ImageView weekdayArrow;

        public WeekdayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayName = itemView.findViewById(R.id.weekday_name);
            activeSwitch = itemView.findViewById(R.id.weekday_active_switch);
            weekdayArrow = itemView.findViewById(R.id.weekday_arrow);
        }
    }
}