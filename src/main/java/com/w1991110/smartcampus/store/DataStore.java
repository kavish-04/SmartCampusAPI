package com.w1991110.smartcampus.store;

import com.w1991110.smartcampus.model.Room;
import com.w1991110.smartcampus.model.Sensor;
import com.w1991110.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readingsBySensor = new ConcurrentHashMap<>();

    public static List<SensorReading> getReadingsForSensor(String sensorId) {
        return readingsBySensor.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    private DataStore() {
    }
}