package task7;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.internal.transactions.IgniteTxTimeoutCheckedException;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.transactions.*;
import task4.Start;

import javax.cache.CacheException;
import java.util.concurrent.locks.Lock;

public class Task4 {


    public static void main(String[] args) {
        Start.withIgniteClientDo((ignite -> {
            //ignite.compute(ignite.cluster()).run(new Task1());
            ignite.compute(ignite.cluster()).run(new Task2());
        }));
    }

    private static class Task1 implements IgniteRunnable {
        @Override
        public void run() {
            System.out.println("TASK1");
            Ignite ignite = Ignition.ignite();
            IgniteCache<Integer, Integer> cache = ignite.cache("TEST");
            cache.put(1, 1);
            try (Transaction tx = ignite.transactions().txStart(TransactionConcurrency.OPTIMISTIC,
                    TransactionIsolation.SERIALIZABLE, 300000000, 0)) {

                System.out.println("TASK1 - Read: " + cache.get(1));
                cache.put(1, 10);
                System.out.println("TASK1 - Wait before commit");
                Thread.sleep(40000);
                System.out.println("TASK1" + cache.get(1));
                tx.commit();
                System.out.println("TASK1 - Committed");
                System.out.println(System.currentTimeMillis());

            }
            catch (CacheException e) {
                if (e.getCause() instanceof TransactionTimeoutException &&
                        e.getCause().getCause() instanceof TransactionDeadlockException)

                    System.out.println(e.getCause().getCause().getMessage());
            } catch (InterruptedException e) {

            }
            System.out.println("TASK1" + cache.get(1));
        }
    }

    private static class Task2 implements IgniteRunnable {
        @Override
        public void run() {
            System.out.println("TASK2");
            Ignite ignite = Ignition.ignite();
            IgniteCache<Integer, Integer> cache = ignite.cache("TEST");

            try (Transaction tx = ignite.transactions().txStart(TransactionConcurrency.OPTIMISTIC,
                    TransactionIsolation.SERIALIZABLE, 300000000, 0)) {

                System.out.println("TASK2 - Read: " + cache.get(1));
                cache.put(1, 20);
                tx.commit();
                System.out.println("TASK2 - Committed");
                System.out.println(System.currentTimeMillis());
            }
            catch (CacheException e) {
                if (e.getCause() instanceof TransactionTimeoutException &&
                        e.getCause().getCause() instanceof TransactionDeadlockException)

                    System.out.println(e.getCause().getCause().getMessage());
            }

            System.out.println("TASK2" + cache.get(1));
        }
    }

}


/*
# Task 7 - Transactions
Questions to answer:
what configuration we need to use cache in transactions?
TRANSACTIONAL albo TRANSACTIONAL_SNAPSHOT musi byc ustawiony do uzycia cache'a w w transakcjach
could we update multiple caches in one transaction?
ten sam atomicity mode dla wszystkich cache'ow
what method in IgniteCache are fully transactional?
te które moga rzucic TransactionException
how two phase commit is working?
The Client sends a prepare message (1 Prepare) to all the Primary Nodes participating in the transaction.
The Primary Nodes acquire all their locks (depending upon whether the transaction is optimistic or pessimistic) and forward the prepare message (2 Prepare) to all the Backup Nodes.
Every node confirms to the Client (3 ACK, 4 ACK) that all the locks are acquired and the transaction is ready to commit.
The Client sends a commit message (5 Commit) to all the Primary Nodes participating in the transaction.
The Primary Nodes commit the transaction and forward the commit message (6 Commit) to all the Backup Nodes and these Backups Nodes commit the transaction.
Every node confirms to the Client that the transaction has been committed (7 ACK, 8 ACK).
what is non-repeatable read in transaction?
to committed-read - dane sa czytane bez locka, i nie cachowane w samej transakcji
what is difference between concurrency mode and isolation level?
concurrency mode jest o tym kiedy lock w transakcji jest zabierany, isolation level jest o tym co dzieje sie w samej transakcji, na jakie operacja jest tworzony w ogole lock
what is difference between OPTIMISTIC and PESSIMISTIC concurrency modes.
Lock tworzony przy koncu transakcji przed commitem (OPTIMISTIC), lock tworzony na poczatku transakcji (PESSIMISTIC)
what is most important rule to avoid dead locks when locking multiple resources?
locki na klucze uczestniczace w transakcji musza byc brane tej samej kolejnosci

TASKS
what happens if we use ATOMIC cache in transaction?
write simple program which ends with dead lock
create task which would in transaction T1 read and modify some cache value V.
Before T1 commits (but after value modification) run second task with transaction T2 which would try to update cache value V and commit before T1 finish/commits.
What would happen in each combination of concurrency model and isolation level. Prepare table with results description.
 */


/*
    Pessimistic - serializable - lock jest zalozony przy operacji czytania i zapisywania, task2 konczy sie po task1
    Pessimistic - repeteable read - lock jest zalozony przy operacji czytania i zapisywania, task2 konczy sie po task1
    Pessimistic - read committed - lock jest zalozony przy operacji zapisywania, task2 konczy sie po task1

    Optimistic - serializable - task2 konczy sie przed task1, task1 rzuca IgniteTxOptimisticCheckedException
    Optimistic - repeteable read - task2 konczy sie przed task1, nie ma sprawdzania wartosci czy zmienila sie w trakcie
    Optimistic - read committed - task2 konczy sie przed task1, nie ma sprawdzania wartosci czy zmienila sie w trakcie
 */