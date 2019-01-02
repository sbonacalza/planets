package com.mercadolibre.planets;

import java.awt.geom.Point2D;

import static java.lang.Math.*;

/**
 * Created by Santiago on 20/12/18.
 */
public class PlanetaryModel {

    public static final int PREDICTION_DAYS = 10 * 365;

    public static final Planet FERENGI = new Planet(500, -1);
    public static final Planet VULCANO = new Planet(1000, 5);
    public static final Planet BETASOIDE = new Planet(2000, -3);

    private static final Point2D SUN = new Point2D.Double(0, 0);

    public static final int APPROXIMATION_STEPS = 1000;

    public enum Alignment { ALL, PLANETS, NONE };

    public enum Forecast { RAINY, OPTIMAL, NORMAL, DROUGHT, UNKNOWN }

    private double maxArea = Double.MIN_VALUE;
    private int maxDay = 0;


    public PlanetaryModel() {

    }

    /**
     * Returns the day with the maximum amount of rain.
     * It is lazily calculated only when calling predict.
     */
    public int getMaxDay() {
        return maxDay;
    }

    public Forecast predict(int day) {
        Alignment alignment = getAlignment(day);

        switch (alignment) {
            case NONE:
                double area = getArea(day);

                if (area > 0) {
                    if (area > maxArea) {
                        maxDay = day;
                        maxArea = area;
                    }

                    return Forecast.RAINY;
                }

                return Forecast.NORMAL;

            case PLANETS:
                return Forecast.OPTIMAL;

            case ALL:
                return Forecast.DROUGHT;

            default:
                return Forecast.NORMAL;
        }
    }

    private double getArea(int days) {
        Point2D F = FERENGI.getPos(days);
        Point2D V = VULCANO.getPos(days);
        Point2D B = BETASOIDE.getPos(days);

        boolean sunInTriangle = sunInTriangle(F, V, B);

        if (sunInTriangle) {

            //source http://www.mathplanet.com/education/algebra-2/matrices/determinants
            return Math.abs(F.getX() * (V.getY() - B.getY()) +
                            V.getX() * (B.getY() - F.getY()) +
                            B.getX() * (F.getY() - V.getY())) / 2;
        }

        return -1;
    }

    private Alignment getAlignment(int days) {
        double r1 = FERENGI.distance;
        double r2 = VULCANO.distance;
        double r3 = BETASOIDE.distance;
        double w1 = FERENGI.angVelocity;
        double w2 = VULCANO.angVelocity;
        double w3 = BETASOIDE.angVelocity;

        boolean aligned = false;
        double t = 0; //time of alignment

        for (int day = days * APPROXIMATION_STEPS; day < (days + 1) * APPROXIMATION_STEPS; day++) {
            t = day / (double)APPROXIMATION_STEPS;

            //source http://www.mathpages.com/home/kmath161/kmath161.htm
            double slopes = r1 * r2 * sin((w2 - w1) * t) +
                            r2 * r3 * sin((w3 - w2) * t) +
                            r3 * r1 * sin((w1 - w3) * t);

            if (equal(slopes, 0)) {
                aligned = true;
                break;
            }
        }

        if (aligned) {
            double slope = getSlope(FERENGI, VULCANO, t);
            double sunSlope = getSlope(FERENGI.getPos(t), SUN);

            if (equal(slope, sunSlope)) {
                return Alignment.ALL;
            }
            else {
                return Alignment.PLANETS;
            }
        }

        return Alignment.NONE;
    }

    private double getSlope(Planet pa, Planet pb, double days) {
        Point2D a = pa.getPos(days);
        Point2D b = pb.getPos(days);

        return getSlope(a, b);
    }

    private double getSlope(Point2D a, Point2D b) {
        double num = b.getY() - a.getY();
        double den = b.getX() - a.getX();

        if (equal(den, 0)) {
            return Double.POSITIVE_INFINITY;
        }

        return num / den;
    }

    private boolean equal(double d1, double d2) {
        return Math.abs(d1 - d2) < 0.0000001;
    }


    //source: http://stackoverflow.com/questions/2049582/how-to-determine-a-point-in-a-2d-triangle
    private double sign (Point2D p1, Point2D p2, Point2D p3) {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) -
               (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    private boolean sunInTriangle(Point2D v1, Point2D v2, Point2D v3) {
        boolean b1, b2, b3;

        b1 = sign(SUN, v1, v2) < 0.0f;
        b2 = sign(SUN, v2, v3) < 0.0f;
        b3 = sign(SUN, v3, v1) < 0.0f;

        return ((b1 == b2) && (b2 == b3));
    }
}
