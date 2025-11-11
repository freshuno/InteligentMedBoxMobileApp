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

public class CompartmentAdapter extends RecyclerView.Adapter<CompartmentAdapter.CompartmentViewHolder> {

    private List<Compartment> compartmentList;
    private Context context;

    public CompartmentAdapter(Context context, List<Compartment> compartmentList) {
        this.context = context;
        this.compartmentList = compartmentList;
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Otwórz nowy ekran szczegółów
                Intent intent = new Intent(context, CompartmentDetailsActivity.class);

                // Przekaż wszystkie dane, których potrzebuje nowy ekran
                intent.putExtra("COMPARTMENT_NAME", compartment.getName());
                intent.putExtra("TIME", compartment.getTime());
                intent.putExtra("MED_COUNT", compartment.getMedCount());

                context.startActivity(intent);
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

        public CompartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.compartment_name);
            time = itemView.findViewById(R.id.compartment_time);
            medCount = itemView.findViewById(R.id.compartment_med_count);
        }
    }
}