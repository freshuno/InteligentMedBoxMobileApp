package com.example.inteligentnypojemnik;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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
        Medication med = medicationList.get(holder.getAdapterPosition());

        if (holder.nameWatcher != null) {
            holder.name.removeTextChangedListener(holder.nameWatcher);
        }
        if (holder.dosageWatcher != null) {
            holder.dosage.removeTextChangedListener(holder.dosageWatcher);
        }

        holder.name.setText(med.getName());
        holder.dosage.setText(med.getDosage());

        holder.nameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                medicationList.get(holder.getAdapterPosition()).setName(s.toString());
            }
        };

        holder.dosageWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                medicationList.get(holder.getAdapterPosition()).setDosage(s.toString());
            }
        };

        holder.name.addTextChangedListener(holder.nameWatcher);
        holder.dosage.addTextChangedListener(holder.dosageWatcher);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    medicationList.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, medicationList.size());
                }
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
        TextWatcher nameWatcher;
        TextWatcher dosageWatcher;

        public EditViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.edit_med_name);
            dosage = itemView.findViewById(R.id.edit_med_dosage);
            deleteButton = itemView.findViewById(R.id.button_delete_med);
        }
    }
}