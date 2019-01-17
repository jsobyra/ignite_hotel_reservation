import org.apache.ignite.IgniteCache;
import task4.Start;

import java.util.Map;

public class CachePutPartitioned {

    public static void main(String[] args) {
        Map<Integer, BigObject> map = BigObject.getMap();
        Start.withIgniteClientDo((ignite) -> {
            IgniteCache<Integer, BigObject> cache = ignite.cache("PARTITIONED");
            long start = System.nanoTime();
            cache.putAll(map);
            long end = System.nanoTime();
            System.out.println((end-start)/1000000000D);
        });
    }
}
