package com.example.inteligentnypojemnik;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
    @POST("api/auth/token/pair")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

}