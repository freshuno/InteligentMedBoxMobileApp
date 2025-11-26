package com.example.inteligentnypojemnik;

import android.app.AlarmManager;
import android.app.AlertDialog; // Dodano import do dialogu
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface; // Dodano import
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri; // Dodano import do Uri
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings; // Dodano import do Settings
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        checkAndRequestExactAlarmPermission();

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
                new AlertDialog.Builder(ElderlyPanelActivity.this)
                        .setTitle("Wylogowanie")
                        .setMessage("Czy na pewno chcesz się wylogować?")
                        .setPositiveButton("Tak", (dialog, which) -> {
                            sessionManager.clearSession();
                            Intent intent = new Intent(ElderlyPanelActivity.this, SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Nie", null)
                        .show();
            }
        });
        // ----------------------------------------------

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchMyDoses();
    }

    private void checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Wymagane uprawnienie")
                        .setMessage("Aby aplikacja mogła przypominać o lekach dokładnie o czasie, musisz zezwolić na ustawianie alarmów w kolejnym oknie.")
                        .setPositiveButton("Ustawienia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Później", null)
                        .show();
            }
        }
    }
    // -------------------------------------------------------

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

                    scheduleReminders(doses);

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

    private void scheduleReminders(List<TodayDose> doses) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w("ALARM", "Brak uprawnień do dokładnych alarmów - pomijam ustawianie.");
                return;
            }
        }

        for (TodayDose dose : doses) {
            try {
                Date doseDate = timeFormat.parse(dose.getTime());
                Calendar doseCalendar = Calendar.getInstance();
                doseCalendar.setTime(doseDate);

                Calendar reminderTime = Calendar.getInstance();
                reminderTime.set(Calendar.HOUR_OF_DAY, doseCalendar.get(Calendar.HOUR_OF_DAY));
                reminderTime.set(Calendar.MINUTE, doseCalendar.get(Calendar.MINUTE));
                reminderTime.set(Calendar.SECOND, 0);

                reminderTime.add(Calendar.MINUTE, -15);

                if (reminderTime.after(Calendar.getInstance())) {
                    StringBuilder medNames = new StringBuilder();
                    for (MedicineDose med : dose.getMedicine()) {
                        if(medNames.length() > 0) medNames.append(", ");
                        medNames.append(med.getName());
                    }

                    Intent intent = new Intent(this, NotificationReceiver.class);
                    intent.putExtra("MED_INFO", medNames.toString());

                    int uniqueId = dose.getTime().hashCode();

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this,
                            uniqueId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    if (alarmManager != null) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminderTime.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d("ALARM", "Ustawiono przypomnienie na: " + reminderTime.getTime());
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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

                String doseString = formatDoseToString(med.getDose());
                if (!doseString.isEmpty()) {
                    medText.append(" (").append(doseString).append(")");
                }
            }

            nextMedName.setText(medText.toString());

            String timeString = "Godzina: " + next.getTime();
            if (isTimeNow(next.getTime())) {
                timeString += " - TERAZ";
            }
            nextMedTime.setText(timeString);
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