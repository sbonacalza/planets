package com.mercadolibre.planets.rest;

import com.mercadolibre.planets.Dao;
import com.mercadolibre.planets.PeriodBuilder.Period;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.sql.SQLException;

/**
 * Created by Santiago on 21/12/18.
 */
@Path("/periods")
public class PeriodsResource {

    //in a real big application this shouldn't be part of the rest
    //layer and should be moved to a controller or similar

    @Inject
    private Dao dao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get() throws SQLException {
        JSONArray arr = new JSONArray();
        
        for (Period period : dao.getPeriods()) {
        	JSONObject jPeriod = new JSONObject();
            
        	jPeriod.put("dayFrom", period.getDayFrom());
            jPeriod.put("dayTo", period.getDayTo());
            jPeriod.put("forecast", period.getForecast().name());
            
            arr.put(jPeriod);
		}
        
        return arr.toString();
    }
}
