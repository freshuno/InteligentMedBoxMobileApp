package com.example.inteligentnypojemnik;

public class Patient {
    private String initials;
    private String name;
    private String deviceCount;

    public Patient(String initials, String name, String deviceCount) {
        this.initials = initials;
        this.name = name;
        this.deviceCount = deviceCount;
    }

    public String getInitials() {
        return initials;
    }

    public String getName() {
        return name;
    }

    public String getDeviceCount() {
        return deviceCount;
    }
}