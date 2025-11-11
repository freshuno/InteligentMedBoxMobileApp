package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CompartmentSettingsActivity extends AppCompatActivity {

    private List<Medication> medicationList;
    private EditMedicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_settings);

        ImageButton backButton = findViewById(R.id.buttonBack);
        MaterialButton saveButton = findViewById(R.id.button_save);
        RecyclerView recyclerView = findViewById(R.id.edit_med_recycler_view);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // W przyszłości zapisze zmiany do API
                Toast.makeText(CompartmentSettingsActivity.this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Przygotuj listę leków "na sztywno"
        medicationList = new ArrayList<>();
        medicationList.add(new Medication("Lek1 500 mg", "1 kapsułka"));

        // W przyszłości dodamy logikę dla "Dodaj lek"
        // medicationList.add(new Medication("Lek2 250 mg", "2 kapsułki"));

        adapter = new EditMedicationAdapter(this, medicationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}