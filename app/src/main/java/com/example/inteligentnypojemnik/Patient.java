package com.example.inteligentnypojemnik;

import java.util.List;

public class Patient {
    private String initials;
    private String name;
    private String deviceCount;
    // [NOWE] Lista urządzeń należących do tego pacjenta
    private List<MyDevice> userDevices;

    public Patient(String initials, String name, String deviceCount, List<MyDevice> userDevices) {
        this.initials = initials;
        this.name = name;
        this.deviceCount = deviceCount;
        this.userDevices = userDevices;
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

    // [NOWE] Getter do listy urządzeń
    public List<MyDevice> getUserDevices() {
        return userDevices;
    }
}