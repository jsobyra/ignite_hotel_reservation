package hotel.dto;

public class Hotel {
    private final int id;
    private final String name;
    private final int stars;

    public Hotel(int id, String name, int stars) {
        this.id = id;
        this.name = name;
        this.stars = stars;
    }

    public int getId() {
        return id;
    }

    public int getStars() {
        return stars;
    }

    public String getName() {
        return name;
    }
}
