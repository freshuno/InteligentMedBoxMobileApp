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

    private List<String> weekdayList;
    private Context context;
    private String deviceName; // DODANE POLE

    // ZAKTUALIZOWANY KONSTRUKTOR
    public WeekdayAdapter(Context context, List<String> weekdayList, String deviceName) {
        this.context = context;
        this.weekdayList = weekdayList;
        this.deviceName = deviceName; // DODANE POLE
    }

    @NonNull
    @Override
    public WeekdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weekday, parent, false);
        return new WeekdayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekdayViewHolder holder, int position) {
        String day = weekdayList.get(position);
        holder.dayName.setText(day);

        // ZAKTUALIZOWANY ONCLICK
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Otwórz nowy ekran przegród
                Intent intent = new Intent(context, CompartmentScheduleActivity.class);
                // Przekaż obie nazwy
                intent.putExtra("DEVICE_NAME", deviceName);
                intent.putExtra("DAY_NAME", day);
                context.startActivity(intent);
            }
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