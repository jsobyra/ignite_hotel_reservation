import org.apache.ignite.IgniteCache;
import task4.Start;

public class CacheGetReplicated {

    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite) -> {
            IgniteCache<Integer, BigObject> cache = ignite.cache("REPLICATED");
            long start = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                BigObject value = cache.get(i);
            }
            long end = System.nanoTime();
            System.out.println((end-start)/1000000000D);
        });
    }
}
