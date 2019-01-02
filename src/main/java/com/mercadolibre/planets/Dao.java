package com.mercadolibre.planets;

import com.mercadolibre.planets.PeriodBuilder.Period;
import com.mercadolibre.planets.PlanetaryModel.Forecast;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Santiago on 20/12/18.
 */
public class Dao {

    public static final String FILE = "./forecast";

    private Connection connection;

    public Dao() throws Exception {
        Class.forName("org.h2.Driver");

        connection = DriverManager.getConnection("jdbc:h2:" + FILE +
                ";TRACE_LEVEL_FILE=0;LOG=0;UNDO_LOG=0;LOCK_MODE=0;FILE_LOCK=NO");

        update( "DROP TABLE IF EXISTS forecasts");
        update( "DROP TABLE IF EXISTS max_rain");
        update( "DROP TABLE IF EXISTS periods");

        update( "CREATE TABLE forecasts (" +
                "	day BIGINT NOT NULL, " +
                "   forecast VARCHAR(255), " +
                "   PRIMARY KEY(day) " +
                ")");

        update( "CREATE TABLE max_rain (" +
                "	day BIGINT NOT NULL, " +
                "   PRIMARY KEY(day) " +
                ")");

        update( "CREATE TABLE periods (" +
                "	day_from BIGINT NOT NULL, " +
                "	day_to BIGINT NOT NULL, " +
                "   forecast VARCHAR(255) " +
                ")");
    }

    public void setMaxRainDay(int day) throws SQLException {
        update("DELETE FROM max_rain");
        update("INSERT INTO max_rain VALUES (" + day + ")"); //lazy with prepared statements
    }

    public int getMaxRainDay() throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT day FROM max_rain LIMIT 1");
        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }

        return -1;
    }

    public void savePeriod(PeriodBuilder.Period period) throws SQLException {
        PreparedStatement st = prepare("INSERT INTO periods VALUES (?, ?, ?)");

        st.setInt(1, period.getDayFrom());
        st.setInt(2, period.getDayTo());
        st.setString(3, period.getForecast().name());

        st.executeUpdate();
        st.close();
    }

    public PeriodBuilder.Period getPeriod(int day) throws SQLException {
        PreparedStatement st = null;
        try {
            st = prepare("SELECT * FROM periods p WHERE ? BETWEEN p.day_from AND p.day_to");
            st.setInt(1, day);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return new PeriodBuilder.Period(Forecast.valueOf(rs.getString("forecast")),
                                                rs.getInt("day_from"),
                                                rs.getInt("day_to"));
            }

            return null;
        }
        finally {
            if (st != null) st.close();
        }
    }
    
    public List<Period> getPeriods() throws SQLException {
    	List<Period> periods = new LinkedList<>();
    	
        PreparedStatement st = null;
        try {
            st = prepare("SELECT * FROM periods");

            ResultSet rs = st.executeQuery();

            while(rs.next()) {
                periods.add(new Period(Forecast.valueOf(rs.getString("forecast")),
                                       rs.getInt("day_from"), rs.getInt("day_to")));
            }

            return periods;
        }
        finally {
            if (st != null) st.close();
        }
    }

    public void save(int day, Forecast forecast) throws SQLException {
        PreparedStatement st = prepare("INSERT INTO forecasts VALUES (?, ?)");

        st.setInt(1, day);
        st.setString(2, forecast.name());

        st.executeUpdate();
        st.close();
    }

    public Forecast get(int day) throws SQLException {
        PreparedStatement st = prepare("SELECT forecast FROM forecasts WHERE day = ?");

        st.setInt(1, day);

        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            return Forecast.valueOf((rs.getString("forecast")));
        }

        st.close();

        return Forecast.UNKNOWN;
    }

    private int update(String sql) throws SQLException {
        PreparedStatement st = connection.prepareStatement(sql);
        int affected = st.executeUpdate();
        st.close();

        return affected;
    }

    private PreparedStatement prepare(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }
}
