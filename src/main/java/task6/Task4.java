package task6;

import org.apache.ignite.*;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterTopologyException;
import org.apache.ignite.compute.*;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteRunnable;
import task4.MyException;
import task4.Start;

import javax.cache.Cache;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Task4 {


    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            //ignite.compute(ignite.cluster()).broadcast(new CreateAtomic());
            //ignite.compute(ignite.cluster()).broadcast(new IncrementAtomic());
            ignite.compute().broadcast(new Locker());
        }));
    }

    private static class CreateAtomic implements IgniteRunnable {
        @Override
        public void run() {
            Ignite ignite = Ignition.ignite();
            boolean create = ignite.cluster().localNode().isClient();
            ignite.atomicLong("ATOMIC", 0, create);
        }
    }

    private static class IncrementAtomic implements IgniteRunnable {
        @Override
        public void run() {
            Ignite ignite = Ignition.ignite();
            System.out.println(ignite.cluster().localNode().isClient());
            System.out.println(ignite.atomicLong("ATOMIC", 0, false).incrementAndGet());
        }
    }

    private static class Locker implements IgniteRunnable {
        @Override
        public void run() {
            Ignite ignite = Ignition.ignite();
            IgniteCache<String, Integer> cache = ignite.cache("TEST");
            Lock lock = cache.lock("keyLock");

            System.out.println("Connecting to bank" + System.currentTimeMillis());
            try {
                lock.lock();
                System.out.println("Transfering $1,000,000" + System.currentTimeMillis());
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("Exception");
            } finally {
                lock.unlock();
            }
            System.out.println("Operation cleanup" + System.currentTimeMillis());
        }
    }

}


