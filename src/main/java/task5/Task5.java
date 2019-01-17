package task5;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskAdapter;
import task4.Start;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Task5 {


    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            List<String> results = ignite.compute().execute(FilterPrice.class, 9.0);
            for(String item : results) {
                System.out.println(item);
            }
        }));
    }

    private static class FilterPrice extends ComputeTaskAdapter<Double, List<String>> {

        @Override
        public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> nodes, Double arg) {
            Map<ComputeJob, ClusterNode> map = new HashMap<>();
            Ignite ignite = Ignition.ignite();
            IgniteCache<String, Double> cache = ignite.cache("TEST");

            for(ClusterNode node : nodes) {
                map.put(new ComputeJobAdapter() {
                    @Override
                    public Object execute() {
                       List<String> keys = cache.query(new ScanQuery<String, Double>(
                                        (k, p) -> p > arg),
                                Cache.Entry::getKey
                        ).getAll();
                        return keys;
                    }
                }, node);
            }
            return map;
        }

        @Override
        public List<String> reduce(List<ComputeJobResult> results) {
            return results.stream()
                    .map(job -> job.<List<String>>getData())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }

}