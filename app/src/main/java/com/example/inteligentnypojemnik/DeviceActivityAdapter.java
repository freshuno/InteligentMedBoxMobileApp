package com.example.inteligentnypojemnik;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeviceActivityAdapter extends RecyclerView.Adapter<DeviceActivityAdapter.ActivityViewHolder> {

    private List<DeviceActivity> activityList;
    private Context context;

    public DeviceActivityAdapter(Context context, List<DeviceActivity> activityList) {
        this.context = context;
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        DeviceActivity activity = activityList.get(position);

        holder.time.setText(activity.getTime());
        holder.description.setText("- " + activity.getDescription());

        if (activity.isError()) {
            holder.description.setTextColor(Color.RED);
            holder.icon.setColorFilter(Color.RED);
        } else {
            holder.description.setTextColor(Color.BLACK);
            holder.icon.setColorFilter(Color.parseColor("#4CAF50"));
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView time;
        TextView description;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.activity_icon);
            time = itemView.findViewById(R.id.activity_time);
            description = itemView.findViewById(R.id.activity_description);
        }
    }
}