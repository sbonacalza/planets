package com.mercadolibre.planets;

import com.google.inject.Inject;
import com.mercadolibre.planets.PlanetaryModel.Forecast;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by Santiago on 20/12/18.
 */
public class Worker extends Thread {

    public static final Logger LOGGER = Logger.getLogger(Worker.class);

    @Inject
    private Dao forecastDao;

    @Inject
    private PlanetaryModel planetaryModel;

    @Inject
    private PeriodBuilder periodBuilder;


    @Override
    public void run() {
        for (int day = 0; day < PlanetaryModel.PREDICTION_DAYS; day++) {
            Forecast forecast = planetaryModel.predict(day);

            periodBuilder.addForecast(forecast);

            try {
                forecastDao.save(day, forecast);
            }
            catch (SQLException e) {
                LOGGER.error("Exception when saving forecast", e);
            }
        }

        try {
            forecastDao.setMaxRainDay(planetaryModel.getMaxDay());
        }
        catch (SQLException e) {
            LOGGER.error("Exception when saving max rain day", e);
        }

        for (PeriodBuilder.Period period : periodBuilder) {
            try {
                forecastDao.savePeriod(period);
            }
            catch (SQLException e) {
                LOGGER.error("Exception when saving period " + period, e);
            }
        }

        for (Forecast forecast : Forecast.values()) {
            int count = periodBuilder.getCount(forecast);

            LOGGER.info("There are " + count + " periods of " + forecast + " forecast");
        }

        LOGGER.info("The most " + Forecast.RAINY + " day will be the #" +
                    planetaryModel.getMaxDay());
    }
}
