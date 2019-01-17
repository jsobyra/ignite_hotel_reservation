import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.lang.IgniteRunnable;
import task4.Start;

import javax.cache.Cache;
import java.util.Collection;
import java.util.stream.StreamSupport;

public class Task3 {


/*

# Task 3 - Explore different ways of running task on grid, part 1
Setup cluster with 4 nodes - 3 servers and 1 client.
## Run tasks from client node to server nodes
experiment with different methods for running jobs on the grid, find what are basic differences between them
create simple job which would print IP address on each node and return it if method allows it
Methods to test:
broadcast
apply
call
run
You can use below code to run jobs
Ignite ignite = Ignition.ignite();
ClusterGroup clusterGroup = <prepareWantedClusterGroup>
ignite.compute(clusterGroup)).<wanted method>;
## run non ending/long running task and run it with timeout, see what result would be returned on client node and what would be logged on server node(s)
 */


    public static void main(String[] args) {
        //run();
        //apply();
        //call();
        broadcast();
    }

    private static void broadcast() {
        Start.withIgniteClientDo((ignite -> {

            Collection<String> response = ignite.compute(ignite.cluster()).broadcastAsync(new Task3.CallCountKeys()).get(1);
            for(String item : response) {
                System.out.println(item);
            }
        }));
    }

    private static void apply() {
        Start.withIgniteClientDo((ignite -> {
            ignite.compute(ignite.cluster()).applyAsync(new Task3.ApplyCountKeys(), "NotUsed").get(1);
        }));
    }

    private static void run() {
        Start.withIgniteClientDo((ignite -> {
            ignite.compute(ignite.cluster()).runAsync(new Task3.RunCountKeys()).get(100);
        }));
    }

    private static void call() {
        Start.withIgniteClientDo((ignite -> {

            String response = ignite.compute(ignite.cluster()).callAsync(new Task3.CallCountKeys()).get(1);
            System.out.println(response);
        }));
    }

    private static class ApplyCountKeys implements IgniteClosure<String, String> {
        @Override
        public String apply(String notImportant) {
            Ignite ignite = Ignition.ignite();
            Iterable<Cache.Entry<Object, Object>> iterable = ignite.getOrCreateCache("PARTITIONED").localEntries(CachePeekMode.PRIMARY);
            long count = StreamSupport.stream(iterable.spliterator(), false).count();
            String localId = ignite.cluster().localNode().id().toString();
            return String.format("Node %s has %d keys", localId, count);
        }
    }

    private static class CallCountKeys implements IgniteCallable<String> {
        @Override
        public String call() {
            Ignite ignite = Ignition.ignite();
            Iterable<Cache.Entry<Object, Object>> iterable = ignite.getOrCreateCache("PARTITIONED").localEntries(CachePeekMode.PRIMARY);
            long count = StreamSupport.stream(iterable.spliterator(), false).count();
            String localId = ignite.cluster().localNode().id().toString();
            int i = 0;
            while (i != 7){
                i += 100;
            }
            return String.format("Node %s has %d keys", localId, count);
        }
    }

    private static class RunCountKeys implements IgniteRunnable {
        @Override
        public void run() {
            Ignite ignite = Ignition.ignite();
            Iterable<Cache.Entry<Object, Object>> iterable = ignite.getOrCreateCache("PARTITIONED").localEntries(CachePeekMode.PRIMARY);
            long count = StreamSupport.stream(iterable.spliterator(), false).count();
            String localId = ignite.cluster().localNode().id().toString();
            System.out.println(String.format("Node %s has %d keys", localId, count));
        }
    }
}