package hotel.dto;

import org.apache.ignite.Ignite;

import java.time.LocalDate;

public class Criteria {
    private final Ignite ignite;
    private final String cityId;
    private final int personsNumber;
    private final int stars;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Criteria(Ignite ignite, String cityId, int personsNumber, int stars, LocalDate startDate, LocalDate endDate) {
        this.ignite = ignite;
        this.cityId = cityId;
        this.stars = stars;
        this.personsNumber = personsNumber;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Ignite getIgnite() {
        return ignite;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getPersonsNumber() {
        return personsNumber;
    }

    public String getCityId() {
        return cityId;
    }

    public int getStars() {
        return stars;
    }
}
