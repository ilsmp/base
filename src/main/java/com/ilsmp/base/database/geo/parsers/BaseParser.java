package com.ilsmp.base.database.geo.parsers;

import org.locationtech.jts.geom.GeometryFactory;

public class BaseParser {

    protected GeometryFactory geometryFactory;

    public BaseParser(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

}
