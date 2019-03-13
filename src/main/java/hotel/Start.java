package hotel;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

import java.util.function.Consumer;

public class Start {
    private final static String SERVER_CONFIG = "src/main/resources/server-config.xml";
    private final static String CLIENT_CONFIG = "src/main/resources/client-config.xml";

    public static void main(String[] args) {
        Ignition.start(SERVER_CONFIG);
    }

    public static void withIgniteDo(String configPath, Consumer<Ignite> consumer) {
        try (Ignite ignite = Ignition.start(configPath)) {
            consumer.accept(ignite);

        }
    }

    public static void withIgniteDo(Consumer<Ignite> consumer) {
        withIgniteDo(SERVER_CONFIG, consumer);
    }

    public static void withIgniteClientDo(Consumer<Ignite> consumer) {
        withIgniteDo(CLIENT_CONFIG, consumer);
    }
}
