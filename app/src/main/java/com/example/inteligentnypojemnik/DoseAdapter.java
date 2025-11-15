package com.example.inteligentnypojemnik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DoseAdapter extends RecyclerView.Adapter<DoseAdapter.DoseViewHolder> {

    private List<TodayDose> doseList;
    private Context context;

    public DoseAdapter(Context context, List<TodayDose> doseList) {
        this.context = context;
        this.doseList = doseList;
    }

    @NonNull
    @Override
    public DoseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dose, parent, false);
        return new DoseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoseViewHolder holder, int position) {
        TodayDose dose = doseList.get(position);

        StringBuilder medText = new StringBuilder();
        for (MedicineDose med : dose.getMedicine()) {
            if (medText.length() > 0) {
                medText.append(", ");
            }
            medText.append(med.getName());

            String doseString = formatDoseToString(med.getDose());
            if (!doseString.isEmpty()) {
                medText.append(" (").append(doseString).append(")");
            }
        }

        holder.doseText.setText(dose.getTime() + " - " + medText.toString());
    }

    private String formatDoseToString(int dose) {
        if (dose <= 0) {
            return "";
        } else if (dose == 1) {
            return "1 kapsułka";
        } else if (dose >= 2 && dose <= 4) {
            return dose + " kapsułki";
        } else {
            return dose + " kapsułek";
        }
    }

    @Override
    public int getItemCount() {
        return doseList.size();
    }

    public static class DoseViewHolder extends RecyclerView.ViewHolder {
        TextView doseText;

        public DoseViewHolder(@NonNull View itemView) {
            super(itemView);
            doseText = itemView.findViewById(R.id.dose_text);
        }
    }
}