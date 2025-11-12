package com.example.inteligentnypojemnik;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://65.21.228.23:8000/";

    private static Retrofit mainRetrofit = null;
    private static Retrofit authRetrofit = null;
    private static ApiService apiService = null;
    private static ApiService authApiService = null;

    private static class TokenAuthenticator implements Authenticator {
        private final SessionManager session;
        private final ApiService authApi;

        TokenAuthenticator(SessionManager session, ApiService authApi) {
            this.session = session;
            this.authApi = authApi;
        }

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            if (response.request().header("Authorization-Retried") != null) return null;

            String refresh = session.getRefreshToken();
            if (refresh == null || refresh.isEmpty()) return null;

            retrofit2.Response<LoginResponse> r =
                    authApi.refreshToken(new RefreshRequest(refresh)).execute();

            if (!r.isSuccessful() || r.body() == null || r.body().getAccess() == null) {
                session.clearSession();
                return null;
            }

            String newAccess = r.body().getAccess();
            String newRefresh = r.body().getRefresh() != null ? r.body().getRefresh() : refresh;
            session.saveTokens(newAccess, newRefresh);

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccess)
                    .header("Authorization-Retried", "1")
                    .build();
        }
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            SessionManager session = new SessionManager(context.getApplicationContext());

            OkHttpClient authOkHttp = getUnsafeOkHttpClient(null); // bez AuthInterceptor
            authRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(authOkHttp)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            authApiService = authRetrofit.create(ApiService.class);

            OkHttpClient mainOkHttp = getUnsafeOkHttpClient(context).newBuilder()
                    .addInterceptor(new AuthInterceptor(context))
                    .authenticator(new TokenAuthenticator(session, authApiService))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            mainRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(mainOkHttp)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = mainRetrofit.create(ApiService.class);
        }
        return apiService;
    }

    private static OkHttpClient getUnsafeOkHttpClient(Context context) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true);

            if (context != null) {
                // dsada
            }
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
