package com.example.inteligentnypojemnik;

public class Compartment {
    private String name;
    private String time;
    private String medCount;

    public Compartment(String name, String time, String medCount) {
        this.name = name;
        this.time = time;
        this.medCount = medCount;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getMedCount() {
        return medCount;
    }
}