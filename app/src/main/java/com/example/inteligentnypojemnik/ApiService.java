package com.example.inteligentnypojemnik;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path; // Import

public interface ApiService {

    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/auth/token/pair")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @GET("api/devices/my-devices")
    Call<MyDevicesResponse> getMyDevices();

    @POST("api/devices/pair")
    Call<MyDevice> pairDevice(@Body PairDeviceRequest pairRequest);

    @GET("api/devices/{paired_device_id}/history")
    Call<EventHistoryResponse> getDeviceHistory(@Path("paired_device_id") int deviceId);
}