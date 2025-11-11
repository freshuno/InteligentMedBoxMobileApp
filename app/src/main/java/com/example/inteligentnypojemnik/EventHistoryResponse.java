package com.example.inteligentnypojemnik;

import java.util.List;

public class EventHistoryResponse {
    private List<EventHistoryItem> events;
    private int count;

    public List<EventHistoryItem> getEvents() { return events; }
    public int getCount() { return count; }
}