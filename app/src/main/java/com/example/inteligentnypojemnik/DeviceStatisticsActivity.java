package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceStatisticsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView activityCount, currentDateText;
    private ImageButton prevDayButton, nextDayButton;

    private DeviceActivityAdapter adapter;
    private List<DeviceActivity> displayedActivities;
    private List<EventHistoryItem> allApiEvents;

    private int deviceId;
    private Calendar currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_statistics);

        recyclerView = findViewById(R.id.activity_recycler_view);
        activityCount = findViewById(R.id.text_activity_count);
        currentDateText = findViewById(R.id.text_current_date);
        prevDayButton = findViewById(R.id.button_prev_day);
        nextDayButton = findViewById(R.id.button_next_day);
        ImageButton backButton = findViewById(R.id.buttonBack);

        deviceId = getIntent().getIntExtra("DEVICE_ID", -1);
        currentDate = Calendar.getInstance();
        allApiEvents = new ArrayList<>();

        backButton.setOnClickListener(v -> finish());

        prevDayButton.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_YEAR, -1);
            updateDateUI();
            filterAndDisplayList();
        });

        nextDayButton.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_YEAR, 1);
            updateDateUI();
            filterAndDisplayList();
        });

        setupRecyclerView();
        updateDateUI();

        if (deviceId != -1) {
            fetchDeviceHistory(deviceId);
        } else {
            Toast.makeText(this, "Błąd: Brak ID urządzenia", Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupRecyclerView() {
        displayedActivities = new ArrayList<>();
        adapter = new DeviceActivityAdapter(this, displayedActivities);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateDateUI() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", new Locale("pl", "PL"));
        currentDateText.setText(dateFormat.format(currentDate.getTime()));
    }

    private void fetchDeviceHistory(int id) {
        RetrofitClient.getApiService(this).getDeviceHistory(id).enqueue(new Callback<EventHistoryResponse>() {
            @Override
            public void onResponse(Call<EventHistoryResponse> call, Response<EventHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allApiEvents.clear();
                    allApiEvents.addAll(response.body().getEvents());

                    // Sortowanie po rzeczywistym czasie (od najwcześniejszych)
                    Collections.sort(allApiEvents, (a, b) -> {
                        Date da = parseTimestamp(a.getTimestamp());
                        Date db = parseTimestamp(b.getTimestamp());
                        if (da == null && db == null) return 0;
                        if (da == null) return -1;
                        if (db == null) return 1;
                        return da.compareTo(db);
                    });

                    filterAndDisplayList();

                } else {
                    Toast.makeText(DeviceStatisticsActivity.this, "Błąd pobierania historii", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventHistoryResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd historii: " + t.getMessage());
                Toast.makeText(DeviceStatisticsActivity.this, "Błąd połączenia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayList() {
        displayedActivities.clear();
        SimpleDateFormat filterFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDate = filterFormat.format(currentDate.getTime());

        for (EventHistoryItem item : allApiEvents) {
            String eventDate = formatTimestampToDateString(item.getTimestamp());

            if (eventDate != null && eventDate.equals(selectedDate)) {
                String time = formatTimestampToTime(item.getTimestamp());
                String desc = getFriendlyEventName(item.getType()); // jeśli masz getTypeValue(), podmień tutaj

                boolean isError = desc.toLowerCase().contains("niepoprawna");
                displayedActivities.add(new DeviceActivity(time, desc, isError));
            }
        }

        adapter.notifyDataSetChanged();
        activityCount.setText(displayedActivities.size() + " aktywności");
    }

    private String getFriendlyEventName(String eventType) {
        // Traktuj brak, "-", "T" i "STRING" jako zwykłą aktywność pudełka
        if (eventType == null ||
                eventType.equalsIgnoreCase("T") ||
                eventType.equals("-") ||
                eventType.equalsIgnoreCase("STRING")) {
            return "Aktywność pudełka";
        }

        switch (eventType) {
            case "CONFIG_UPDATED":
                return "Konfiguracja zaktualizowana";
            case "BOX_SENSOR_TRIGGERED":
                return "Aktywność pudełka";
            case "DEVICE_PAIRED":
                return "Urządzenie sparowane";
            default:
                return eventType;
        }
    }

    /**
     * Parser ISO8601 z obsługą Z/offsetu i frakcji sekund.
     * Preferuje java.time (Instant/OffsetDateTime), a na końcu ma bezpieczny fallback.
     */
    private Date parseTimestamp(String ts) {
        if (ts == null || ts.isEmpty()) return null;

        // java.time – poprawnie uwzględnia 'Z' i offset
        try {
            Instant instant = Instant.parse(ts); // np. 2025-11-09T21:49:00Z lub z ułamkami
            return Date.from(instant);
        } catch (Exception ignored) { }

        try {
            OffsetDateTime odt = OffsetDateTime.parse(ts); // np. 2025-11-09T21:49:00+00:00
            return Date.from(odt.toInstant());
        } catch (Exception ignored) { }

        // Fallbacki na stare wzorce, gdyby przyszło coś niestandardowego
        try {
            SimpleDateFormat formatWithMillis =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            formatWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatWithMillis.parse(ts);
        } catch (ParseException ignored) { }

        try {
            SimpleDateFormat formatWithoutMillis =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            formatWithoutMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatWithoutMillis.parse(ts);
        } catch (ParseException e2) {
            Log.e("TimeFormat", "Błąd parsowania daty: " + ts, e2);
            return null;
        }
    }

    private String formatTimestampToTime(String inputTimestamp) {
        Date date = parseTimestamp(inputTimestamp);
        if (date != null) {
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault());
            return outputFormat.format(date);
        }
        return "B/D";
    }

    private String formatTimestampToDateString(String inputTimestamp) {
        Date date = parseTimestamp(inputTimestamp);
        if (date != null) {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault());
            return outputFormat.format(date);
        }
        return null;
    }
}
