import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import task4.Start;

public class CacheGetPartitioned {

    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite) -> {
            IgniteCache<Integer, BigObject> cache = ignite.cache("PARTITIONED");
            long start = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                BigObject value = cache.get(i);
            }
            long end = System.nanoTime();
            System.out.println((end-start)/1000000000D);
        });
    }
}
