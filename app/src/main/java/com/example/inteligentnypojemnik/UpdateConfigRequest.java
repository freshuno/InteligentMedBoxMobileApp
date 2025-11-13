package com.example.inteligentnypojemnik;

// Ta klasa jest potrzebna, aby wysłać JSON w formacie: {"configuration": {...}}
public class UpdateConfigRequest {
    DeviceDetailsResponse.Configuration configuration;

    public UpdateConfigRequest(DeviceDetailsResponse.Configuration configuration) {
        this.configuration = configuration;
    }
}