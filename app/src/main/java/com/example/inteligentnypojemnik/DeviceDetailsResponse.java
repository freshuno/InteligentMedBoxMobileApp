package com.example.inteligentnypojemnik;

import java.util.List;
import java.util.Map;

public class DeviceDetailsResponse {
    public int id;
    public String label;
    public String senior_username;
    public String senior_display_name;
    public boolean is_active;
    public Configuration configuration;

    public static class Configuration {
        public DayConfig monday;
        public DayConfig tuesday;
        public DayConfig wednesday;
        public DayConfig thursday;
        public DayConfig friday;
        public DayConfig saturday;
        public DayConfig sunday;

        public DayConfig get(String dayKey) {
            switch (dayKey) {
                case "monday": return monday;
                case "tuesday": return tuesday;
                case "wednesday": return wednesday;
                case "thursday": return thursday;
                case "friday": return friday;
                case "saturday": return saturday;
                case "sunday": return sunday;
            }
            return null;
        }
    }

    public static class DayConfig {
        public boolean active;
        public Map<String, ContainerConfig> containers;
    }

    public static class ContainerConfig {
        public boolean active;
        public String reminder_time;
        public List<MedicineItem> medicine;
    }

    public static class MedicineItem {
        public String name;
        public int dose;
    }
}

