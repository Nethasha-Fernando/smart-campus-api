package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
  Singleton in-memory data store for the Smart Campus API.
  ConcurrentHashMap is used instead of HashMap to make the program thread-safe, so multiple requests
  can access and update data at the same time without causing errors.
 */

public class DataStore {

    //only one datastore exists in the whole app
    private static final DataStore INSTANCE = new DataStore();

    // Thread-safe maps acting as our in-memory "database"
    private final Map<String, Room> rooms = new ConcurrentHashMap<>(); //stores rooms, Your API can handle multiple users at the same time or else too many users crashes
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>(); //stores sensors
    // sensorId -> list of readings
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    //anywhere in ur app if u call this u get the same data everytime, without this prev data will dissapear.
    public static DataStore getInstance() {
        return INSTANCE;
    }

    /** Pre-populate with sample data so the API is immediately demonstrable. */
    private void seedData() {
        // Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-102", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-001", "Main Assembly Hall", 500);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LAB-102");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "ACTIVE", 19.8, "HALL-001");

        for (Sensor s : new Sensor[]{s1, s2, s3, s4}) {
            sensors.put(s.getId(), s);
            rooms.get(s.getRoomId()).getSensorIds().add(s.getId());
            readings.put(s.getId(), new ArrayList<>());
        }

        // Sample readings
        readings.get("TEMP-001").add(new SensorReading(21.0));
        readings.get("TEMP-001").add(new SensorReading(22.5));
        readings.get("CO2-001").add(new SensorReading(400.0));
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getReadings() { return readings; }
}

//This DataStore class is like a mini fake database inside your program.
//Instead of using MySQL or MongoDB, you’re storing everything in memory using:
//Maps (like tables)
//Lists (like collections of data)

