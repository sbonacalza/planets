package com.mercadolibre.planets;

import java.awt.geom.Point2D;

/**
 * Created by Santiago on 20/12/2018.
 */
public class Planet {

    public final double distance;
    public final double angVelocity;

    public Planet(double distance, double angVelocity) {
        this.distance = distance;
        this.angVelocity = Math.toRadians(angVelocity);
    }

    public Point2D getPos(double days) {
        double x = Math.cos(angVelocity * days) * distance;
        double y = Math.sin(angVelocity * days) * distance;

        return new Point2D.Double(x, y);
    }
}
