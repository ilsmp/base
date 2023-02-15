package com.ilsmp.base.database.geo.jts;

import java.util.ArrayList;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * @author ZJH Title: JtsTool Package com.zhihui.gongdi.tool.jts Description: jts geo转对象工具 Date 2020/8/6 15:13
 */
public class JtsTool {

    public static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    /**
     * geojson转换空间数据对象
     *
     * @param geoJson
     * @param <T>
     * @return
     */
    public static <T extends Geometry> T geoJsonToWKT(String geoJson) {
        T geometry = null;
        if (geoJson != null && !"".equals(geoJson)) {
            try {
                geometry = (T) new WKTReader().read(geoJson);
                geometry.setSRID(4326);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return geometry;
    }


    public static <T extends Geometry> T geoArrayToGeo(Map<String, Object> geo) {
        if ("Point".equals(geo.get("type"))) {
            double[] coordinates = (double[]) geo.get("coordinates");
            Coordinate coordinate = new Coordinate(coordinates[0], coordinates[1]);
            return (T) geometryFactory.createPoint(coordinate);
        } else if ("Polygon".equals(geo.get("type"))) {
            ArrayList<ArrayList<ArrayList>> coordinates = (ArrayList<ArrayList<ArrayList>>) geo.get("coordinates");
            ArrayList<ArrayList> cs = coordinates.get(0);
            if (cs == null || cs.size() < 3) {
                return null;
            } else if (!(cs.get(0).equals(cs.get(cs.size() - 1)))) {
                return null;
            }
            Coordinate[] coos = new Coordinate[cs.size()];
            for (int i = 0; i < cs.size(); i++) {
                ArrayList<Double> coo = cs.get(i);
                coos[i] = new Coordinate(coo.get(0), coo.get(1));
            }
            return (T) geometryFactory.createPolygon(coos);
        } else {
            return null;
        }
    }
}
