package com.loadingking.loading_king.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class GeometryUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtils {

    private static final GeometryFactory geometryFactory
            = new GeometryFactory(new PrecisionModel(), 4326);

    public static Point createPoint(double x, double y) {
        return geometryFactory.createPoint(new Coordinate(x, y));
    }
}
}
