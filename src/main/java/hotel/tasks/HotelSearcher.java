package hotel.tasks;

import hotel.Start;
import hotel.dto.*;
import hotel.tasks.ReservationMaker;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskAdapter;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HotelSearcher {
    private static final String HOTEL_CACHE = "hotelCache";
    private static final String ROOM_CACHE = "roomCache";
    private static final String RESERVATION_CACHE = "reservationCache";

    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            try (IgniteCache<ReservationKey, List<Reservation>> reservationCache = ignite.cache("reservationCache")) {
                Criteria criteria = new Criteria(ignite, "City1", 1, 5, LocalDate.now(), LocalDate.now().plusDays(2));
                List<Room> matchingRooms = ignite.compute().execute(FreeRoomSearcher.class, criteria);
                if(!matchingRooms.isEmpty()) {
                    Reservation reservation = new Reservation(matchingRooms.get(0).getHotelId(), matchingRooms.get(0).getId(), LocalDate.now(), LocalDate.now().plusDays(2));
                    ReservationMaker.makeReservation(criteria, reservation);
                } else System.out.println("There is no available rooms now");
            }
        }));
    }

    private static class FreeRoomSearcher extends ComputeTaskAdapter<Criteria, List<Room>> {

        @Override
        public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> nodes, Criteria criteria) {
            IgniteCache<String, List<Hotel>> hotelCache = criteria.getIgnite().getOrCreateCache(HOTEL_CACHE);
            IgniteCache<Integer, List<Room>> roomCache = criteria.getIgnite().getOrCreateCache(ROOM_CACHE);
            IgniteCache<ReservationKey, List<Reservation>> reservationCache = criteria.getIgnite().getOrCreateCache(RESERVATION_CACHE);

            List<Hotel> hotels = hotelCache.get(criteria.getCityId()).stream()
                    .filter(hotel -> hotel.getStars() == criteria.getStars())
                    .collect(Collectors.toList());

            List<Integer> keys = IntStream.rangeClosed(0, hotels.size()-1).boxed().collect(Collectors.toList());
            Map<ClusterNode, Collection<Integer>> keyToNodeMappings = criteria.getIgnite().<Integer>affinity(HOTEL_CACHE).mapKeysToNodes(keys);

            Map<ComputeJob, ClusterNode> map = new HashMap<>();

            keyToNodeMappings.entrySet().forEach(mapping -> {
                ClusterNode clusterNode = mapping.getKey();

                mapping.getValue().forEach(key -> {
                    map.put(new ComputeJobAdapter() {
                        @Override
                        public Object execute() {
                            List<Room> rooms = roomCache.get(hotels.get(key).getId());
                            List<Room> matchingRooms = new ArrayList<>();
                            for(Room room : rooms) {
                                if(room.getPersonNumber() == criteria.getPersonsNumber()) {
                                    List<Reservation> reservations = reservationCache.get(new ReservationKey(hotels.get(key).getId(), room.getId()));
                                    if(isAvailable(reservations, criteria))
                                        matchingRooms.add(room);
                                }
                            }
                            return matchingRooms;
                        }
                    }, clusterNode);
                });
            });
            return map;
        }

        @Override
        public List<Room> reduce(List<ComputeJobResult> results) {
            List<Room> rooms = new ArrayList<>();
            for (ComputeJobResult result: results) {
                rooms.addAll(result.getData());
            }
            return rooms;
        }

        private boolean isAvailable(List<Reservation> reservations, Criteria criteria) {
            if(reservations == null)
                return true;
            return !reservations.stream()
                    .filter(reservation -> criteria.getStartDate().equals(reservation.getStartDate()))
                    .filter(reservation -> criteria.getStartDate().isBefore(reservation.getStartDate()) && criteria.getEndDate().isAfter(reservation.getEndDate()))
                    .filter(reservation -> criteria.getStartDate().isAfter(reservation.getStartDate()) && criteria.getStartDate().isBefore(reservation.getEndDate()))
                    .findFirst().isPresent();
        }
    }
}
