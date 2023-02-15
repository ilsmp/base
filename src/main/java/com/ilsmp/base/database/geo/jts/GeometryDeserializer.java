package com.ilsmp.base.database.geo.jts;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ilsmp.base.database.geo.parsers.GenericGeometryParser;
import com.ilsmp.base.database.geo.parsers.GeometryCollectionParser;
import com.ilsmp.base.database.geo.parsers.GeometryParser;
import com.ilsmp.base.database.geo.parsers.LineStringParser;
import com.ilsmp.base.database.geo.parsers.MultiLineStringParser;
import com.ilsmp.base.database.geo.parsers.MultiPointParser;
import com.ilsmp.base.database.geo.parsers.MultiPolygonParser;
import com.ilsmp.base.database.geo.parsers.PointParser;
import com.ilsmp.base.database.geo.parsers.PolygonParser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryDeserializer<T extends Geometry> extends JsonDeserializer<T> {
    public static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private static Map<String, GeometryParser> parserMap = new ConcurrentHashMap<>();
    public GeometryDeserializer() {
        obtainParser();
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode root = oc.readTree(jsonParser);
        return ((GeometryParser<T>) parserMap.get(jsonParser.getCurrentName())).geometryFromJson(root);
    }

    private void obtainParser() {
        parserMap.put(obtainName(Geometry.class), new GenericGeometryParser(geometryFactory));
        parserMap.put(obtainName(Point.class), new PointParser(geometryFactory));
        parserMap.put(obtainName(MultiPoint.class), new MultiPointParser(geometryFactory));
        parserMap.put(obtainName(LineString.class), new LineStringParser(geometryFactory));
        parserMap.put(obtainName(MultiLineString.class), new MultiLineStringParser(geometryFactory));
        parserMap.put(obtainName(Polygon.class), new PolygonParser(geometryFactory));
        parserMap.put(obtainName(MultiPolygon.class), new MultiPolygonParser(geometryFactory));
        parserMap.put(obtainName(GeometryCollection.class), new GeometryCollectionParser(geometryFactory, new GenericGeometryParser(geometryFactory)));
    }

    private String obtainName(Class cla) {
        char[] chars = cla.getSimpleName().toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
