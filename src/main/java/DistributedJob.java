import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteCallable;
import task4.Start;

import javax.cache.Cache.Entry;
import java.util.Collection;
import java.util.stream.StreamSupport;

public class DistributedJob {

    public static void main(String[] args) {
       Start.withIgniteClientDo((ignite -> {
           ClusterGroup servers = ignite.cluster().forServers();
            /*
           ClusterGroup clients = ignite.cluster().forClients();
           ClusterGroup randomServers = servers.forOthers(servers.forRandom());
           ClusterGroup oldestServer = servers.forOldest();
           ClusterGroup localNode = ignite.cluster().forLocal();
           */


           Collection<String> response = ignite.compute(servers).broadcast(new CountKeys());
           for(String item : response) {
               System.out.println(item);
           }
       }));
    }

    private static class CountKeys implements IgniteCallable<String> {
        @Override
        public String call() throws Exception {
            Ignite ignite = Ignition.ignite();
            Iterable<Entry<Object, Object>> iterable = ignite.getOrCreateCache("PARTITIONED").localEntries(CachePeekMode.PRIMARY);
            long count = StreamSupport.stream(iterable.spliterator(), false).count();
            String localId = ignite.cluster().localNode().id().toString();

            return String.format("Node %s has %d keys", localId, count);
        }
    }
}


