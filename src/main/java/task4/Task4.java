package task4;

import org.apache.ignite.IgniteException;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterTopologyException;
import org.apache.ignite.compute.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Task4 {


    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
            Integer result = ignite.compute().withNoFailover().execute(SumInteger.class, list);
            System.out.println(result);
        }));
    }

    private static class SumInteger extends ComputeTaskAdapter<List<Integer>, Integer> {

        @Override
        public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> nodes, List<Integer> arg) {
            Map<ComputeJob, ClusterNode> map = new HashMap<>();
            Iterator<List<Integer>> iterator = partition(arg, arg.size()/nodes.size()).iterator();
            System.out.println(nodes.size());
            for(int i = 0; i < nodes.size(); i++) {
                List<Integer> partition = iterator.next();
                map.put(new ComputeJobAdapter() {
                    @Override
                    public Object execute() {
                        System.out.println(partition.size());
                        Integer result = partition.stream().mapToInt(x -> x).sum();
                        if(new Random().nextInt() % 2 == 0) throw new MyException();
                        System.out.println(result);
                        return result;
                    }
                }, nodes.get(i));
            }

            return map;
        }

        private static Collection<List<Integer>> partition(List<Integer> list, int partitionNumber) {
            AtomicInteger counter = new AtomicInteger(0);
            return list.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / partitionNumber))
                    .values();
        }

        @Override
        public Integer reduce(List<ComputeJobResult> results) {
            return results.stream()
                    .map(job -> job.<Integer>getData())
                    .mapToInt(x -> x)
                    .sum();
        }

        @Override
        public ComputeJobResultPolicy result(ComputeJobResult res, List<ComputeJobResult> rcvd) throws IgniteException {
            IgniteException e = res.getException();

            // Try to failover if result is failed.
            if (e != null) {
                // Don't failover user's code errors.
                if (e instanceof ComputeExecutionRejectedException ||
                        e instanceof ClusterTopologyException ||
                        e instanceof MyException ||
                        // Failover exception is always wrapped.
                        e.hasCause(ComputeJobFailoverException.class))
                    return ComputeJobResultPolicy.FAILOVER;

                throw new IgniteException("Remote job threw user exception (override or implement ComputeTask.result(..) " +
                        "method if you would like to have automatic failover for this exception): " + e.getMessage(), e);
            }

            // Wait for all job responses.
            return ComputeJobResultPolicy.WAIT;
        }
    }

}