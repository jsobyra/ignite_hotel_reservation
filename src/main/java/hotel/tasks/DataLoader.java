package hotel.tasks;

import hotel.Start;
import hotel.dto.Hotel;
import hotel.dto.Room;
import org.apache.ignite.IgniteDataStreamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//300 cities, 1000 hotels in each city, 100 rooms in each hotel
public class DataLoader {
    private static final Random random = new Random();

    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            ignite.cluster().active(true);
            try (IgniteDataStreamer<String, List<Hotel>> hotelCache = ignite.dataStreamer("hotelCache");
                 IgniteDataStreamer<Integer, List<Room>> roomCache = ignite.dataStreamer("roomCache")) {
                ignite.cluster().disableWal("hotelCache");
                ignite.cluster().disableWal("roomCache");
                for (int i = 1; i <= 30; i++) {
                    List<Hotel> hotels = createHotelsForCity(i, 100);
                    hotelCache.addData("City" + i, hotels);

                    hotels.forEach(hotel -> {
                        List<Room> rooms = createRoomsForHotel(hotel.getId(), 10);
                        roomCache.addData(hotel.getId(), rooms);
                    });
                }

                ignite.cluster().enableWal("hotelCache");
                ignite.cluster().enableWal("roomCache");
                ignite.cluster().enableWal("reservationCache");
            }
        }));
    }

    private static List<Hotel> createHotelsForCity(int cityId, int numberOfHotels) {
        List<Hotel> hotels = new ArrayList<>();
        for(int i = 1; i <= numberOfHotels; i++) {
            int hotelId = i + (cityId-1)*numberOfHotels;
            hotels.add(new Hotel(hotelId, "Hotel" + hotelId, random.nextInt(5) + 1));
        }
        return hotels;
    }

    private static List<Room> createRoomsForHotel(int hotelId, int numberOfRooms) {
        List<Room> rooms = new ArrayList<>();
        for(int i = 1; i <= numberOfRooms; i++) {
            int roomId = i + (hotelId-1)*numberOfRooms;
            rooms.add(new Room(roomId, random.nextInt(20) + 1, hotelId));
        }
        return rooms;
    }
}
