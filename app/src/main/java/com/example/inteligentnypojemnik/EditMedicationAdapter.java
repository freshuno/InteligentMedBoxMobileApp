package com.example.inteligentnypojemnik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EditMedicationAdapter extends RecyclerView.Adapter<EditMedicationAdapter.EditViewHolder> {

    private List<Medication> medicationList;
    private Context context;

    public EditMedicationAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public EditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_medication, parent, false);
        return new EditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditViewHolder holder, int position) {
        Medication med = medicationList.get(position);
        holder.name.setText(med.getName());
        holder.dosage.setText(med.getDosage());

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // W przyszłości usunie lek
                medicationList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class EditViewHolder extends RecyclerView.ViewHolder {
        EditText name;
        EditText dosage;
        Button deleteButton;

        public EditViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.edit_med_name);
            dosage = itemView.findViewById(R.id.edit_med_dosage);
            deleteButton = itemView.findViewById(R.id.button_delete_med);
        }
    }
}