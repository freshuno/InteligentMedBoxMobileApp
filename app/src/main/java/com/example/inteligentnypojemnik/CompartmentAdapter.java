package com.example.inteligentnypojemnik;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import java.util.List;

public class CompartmentAdapter extends RecyclerView.Adapter<CompartmentAdapter.CompartmentViewHolder> {

    private List<Compartment> compartmentList;
    private Context context;

    public CompartmentAdapter(Context context, List<Compartment> compartmentList) {
        this.context = context;
        this.compartmentList = compartmentList;
    }

    public interface OnItemClickListener {
        void onClick(Compartment c);
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener l) {
        this.onItemClickListener = l;
    }

    public interface OnCompartmentActiveChangedListener {
        void onCompartmentActiveChanged(String compartmentKey, boolean isActive);
    }
    private OnCompartmentActiveChangedListener activeChangedListener;
    public void setOnCompartmentActiveChangedListener(OnCompartmentActiveChangedListener listener) {
        this.activeChangedListener = listener;
    }

    @NonNull
    @Override
    public CompartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_compartment, parent, false);
        return new CompartmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompartmentViewHolder holder, int position) {
        Compartment compartment = compartmentList.get(position);

        holder.name.setText(compartment.getName());
        holder.time.setText(compartment.getTime());
        holder.medCount.setText(compartment.getMedCount());

        DeviceDetailsResponse.ContainerConfig config = new Gson()
                .fromJson(compartment.getExtraJson(), DeviceDetailsResponse.ContainerConfig.class);

        holder.activeSwitch.setOnCheckedChangeListener(null);
        holder.activeSwitch.setChecked(config != null && config.active);

        holder.activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (activeChangedListener != null) {
                activeChangedListener.onCompartmentActiveChanged(compartment.getCompartmentKey(), isChecked);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(compartment);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return compartmentList.size();
    }

    public static class CompartmentViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView time;
        TextView medCount;
        SwitchMaterial activeSwitch;

        public CompartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.compartment_name);
            time = itemView.findViewById(R.id.compartment_time);
            medCount = itemView.findViewById(R.id.compartment_med_count);
            activeSwitch = itemView.findViewById(R.id.compartment_active_switch);
        }
    }
}