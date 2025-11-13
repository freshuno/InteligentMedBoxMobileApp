package com.example.inteligentnypojemnik;

public class Compartment {
    private String name;
    private String time;
    private String medCount;

    private String extraJson;
    private String compartmentKey;

    public Compartment(String name, String time, String medCount) {
        this.name = name;
        this.time = time;
        this.medCount = medCount;
    }

    public String getName() { return name; }
    public String getTime() { return time; }
    public String getMedCount() { return medCount; }

    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }

    public String getCompartmentKey() { return compartmentKey; }
    public void setCompartmentKey(String compartmentKey) { this.compartmentKey = compartmentKey; }
}
