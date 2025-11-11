package com.example.inteligentnypojemnik;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.WeekdayViewHolder> {

    private final List<String> weekdayList;
    private final Context context;
    private final String deviceName;
    private final int deviceId;
    private final String deviceJson;

    public WeekdayAdapter(Context context, List<String> weekdayList,
                          String deviceName, int deviceId, String deviceJson) {
        this.context = context;
        this.weekdayList = weekdayList;
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.deviceJson = deviceJson;
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
        holder.dayName.setText(dayName);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CompartmentScheduleActivity.class);

            intent.putExtra("DEVICE_NAME", deviceName);
            intent.putExtra("DEVICE_ID", deviceId);
            intent.putExtra("DEVICE_JSON", deviceJson);

            // mapowanie PL → ENG
            String[] keysPl = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
            String[] keysEn = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

            String mappedKey = "monday"; // fallback
            for (int i = 0; i < keysPl.length; i++) {
                if (keysPl[i].equals(dayName)) {
                    mappedKey = keysEn[i];
                    break;
                }
            }

            intent.putExtra("DAY_NAME", dayName);
            intent.putExtra("DAY_KEY", mappedKey);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return weekdayList.size();
    }

    public static class WeekdayViewHolder extends RecyclerView.ViewHolder {
        TextView dayName;

        public WeekdayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayName = itemView.findViewById(R.id.weekday_name);
        }
    }
}
