package com.example.inteligentnypojemnik;

import android.content.Context;
import android.graphics.Color; // Dodano import
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException; // Dodano import
import java.text.SimpleDateFormat; // Dodano import
import java.util.Calendar; // Dodano import
import java.util.Date; // Dodano import
import java.util.List;
import java.util.Locale; // Dodano import

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

        // --- NOWA LOGIKA: ZMIANA KOLORU JEŚLI MINĄŁ CZAS ---
        if (isTimePassed(dose.getTime())) {
            // Styl dla leków przeterminowanych (Wyszarzony)
            holder.doseText.setBackgroundColor(Color.parseColor("#E0E0E0")); // Jasny szary
            holder.doseText.setTextColor(Color.parseColor("#757575"));     // Ciemny szary tekst
        } else {
            // Styl domyślny (Zielony - taki jak w themes.xml)
            holder.doseText.setBackgroundColor(Color.parseColor("#A5D6A7")); // Oryginalny zielony
            holder.doseText.setTextColor(Color.BLACK);                       // Czarny tekst
        }
        // ----------------------------------------------------
    }

    // Metoda pomocnicza sprawdzająca czy minęło 15 minut od czasu leku
    private boolean isTimePassed(String doseTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            // Parsowanie godziny leku
            Date date = sdf.parse(doseTimeStr);
            if (date == null) return false;

            Calendar doseTime = Calendar.getInstance();
            Calendar tempDate = Calendar.getInstance();
            tempDate.setTime(date);

            // Ustawiamy godzinę i minutę z JSON-a na dzisiejszy dzień
            doseTime.set(Calendar.HOUR_OF_DAY, tempDate.get(Calendar.HOUR_OF_DAY));
            doseTime.set(Calendar.MINUTE, tempDate.get(Calendar.MINUTE));
            doseTime.set(Calendar.SECOND, 0);
            doseTime.set(Calendar.MILLISECOND, 0);

            // Pobieramy aktualny czas
            Calendar now = Calendar.getInstance();

            // Obliczamy różnicę w milisekundach
            long diffInMillis = now.getTimeInMillis() - doseTime.getTimeInMillis();
            long fifteenMinutesInMillis = 15 * 60 * 1000;

            // Jeśli różnica jest większa niż 15 minut (i jest na plusie), to czas minął
            return diffInMillis > fifteenMinutesInMillis;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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