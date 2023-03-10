package com.ilsmp.base.database.geo.parsers;

import java.util.HashMap;
import java.util.Map;

import static com.ilsmp.base.database.geo.jts.GeoJson.GEOMETRY_COLLECTION;
import static com.ilsmp.base.database.geo.jts.GeoJson.LINE_STRING;
import static com.ilsmp.base.database.geo.jts.GeoJson.MULTI_LINE_STRING;
import static com.ilsmp.base.database.geo.jts.GeoJson.MULTI_POINT;
import static com.ilsmp.base.database.geo.jts.GeoJson.MULTI_POLYGON;
import static com.ilsmp.base.database.geo.jts.GeoJson.POINT;
import static com.ilsmp.base.database.geo.jts.GeoJson.POLYGON;
import static com.ilsmp.base.database.geo.jts.GeoJson.TYPE;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class GenericGeometryParser extends BaseParser implements GeometryParser<Geometry> {

    private Map<String, GeometryParser> parsers;

    public GenericGeometryParser(GeometryFactory geometryFactory) {
        super(geometryFactory);
        parsers = new HashMap<String, GeometryParser>();
        parsers.put(POINT, new PointParser(geometryFactory));
        parsers.put(MULTI_POINT, new MultiPointParser(geometryFactory));
        parsers.put(LINE_STRING, new LineStringParser(geometryFactory));
        parsers.put(MULTI_LINE_STRING, new MultiLineStringParser(geometryFactory));
        parsers.put(POLYGON, new PolygonParser(geometryFactory));
        parsers.put(MULTI_POLYGON, new MultiPolygonParser(geometryFactory));
        parsers.put(GEOMETRY_COLLECTION, new GeometryCollectionParser(geometryFactory, this));
    }

    @Override
    public Geometry geometryFromJson(JsonNode node) throws JsonMappingException {
        String typeName = node.get(TYPE).asText();
        GeometryParser parser = parsers.get(typeName);
        if (parser != null) {
            return parser.geometryFromJson(node);
        } else {
            throw new JsonMappingException("Invalid geometry type: " + typeName);
        }
    }
}
