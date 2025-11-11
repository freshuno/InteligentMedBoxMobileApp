package com.example.inteligentnypojemnik;

public class PairDeviceRequest {
    String physical_device_id;
    String senior_username;
    String label;

    public PairDeviceRequest(String physical_device_id, String senior_username, String label) {
        this.physical_device_id = physical_device_id;
        this.senior_username = senior_username;
        this.label = label;
    }
}