import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigObject {
    private List<Integer> list = new ArrayList<>(5000000);
    private int index;

    public BigObject(int index) {
        this.index = index;
    }

    public static Map<Integer, BigObject> getMap() {
        Map<Integer, BigObject> map = new HashMap<>();
        for(int i = 0; i < 100; i++) {
            map.put(i, new BigObject(i));
        }
        return map;
    }
}
