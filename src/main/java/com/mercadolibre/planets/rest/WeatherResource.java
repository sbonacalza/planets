package com.mercadolibre.planets.rest;

import com.mercadolibre.planets.Dao;
import com.mercadolibre.planets.PeriodBuilder;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.sql.SQLException;

/**
 * Created by Santiago on 20/12/18.
 */
@Path("/weather")
public class WeatherResource {

    //in a real big application this shouldn't be part of the rest
    //layer and should be moved to a controller or similar

    @Inject
    private Dao forecastDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("day") Integer day) throws SQLException {
        if (day == null) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                            .entity("day parameter is mandatory")
                            .build()
            );
        }

        PeriodBuilder.Period period = forecastDao.getPeriod(day);

        JSONObject j = new JSONObject();

        j.put("day", day);
        j.put("forecast", forecastDao.get(day));

        if (period != null) {
            JSONObject jPeriod = new JSONObject();
            jPeriod.put("dayFrom", period.getDayFrom());
            jPeriod.put("dayTo", period.getDayTo());

            j.put("period", jPeriod);
        }

        return j.toString();
    }
}
