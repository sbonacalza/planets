package com.mercadolibre.planets;

import com.mercadolibre.planets.PlanetaryModel.Forecast;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Santiago on 20/12/18.
 */
public class PeriodBuilder implements Iterable<PeriodBuilder.Period> {

    private List<Period> periods;
    private Map<Forecast, AtomicInteger> counters;

    private Period last;
    private int currDay = 0;

    public PeriodBuilder() {
        periods = new LinkedList<>();
        counters = new HashMap<>();

        for (Forecast forecast : Forecast.values()) {
            counters.put(forecast, new AtomicInteger(0));
        }

    }

    /**
        Meant to be called sequentially, without skipping days or going back
     */
    public void addForecast(Forecast forecast) {
        if (last == null || last.getForecast() != forecast) { //new period
            last = new Period(forecast, currDay);

            periods.add(last);
            counters.get(forecast).incrementAndGet();
        }
        else {
            last.incDayTo();
        }

        currDay++;
    }

    public int getCount(Forecast forecast) {
        return counters.get(forecast).intValue();
    }

    @Override
    public Iterator<Period> iterator() {
        return periods.iterator();
    }

    public static class Period {
        private final Forecast forecast;
        private final int dayFrom;
        private int dayTo;

        public Period(Forecast forecast, int dayFrom) {
            this(forecast, dayFrom, dayFrom);
        }

        public Period(Forecast forecast, int dayFrom, int dayTo) {
            this.forecast = forecast;
            this.dayFrom = dayFrom;
            this.dayTo = dayTo;
        }

        public void incDayTo() {
            dayTo++;
        }

        public Forecast getForecast() {
            return forecast;
        }

        public int getDayFrom() {
            return dayFrom;
        }

        public int getDayTo() {
            return dayTo;
        }

        @Override
        public String toString() {
            return forecast + " from day " + dayFrom + " to " + dayTo;
        }
    }
}
