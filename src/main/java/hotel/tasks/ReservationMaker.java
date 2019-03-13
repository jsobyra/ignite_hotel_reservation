package hotel.tasks;

import hotel.dto.Criteria;
import hotel.dto.Reservation;
import hotel.dto.ReservationKey;
import hotel.dto.Room;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;

import javax.cache.CacheException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

public class ReservationMaker {

    public static void makeReservation(Criteria criteria, Reservation reservation) throws IgniteException{
        try (Transaction tx = Ignition.ignite().transactions().txStart(PESSIMISTIC, REPEATABLE_READ, 300, 0)) {
            IgniteCache<ReservationKey, List<Reservation>> cache = criteria.getIgnite().cache("reservationCache");
            List<Reservation> reservations;
            if(!cache.containsKey(reservation.getReservationKey()))
                reservations = new ArrayList<>();
            else reservations = cache.get(reservation.getReservationKey());

            reservations.add(reservation);
            cache.put(reservation.getReservationKey(), reservations);
            tx.commit();
        }
        System.out.println("Reservation made succesfully");
        System.out.println(reservation.toString());
    }
}