package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ElderlyPanelActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ImageButton logoutButton;

    private RecyclerView dosesRecyclerView;
    private DoseAdapter doseAdapter;
    private List<TodayDose> doseList;

    private TextView nextMedName, nextMedTime, userName, currentDateText;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_elderly_panel);

        sessionManager = new SessionManager(getApplicationContext());

        logoutButton = findViewById(R.id.button_logout);
        TextView settingsButton = findViewById(R.id.nav_settings);
        dosesRecyclerView = findViewById(R.id.doses_recycler_view);
        nextMedName = findViewById(R.id.textViewNextMedName);
        nextMedTime = findViewById(R.id.textViewNextMedTime);
        userName = findViewById(R.id.textViewUserName);
        currentDateText = findViewById(R.id.textViewCurrentDate);

        userName.setText(sessionManager.getUsername());

        updateDate();

        setupRecyclerView();

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ElderlyPanelActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.clearSession();
                Intent intent = new Intent(ElderlyPanelActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchMyDoses();
    }

    private void setupRecyclerView() {
        doseList = new ArrayList<>();
        doseAdapter = new DoseAdapter(this, doseList);
        dosesRecyclerView.setAdapter(doseAdapter);
        dosesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("pl", "PL"));
        String formattedDate = dateFormat.format(c.getTime());
        currentDateText.setText(formattedDate);
    }

    private void fetchMyDoses() {
        RetrofitClient.getApiService(this).getMyDoses().enqueue(new Callback<MyDosesResponse>() {
            @Override
            public void onResponse(Call<MyDosesResponse> call, Response<MyDosesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<TodayDose> doses = response.body().getTodayDoses();

                    Collections.sort(doses, (o1, o2) -> {
                        try {
                            return timeFormat.parse(o1.getTime()).compareTo(timeFormat.parse(o2.getTime()));
                        } catch (ParseException e) {
                            return 0;
                        }
                    });

                    doseList.clear();
                    doseList.addAll(doses);
                    doseAdapter.notifyDataSetChanged();

                    TodayDose nextDose = findNextDose(doses);
                    updateNextDoseCard(nextDose);

                } else {
                    Toast.makeText(ElderlyPanelActivity.this, "Nie udało się pobrać dawek", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyDosesResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd pobierania dawek: " + t.getMessage());
                Toast.makeText(ElderlyPanelActivity.this, "Błąd połączenia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TodayDose findNextDose(List<TodayDose> sortedDoses) {
        if (sortedDoses.isEmpty()) {
            return null;
        }

        Calendar currentTime = Calendar.getInstance();

        for (TodayDose dose : sortedDoses) {
            try {
                Date doseTimeDate = timeFormat.parse(dose.getTime());
                Calendar doseTime = Calendar.getInstance();
                doseTime.setTime(doseTimeDate);

                Calendar doseTimeToday = Calendar.getInstance();
                doseTimeToday.set(Calendar.HOUR_OF_DAY, doseTime.get(Calendar.HOUR_OF_DAY));
                doseTimeToday.set(Calendar.MINUTE, doseTime.get(Calendar.MINUTE));
                doseTimeToday.set(Calendar.SECOND, 0);

                if (doseTimeToday.after(currentTime)) {
                    return dose;
                }
            } catch (ParseException e) {
                Log.e("TimeParse", "Błąd parsowania czasu: " + dose.getTime());
            }
        }

        return null;
    }

    private void updateNextDoseCard(TodayDose next) {
        if (next == null) {
            nextMedName.setText("Brak kolejnych dawek na dzisiaj");
            nextMedTime.setText("");
        } else {
            StringBuilder medText = new StringBuilder();
            for (MedicineDose med : next.getMedicine()) {
                if (medText.length() > 0) medText.append(", ");
                medText.append(med.getName());
            }

            nextMedName.setText(medText.toString());

            String timeString = "Godzina: " + next.getTime();
            if (isTimeNow(next.getTime())) {
                timeString += " - TERAZ";
            }
            nextMedTime.setText(timeString);
        }
    }

    private boolean isTimeNow(String doseTimeStr) {
        try {
            Date doseTimeDate = timeFormat.parse(doseTimeStr);
            Calendar doseTime = Calendar.getInstance();
            doseTime.setTime(doseTimeDate);

            Calendar doseTimeToday = Calendar.getInstance();
            doseTimeToday.set(Calendar.HOUR_OF_DAY, doseTime.get(Calendar.HOUR_OF_DAY));
            doseTimeToday.set(Calendar.MINUTE, doseTime.get(Calendar.MINUTE));

            Calendar currentTime = Calendar.getInstance();

            long diffInMillis = doseTimeToday.getTimeInMillis() - currentTime.getTimeInMillis();

            long fifteenMinutes = 15 * 60 * 1000;

            if (diffInMillis > 0 && diffInMillis < fifteenMinutes) {
                return true;
            }
            return false;
        } catch (ParseException e) {
            return false;
        }
    }
}