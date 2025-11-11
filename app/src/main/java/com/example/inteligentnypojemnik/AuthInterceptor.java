package com.example.inteligentnypojemnik;

import android.content.Context;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        // najlepiej używać applicationContext
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        // NIE doklejaj Bearera do /api/auth/token/*
        if (path.startsWith("/api/auth/token/")) {
            return chain.proceed(originalRequest);
        }

        String token = sessionManager.getAuthToken();
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(originalRequest);
    }
}
