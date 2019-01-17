package task5;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteRunnable;
import task4.Start;

import java.util.Random;

public class Populate {

    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            ignite.compute(ignite.cluster()).run(new Populate.PopulateNodes());
        }));
    }

    private static class PopulateNodes implements IgniteRunnable {
        @Override
        public void run() {
            Ignite ignite = Ignition.ignite();
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                IgniteCache<String, Double> cache = ignite.cache("TEST");
                double price = random.nextDouble()*10;
                cache.putIfAbsent("Item" + price, price);
            }
        }
    }
}

/*
Read java doc describing Grid Task Execution Sequence in class org.apache.ignite.compute.ComputeTask

Write task which would take list of integers and then sum with usage of task distribution
List should be splitted so each node could split different part of list for speed up.
Modify job so it would fail randomly when executing on nodes (throw your own custom exception)
To fix it implement your own failover behaviour so failed task would be retried but only in case of yours exception (see ComputeTaskAdapter#result for example)
Try what what would happen to yours failover when you run task with igniteCompute.withNoFailover()
 */