package hotel.dto;

import java.util.Objects;

public class ReservationKey {
    private final int hotelId;
    private final int roomId;

    public ReservationKey(int hotelId, int roomId) {
        this.hotelId = hotelId;
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationKey that = (ReservationKey) o;
        return hotelId == that.hotelId &&
                roomId == that.roomId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, roomId);
    }
}
