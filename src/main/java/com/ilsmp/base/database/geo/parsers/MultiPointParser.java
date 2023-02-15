package com.ilsmp.base.database.geo.parsers;

import static com.ilsmp.base.database.geo.jts.GeoJson.COORDINATES;
import static com.ilsmp.base.database.geo.parsers.PointParser.coordinatesFromJson;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;

public class MultiPointParser extends BaseParser implements GeometryParser<MultiPoint> {

    public MultiPointParser(GeometryFactory geometryFactory) {
        super(geometryFactory);
    }

    public MultiPoint multiPointFromJson(JsonNode root) {
        return geometryFactory.createMultiPointFromCoords(
                coordinatesFromJson(root.get(COORDINATES)));
    }

    @Override
    public MultiPoint geometryFromJson(JsonNode node) throws JsonMappingException {
        return multiPointFromJson(node);
    }
}
