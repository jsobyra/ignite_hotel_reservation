package hotel.dto;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

public class Room {
    private final int id;
    private final int personNumber;
    @AffinityKeyMapped
    private final int hotelId;

    public Room(int id, int personNumber, int hotelId) {
        this.id = id;
        this.personNumber = personNumber;
        this.hotelId = hotelId;
    }

    public int getPersonNumber() {
        return personNumber;
    }

    public int getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    }
}
