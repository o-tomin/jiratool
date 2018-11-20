package com.jiratool.data;

import java.util.HashMap;
import java.util.Map;

public class SmartItem {
    private final int id;
    private final Map<Integer, Object> data = new HashMap<>();

    public SmartItem(final int id) {
        this.id = id;
    }

    public synchronized void add(int key, Object value) {
        data.put(key, value);
    }

    public synchronized Object get(int key) {
        return data.get(key);
    }

    public int getId() {
        return id;
    }

    public boolean contains(int timeSpendPerDayKey) {
        return data.get(timeSpendPerDayKey) != null;
    }


}
