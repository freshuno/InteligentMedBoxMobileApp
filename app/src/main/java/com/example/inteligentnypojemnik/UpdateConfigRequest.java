package com.example.inteligentnypojemnik;

public class UpdateConfigRequest {
    DeviceDetailsResponse.Configuration configuration;

    public UpdateConfigRequest(DeviceDetailsResponse.Configuration configuration) {
        this.configuration = configuration;
    }
}