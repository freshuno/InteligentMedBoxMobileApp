package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CompartmentScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_schedule);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.header_title);
        RecyclerView recyclerView = findViewById(R.id.compartments_recycler_view);

        // Pobierz dane wysłane z poprzedniego ekranu
        String deviceName = getIntent().getStringExtra("DEVICE_NAME");
        String dayName = getIntent().getStringExtra("DAY_NAME");

        // Ustaw tytuł nagłówka
        if (deviceName != null && dayName != null) {
            headerTitle.setText(deviceName + " - " + dayName);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Stwórz listę przegród "na sztywno"
        List<Compartment> compartments = new ArrayList<>();
        compartments.add(new Compartment("Przegroda 1", "9:00", "4"));
        compartments.add(new Compartment("Przegroda 2", "14:00", "3"));
        compartments.add(new Compartment("Przegroda 3", "19:00", "5"));

        CompartmentAdapter adapter = new CompartmentAdapter(this, compartments);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}