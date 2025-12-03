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

import retrofit2.Response;

public class MissedDoseWorker extends Worker {

    private static final String TAG = "MissedDoseWorker";

    public MissedDoseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "--- WORKER: SPRAWDZANIE STANU (TRYB PRODUKCYJNY 15min) ---");
        Context context = getApplicationContext();
        ApiService apiService = RetrofitClient.getApiService(context);

        try {
            Response<MyDevicesResponse> devicesResponse = apiService.getMyDevices().execute();
            if (!devicesResponse.isSuccessful() || devicesResponse.body() == null) {
                Log.e(TAG, "Błąd API (urządzenia): " + devicesResponse.code());
                return Result.retry(); // Spróbuj ponownie później w razie błędu sieci
            }

            List<MyDevice> devices = devicesResponse.body().getDevices();

            for (MyDevice device : devices) {
                checkDeviceForMissedDoses(apiService, device, context);
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Wyjątek w workerze", e);
            return Result.failure();
        }
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

        if (dayConfig == null || dayConfig.containers == null) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long thresholdMillis = 15 * 60 * 1000;
        long maxLookBackMillis = 30 * 60 * 1000;

        for (DeviceDetailsResponse.ContainerConfig container : dayConfig.containers.values()) {

            if (!container.active) {
                continue;
            }

            if (container.reminder_time != null) {

                Calendar doseCalendar = parseTimeToday(container.reminder_time);
                if (doseCalendar == null) continue;

                long doseTimeMillis = doseCalendar.getTimeInMillis();

                // Logika okna czasowego:
                // "Czy minęło już 15 minut?"  ORAZ  "Czy nie minęło jeszcze 30 minut?"
                boolean isAfterThreshold = currentTimeMillis > (doseTimeMillis + thresholdMillis);
                boolean isWithinWindow = currentTimeMillis < (doseTimeMillis + maxLookBackMillis);

                if (isAfterThreshold && isWithinWindow) {

                    Log.d(TAG, "Sprawdzam lek w oknie alarmowym: " + container.reminder_time);

                    boolean medicineTaken = false;

                    if (history != null) {
                        for (EventHistoryItem event : history) {
                            Date eventDate = parseTimestamp(event.getTimestamp());
                            if (eventDate != null) {
                                long eventTime = eventDate.getTime();
                                if (eventTime >= doseTimeMillis) {
                                    medicineTaken = true;
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

    private Date parseTimestamp(String ts) {
        if (ts == null || ts.isEmpty()) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try { return Date.from(Instant.parse(ts)); } catch (Exception ignored) {}
            try { return Date.from(OffsetDateTime.parse(ts).toInstant()); } catch (Exception ignored) {}
        }

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(ts);
            } catch (ParseException ignored) {}
        }
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
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Błąd security przy powiadomieniu", e);
        }
    }
}