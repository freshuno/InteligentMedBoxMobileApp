package com.example.inteligentnypojemnik;

public class Device {
    private String name;
    private String patientName;
    private String nextDose;
    private String medCount;
    private String status;

    public Device(String name, String patientName, String nextDose, String medCount, String status) {
        this.name = name;
        this.patientName = patientName;
        this.nextDose = nextDose;
        this.medCount = medCount;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getNextDose() {
        return nextDose;
    }

    public String getMedCount() {
        return medCount;
    }

    public String getStatus() {
        return status;
    }
}