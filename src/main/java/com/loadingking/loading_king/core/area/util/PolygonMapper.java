package com.loadingking.loading_king.core.area.util;

import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class PolygonMapper {

    private static final GeometryFactory geometryFactory = new  GeometryFactory(
            new PrecisionModel(),
            4326);

    public static MultiPolygon toMultiPolygon(List<List<List<List<Double>>>> coords) {

        Polygon[] polygons = new Polygon[coords.size()];

        for (int i = 0; i < coords.size(); i++) {

            List<List<List<Double>>> polygonCoords = coords.get(i);
            if (polygonCoords == null || polygonCoords.isEmpty()) {
                throw new IllegalArgumentException("Polygon #" + i + "의 좌표가 비어 있습니다.");
            }

            LinearRing shell = toLinearRing(polygonCoords.get(0));

            int holeCount = polygonCoords.size() - 1;
            LinearRing[] holes = new LinearRing[holeCount > 0 ? holeCount : 0];

            for (int j = 1; j < polygonCoords.size(); j++) {
                holes[j - 1] = toLinearRing(polygonCoords.get(j));
            }

            polygons[i] = geometryFactory.createPolygon(shell, holes);
        }

        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons);
        multiPolygon.setSRID(4326);

        return multiPolygon;
    }

    public static LinearRing toLinearRing(List<List<Double>> ringCoords) {

        Coordinate[] coordinates = new Coordinate[ringCoords.size()];

        for (int k = 0; k < ringCoords.size(); k++) {
            List<Double> c = ringCoords.get(k);
            coordinates[k] = new Coordinate(c.get(0), c.get(1));
        }

        return geometryFactory.createLinearRing(coordinates);
    }


    public static List<List<List<List<Double>>>> toCoordinateList(MultiPolygon multiPolygon) {

        List<List<List<List<Double>>>> result = new ArrayList<>();

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {

            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            List<List<List<Double>>> polygonCoords = new ArrayList<>();

            polygonCoords.add(toRingList(polygon.getExteriorRing()));

            for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                polygonCoords.add(toRingList(polygon.getInteriorRingN(j)));
            }

            result.add(polygonCoords);
        }
        return result;
    }

    public static List<List<Double>> toRingList(LineString ring) {

        List<List<Double>> ringCoords = new ArrayList<>();
        Coordinate[] coords = ring.getCoordinates();

        for (Coordinate c : coords) {
            ringCoords.add(List.of(c.getX(), c.getY()));
        }
        return ringCoords;
    }
}
