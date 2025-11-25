package com.example.inteligentnypojemnik;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class MissedDoseWorker extends Worker {

    private static final String TAG = "MissedDoseWorker";

    public MissedDoseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "--- WORKER: SPRAWDZANIE STANU ---");
        Context context = getApplicationContext();
        ApiService apiService = RetrofitClient.getApiService(context);

        try {
            Response<MyDevicesResponse> devicesResponse = apiService.getMyDevices().execute();
            if (!devicesResponse.isSuccessful() || devicesResponse.body() == null) {
                Log.e(TAG, "Błąd API (urządzenia): " + devicesResponse.code());
                scheduleNextCheck();
                return Result.success();
            }

            List<MyDevice> devices = devicesResponse.body().getDevices();

            for (MyDevice device : devices) {
                checkDeviceForMissedDoses(apiService, device, context);
            }

            // Zaplanuj kolejne sprawdzenie za 1 minutę (PĘTLA DO TESTÓW)
            scheduleNextCheck();

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Wyjątek w workerze", e);
            scheduleNextCheck();
            return Result.failure();
        }
    }

    private void scheduleNextCheck() {
        OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(MissedDoseWorker.class)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                "TestMonitorLekow",
                ExistingWorkPolicy.REPLACE,
                nextRequest
        );
    }

    private void checkDeviceForMissedDoses(ApiService api, MyDevice device, Context context) throws IOException {
        Response<DeviceDetailsResponse> detailsResp = api.getDeviceDetails(device.getId()).execute();
        Response<EventHistoryResponse> historyResp = api.getDeviceHistory(device.getId()).execute();

        if (!detailsResp.isSuccessful() || !historyResp.isSuccessful() || detailsResp.body() == null) {
            return;
        }

        DeviceDetailsResponse.Configuration config = detailsResp.body().configuration;
        List<EventHistoryItem> history = historyResp.body() != null ? historyResp.body().getEvents() : null;

        String todayKey = getTodayKey();
        DeviceDetailsResponse.DayConfig dayConfig = config.get(todayKey);

        // Jeśli brak konfiguracji na dzisiaj lub dzień jest pusty - ignorujemy
        if (dayConfig == null || dayConfig.containers == null) {
            Log.d(TAG, "Brak konfiguracji na dzień: " + todayKey + " dla urządzenia " + device.getSeniorDisplayName());
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();

        // === KONFIGURACJA CZASU (TESTY) ===
        // Czas po którym uznajemy dawkę za pominiętą (np. 2 minuty po czasie leku)
        long thresholdMillis = 2 * 60 * 1000;

        // Maksymalny czas wstecz (żeby nie alarmować o lekach z rana)
        long maxLookBackMillis = 60 * 60 * 1000;

        for (DeviceDetailsResponse.ContainerConfig container : dayConfig.containers.values()) {

            // WAŻNE: Sprawdzamy, czy lek/przegroda jest w ogóle aktywna
            if (!container.active) {
                // Log.d(TAG, "Pominięto nieaktywną przegrodę: " + container.reminder_time);
                continue;
            }

            if (container.reminder_time != null) {

                Calendar doseCalendar = parseTimeToday(container.reminder_time);
                if (doseCalendar == null) continue;

                long doseTimeMillis = doseCalendar.getTimeInMillis();

                // 1. Czy jesteśmy w oknie "PO CZASIE"? (Aktualny czas > czas leku + 2 minuty)
                boolean isAfterThreshold = currentTimeMillis > (doseTimeMillis + thresholdMillis);
                // 2. Czy nie jest za późno na powiadomienie?
                boolean isNotTooLate = currentTimeMillis < (doseTimeMillis + maxLookBackMillis);

                if (isAfterThreshold && isNotTooLate) {

                    Log.d(TAG, "Sprawdzam lek: " + container.reminder_time + " dla " + device.getSeniorDisplayName());

                    boolean medicineTaken = false;

                    if (history != null) {
                        for (EventHistoryItem event : history) {
                            Date eventDate = parseTimestamp(event.getTimestamp());
                            if (eventDate != null) {
                                long eventTime = eventDate.getTime();

                                // Aktywność musi być PO lub RÓWNO z czasem zaplanowanym.
                                if (eventTime >= doseTimeMillis) {
                                    medicineTaken = true;
                                    Log.d(TAG, "-> ZNALEZIONO AKTYWNOŚĆ! (Lek wzięty o: " + event.getTimestamp() + ")");
                                    break;
                                }
                            }
                        }
                    }

                    if (!medicineTaken) {
                        Log.w(TAG, "!!! ALARM !!! Brak aktywności po godzinie " + container.reminder_time);
                        sendNotification(context, device.getSeniorDisplayName(), container.reminder_time);
                    } else {
                        Log.d(TAG, "OK: Lek wzięty.");
                    }
                }
            }
        }
    }

    // --- ULEPSZONE PARSOWANIE DATY (Obsługa wielu formatów) ---
    private Date parseTimestamp(String ts) {
        if (ts == null || ts.isEmpty()) return null;

        // 1. Próba parsowania Instant/OffsetDateTime (Dla nowszych Androidów)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try { return Date.from(Instant.parse(ts)); } catch (Exception ignored) {}
            try { return Date.from(OffsetDateTime.parse(ts).toInstant()); } catch (Exception ignored) {}
        }

        // 2. Lista formatów do sprawdzenia (Starsze Androidy + niestandardowe formaty)
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", // API z mikrosekundami
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",    // Standardowe milisekundy
                "yyyy-MM-dd'T'HH:mm:ss.SSS",       // <-- TO NAPRAWIA TWÓJ BŁĄD (bez 'Z')
                "yyyy-MM-dd'T'HH:mm:ss'Z'",        // Bez milisekund
                "yyyy-MM-dd'T'HH:mm:ss"            // Bez milisekund i bez 'Z'
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Zakładamy, że serwer zwraca UTC
                return sdf.parse(ts);
            } catch (ParseException ignored) {
                // Próbujemy kolejny format
            }
        }

        Log.e("TimeFormat", "Żaden format nie pasował do: " + ts);
        return null;
    }

    private String getTodayKey() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY: return "monday";
            case Calendar.TUESDAY: return "tuesday";
            case Calendar.WEDNESDAY: return "wednesday";
            case Calendar.THURSDAY: return "thursday";
            case Calendar.FRIDAY: return "friday";
            case Calendar.SATURDAY: return "saturday";
            case Calendar.SUNDAY: return "sunday";
        }
        return "monday";
    }

    private Calendar parseTimeToday(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            c.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c;
        } catch (Exception e) { return null; }
    }

    private void sendNotification(Context context, String patientName, String time) {
        String channelId = "caregiver_alerts";
        int notificationId = time.hashCode();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Alerty Opiekuna", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, CaregiverPanelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("ALARM: Brak aktywności!")
                .setContentText("Podopieczny " + patientName + " nie wziął leków z godziny " + time + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, builder.build());
            } else {
                Log.e(TAG, "Brak uprawnień POST_NOTIFICATIONS!");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Błąd security przy powiadomieniu", e);
        }
    }
}