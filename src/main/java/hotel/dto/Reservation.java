package hotel.dto;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.time.LocalDate;

public class Reservation {
    @AffinityKeyMapped
    private final int hotelId;
    @AffinityKeyMapped
    private final int roomId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Reservation(int hotelId, int roomId, LocalDate startDate, LocalDate endDate) {
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ReservationKey getReservationKey() {
        return new ReservationKey(hotelId, roomId);
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "hotelId=" + hotelId +
                ", roomId=" + roomId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
