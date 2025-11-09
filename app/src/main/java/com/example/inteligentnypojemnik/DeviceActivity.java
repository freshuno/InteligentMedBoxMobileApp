package com.example.inteligentnypojemnik;

public class DeviceActivity {
    private String time;
    private String description;
    private boolean isError;

    public DeviceActivity(String time, String description, boolean isError) {
        this.time = time;
        this.description = description;
        this.isError = isError;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public boolean isError() {
        return isError;
    }
}