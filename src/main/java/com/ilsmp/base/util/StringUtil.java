package com.ilsmp.base.util;

import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

/**
 * @author: ZJH Title: MyTool Package com.zhihui.gongdi.tool Description: 工具类 Date 2019/10/23 11:28
 */
@Slf4j
public class StringUtil {

    /**
     * 赤道半径
     */
    private static final double EARTH_RADIUS = 6378.137;

    /***
     * 还原对象反射状态
     * @param member 方法、属性、构造方法
     */
    public static void resetAccessible(Member member) {
        if (((Field) member).getModifiers() != Modifier.PUBLIC) {
            ((Field) member).setAccessible(false);
        } else if (((Method) member).getModifiers() != Modifier.PUBLIC) {
            ((Method) member).setAccessible(false);
        } else if (((Constructor) member).getModifiers() != Modifier.PUBLIC) {
            ((Constructor) member).setAccessible(false);
        }
    }

    /**
     * 获取任意成员变量的值
     * @param instance 要获取的对象
     * @param filedName 获取的变量名称
     * @return 返回获取变量的信息（需要强转）
     */
    public static Object getFieldValue(Object instance, String filedName) throws NoSuchFieldException,
            IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(filedName);
        field.setAccessible(true);
        Object value = field.get(instance);
        resetAccessible(field);
        return value;
    }

    /**
     * 设置任意成员的值
     * @param instance 要获取的对象
     * @param fieldName 要获取的变量名
     * @param value 设置的值
     */
    public static void setFieldValue(Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
        resetAccessible(field);
    }

    /**
     * 访问任意方法
     * @param instance 要获取的对象
     * @param methodName 私有方法的名称
     * @param classes  CLASS的返回信息
     * @param objects 参数信息
     * @return
     */
    public static Object getMethodParam(Object instance, String methodName, Class[] classes, String objects) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = instance.getClass().getDeclaredMethod(methodName, classes);
        method.setAccessible(true);
        Object param = method.invoke(instance, objects);
        resetAccessible(method);
        return param;
    }

    /**
     * 构建对象的属性map
     * param clazz
     */
    private static Map<String, Field> obtainFieldsMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    /**
     * 对象强转数字类型，不是的转为0
     * number 要判断对象
     */
    public static Number object2Num(Object number) {
        if (number instanceof Number) {
            return (Number) number;
        } else {
            return 0;
        }
    }

    /**
     * 判断是否是数值
     * number 要判断对象
     * number
     */
    public static boolean isNum(Object number) {
        return number instanceof Number;
    }

    /**
     * 数值类型相加
     */
    public static <T extends Number> Number addObject2To1(T t1, T t2) {
        Number result = 0;
        if (t1.getClass().equals(Double.class)) {
            result = t1.doubleValue();
        } else if (t1.getClass().equals(Float.class)) {
            result = t1.floatValue();
        } else if (t1.getClass().equals(BigInteger.class)) {
            result = BigInteger.valueOf(t1.longValue());
        } else {
            result = t1.longValue();
        }
        if (t2.getClass().equals(Double.class)) {
            result = result.doubleValue() + t2.doubleValue();
        } else if (t2.getClass().equals(Float.class)) {
            result = result.floatValue() + t2.floatValue();
        } else if (t2.getClass().equals(BigInteger.class)) {
            result = BigInteger.valueOf(result.longValue() + t2.longValue());
        } else {
            result = result.longValue() + t2.longValue();
        }
        return result;
    }

    /**
     * biginteger相加
     *
     * @param f
     * @param s
     * @return
     */
    public static String bigNumberAdd(BigInteger f, BigInteger s) {
        // 翻转两个字符串，并转换成数组
        char[] a = new StringBuffer(f.toString()).reverse().toString().toCharArray();
        char[] b = new StringBuffer(s.toString()).reverse().toString().toCharArray();
        int lenA = a.length;
        int lenB = b.length;
        // 计算两个长字符串中的较长字符串的长度
        int len = lenA > lenB ? lenA : lenB;
        int[] result = new int[len + 1];
        for (int i = 0; i < len + 1; i++) {
            // 如果当前的i超过了其中的一个，就用0代替，和另一个字符数组中的数字相加
            int aint = i < lenA ? (a[i] - '0') : 0;
            int bint = i < lenB ? (b[i] - '0') : 0;
            result[i] = aint + bint;
        }
        // 处理结果集合，如果大于10的就向前一位进位，本身进行除10取余
        for (int i = 0; i < result.length; i++) {
            if (result[i] > 10) {
                result[i + 1] += 1;
                result[i] %= 10;
            }
        }
        StringBuffer sb = new StringBuffer();
        // 该字段用于标识是否有前置0，如果有就不要存储
        boolean flag = true;
        for (int i = len; i >= 0; i--) {
            if (result[i] == 0 && flag) {
                continue;
            } else {
                flag = false;
            }
            sb.append(result[i]);
        }
        return sb.toString();
    }

    /**
     * 合并两个Map对象的数值属性
     *
     * @param l1
     * @param l2
     * @return 第一个对象
     */
    public static Map<String, Object> obtain1With2(Map<String, Object> l1, Map<String, Object> l2) {
        String key;
        for (Map.Entry<String, Object> entry : l1.entrySet()) {
            key = entry.getKey();
            if ("id".equals(key) || "pid".equals(key)) {
                continue;
            } else if (isNum(l2.get(key))) {
                entry.setValue(addObject2To1((Number) entry.getValue(), (Number) l2.get(key)));
            }
        }
        return l1;
    }

    public static <T> T obtain1With2(T t1, T t2) {
        Field[] fields = t1.getClass().getDeclaredFields();
        String key;
        for (Field field : fields) {
            field.setAccessible(true);
            key = field.getName();
            if ("id".equals(key) || "pid".equals(key)) {
                continue;
            } else {
                try {
                    Object value2 = getFieldValue(t2, key);
                    if (isNum(value2)) {
                        if (field.getType().equals(Double.class)) {
                            field.set(t1, ((Number) field.get(t1)).doubleValue() + ((Number) value2).doubleValue());
                        } else if (field.getType().equals(Float.class)) {
                            field.set(t1, ((Number) field.get(t1)).floatValue() + ((Number) value2).floatValue());
                        } else if (field.getType().equals(BigInteger.class)) {
                            field.set(t1, BigInteger.valueOf(((Number) field.get(t1)).longValue()
                                    + ((Number) value2).longValue()));
                        } else {
                            field.set(t1, ((Number) field.get(t1)).longValue() + ((Number) value2).longValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resetAccessible(field);
        }
        return t1;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * Description : 通过经纬度获取距离(单位：米) Group :
     *
     * @param origin
     *         出发点
     * @param destination
     *         目的地
     * @return double
     * @author honghh
     * @date 2019/2/13 0013 15:50
     */
    public static double getDistance(String origin, String destination) {
        if (origin == null) {
            log.info("出发点 经纬度不可以为空！");
            return 0;
        }
        if (destination == null) {
            log.info("目的地 经纬度不可以为空！");
            return 0;
        }
        String[] temp = origin.split(",");
        String[] temp2 = destination.split(",");

        double radLat1 = rad(Double.parseDouble(temp[1]));
        double radLat2 = rad(Double.parseDouble(temp2[1]));
        double a = radLat1 - radLat2;
        double b = rad(Double.parseDouble(temp[0])) - rad(Double.parseDouble(temp2[0]));
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        // 保留两位小数
        s = Math.round(s * 1000d) / 1000d;
        s = s * 1000;
        return s;
    }

    /**
     * Description : 通过经纬度获取距离(单位：米) Group :
     *
     * @param originLon
     *         出发点经度
     * @param originLat
     *         出发点纬度
     * @param destinationLon
     *         目的地经度
     * @param destinationLat
     *         目的地纬度
     * @return double
     * @author honghh
     * @date 2019/2/15 0015 9:14
     */
    public static double getDistance(String originLon, String originLat, String destinationLon, String destinationLat) {
        if (isEmpty(originLon)) {
            log.info("出发点 经度不可以为空！");
            return 0;
        }
        if (isEmpty(originLat)) {
            log.info("出发点 纬度不可以为空！");
            return 0;
        }
        if (isEmpty(destinationLon)) {
            log.info("目的地 经度不可以为空！");
            return 0;
        }
        if (isEmpty(destinationLat)) {
            log.info("目的地 纬度不可以为空！");
            return 0;
        }

        double radLat1 = rad(Double.parseDouble(originLat));
        double radLat2 = rad(Double.parseDouble(destinationLat));
        double a = radLat1 - radLat2;
        double b = rad(Double.parseDouble(originLon)) - rad(Double.parseDouble(destinationLon));
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        // 保留3位小数
        s = Math.round(s * 1000d) / 1000d;
        s = s * 1000;
        return s;
    }

    /**
     * 根据两个经纬度坐标计算距离
     */
    public static double getDistance(double lon1, double lat1, double lon2, double lat2) {

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);

        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);

        double c = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math
                        .pow(Math.sin(b / 2), 2)));

        c = c * EARTH_RADIUS;
        c = Math.round(c * 1000d) / 1000d;
        return c * 1000;
    }
    /**
     * 2 判断是否在经纬度范围内 3 Title: isInRange 4 Description: 5 param point 6 param left 7 param right 8
     * return 9 return
     * boolean 10 throws 11
     */
    public static boolean isInRange(double point, double left, double right) {
        return point >= Math.min(left, right) && point <= Math.max(left, right);
    }

    /**
     * 判断是否在矩形范围内
     *
     * @param lat
     *         测试点纬度
     * @param lng
     *         测试点经度
     * @param minLat
     *         纬度范围限制1
     * @param maxLat
     *         纬度范围限制2
     * @param minLng
     *         经度范围限制1
     * @param maxLng
     *         经度范围限制2
     * @return
     */
    public static boolean isInRectangleArea(double lat, double lng, double minLat, double maxLat, double minLng,
                                            double maxLng) {

        if (isInRange(lat, minLat, maxLat)) {
            //如果在维度范围内
            if (minLng * maxLng > 0) {
                if (isInRange(lng, minLng, maxLng)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (Math.abs(minLng) + Math.abs(maxLng) < 180) {
                    if (isInRange(lng, minLng, maxLng)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    double left = Math.max(minLng, maxLng);
                    double right = Math.min(minLng, maxLng);
                    if (isInRange(lng, left, 180) || isInRange(lng, right, -180)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    /**
     * 计算是否在圆上(单位/千米)
     *
     * @param radius
     *         半径
     * @param lat1
     *         维度
     * @param lng1
     *         经度
     * @return
     */
    public static boolean isInCircle(double radius, double lat1, double lng1, double lat2, double lng2) {

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(
                Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        if (s > radius) {
            //不在圆上
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断点是否在多边形内
     * @param polygon
     *         多边形
     * @param point
     *         检测点
     * @return 点在多边形内返回true，否则返回false
     */
    public static boolean IsPtInPoly(List<Point2D.Double> polygon, Point2D.Double point) {

        int N = polygon.size();
        //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        boolean boundOrVertex = true;
        //cross points count of x--交叉点计数X
        int intersectCount = 0;
        //浮点类型计算时候与0比较时候的容差
        double precision = 2e-10;
        //neighbour bound vertices--临近绑定顶点
        Point2D.Double p1, p2;
        //当前点
        Point2D.Double p = point;

        p1 = polygon.get(0);
        //left vertex--左顶点
        for (int i = 1; i <= N; ++i) {
            //check all rays--检查所有射线
            if (p.equals(p1)) {
                //p is an vertex--p是一个顶点
                return boundOrVertex;
            }
            //right vertex--右顶点
            p2 = polygon.get(i % N);
            if (p.x < Math.min(p1.x, p2.x) || p.x > Math.max(p1.x, p2.x)) {
                //ray is outside of our interests--射线不在我们的兴趣范围之内
                p1 = p2;
                continue;//next ray left point--下一条射线的左边点
            }
            //ray is crossing over by the algorithm(common part of)--射线被算法穿越(常见的一部分)
            if (p.x > Math.min(p1.x, p2.x) && p.x < Math.max(p1.x, p2.x)) {
                //x is before of ray--x在射线之前
                if (p.y <= Math.max(p1.y, p2.y)) {
                    //overlies on a horizontal ray--在一条水平射线上
                    if (p1.x == p2.x && p.y >= Math.min(p1.y, p2.y)) {
                        return boundOrVertex;
                    }
                    //ray is vertical--射线是垂直的
                    if (p1.y == p2.y) {
                        //overlies on a vertical ray--覆盖在垂直光线上
                        if (p1.y == p.y) {
                            return boundOrVertex;
                        } else {
                            //before ray--射线之前
                            ++intersectCount;
                        }

                    } else {
                        //cross point on the left side--左边的交叉点
                        double xinters = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y;
                        //cross point of y--y的交叉点
                        if (Math.abs(p.y - xinters) < precision) {
                            //overlies on a ray--覆盖在射线
                            return boundOrVertex;
                        }

                        if (p.y < xinters) {
                            //before ray--射线之前
                            ++intersectCount;
                        }
                    }
                }
            } else {
                //special case when ray is crossing through the vertex--特殊情况下，当射线穿过顶点
                if (p.x == p2.x && p.y <= p2.y) {
                    //p crossing over p2--p交叉p2
                    Point2D.Double p3 = polygon.get((i + 1) % N);
                    //next vertex--下一个顶点
                    if (p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(p1.x, p3.x)) {
                        //p.x lies between p1.x & p3.x--p.x在p1.x和p3.x之间
                        ++intersectCount;
                    } else {
                        intersectCount += 2;
                    }
                }
            }
            //next ray left point--下一条射线的左边点
            p1 = p2;
        }
        //偶数在多边形外
        if (intersectCount % 2 == 0) {
            return false;
        } else {
            //奇数在多边形内
            return true;
        }
    }

    /**
     *  Base64编码
     */
    public static String encodeToString(String str) {
        return Base64Utils.encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     */
    public static String decodeFromString(String str) {
        byte[] bytes = Base64Utils.decodeFromString(str);
        return new String(bytes);
    }

    public static String utf8FromIso88591(String str) {
        return new String(str.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);
    }

    /**
     * 切分字符串为列表
     */
    public static List<String> splitListFromString(String string) {
        return Arrays.asList(string.split(","));
    }

    /**
     * 切分字符串Iso88591格式为列表
     */
    public static List<String> splitListFromIso88591(String string) {
        return Arrays.asList(utf8FromIso88591(string).split(","));
    }

    /**
     * Description: 判断对象是否为空集合或者空字符串、null
     */
    public static Boolean isEmpty(Object object) {
        if (object != null) {
            if (object instanceof Map) {
                return ((Map<?, ?>) object).isEmpty();
            } else if (object instanceof List) {
                return ((List<?>) object).isEmpty();
            } else if (object instanceof String) {
                return ((String) object).trim().isEmpty();
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static <T extends CharSequence> boolean isBlank(T str) {
        return str == null || ((String) str).trim().isEmpty();
    }

    public static <T extends CharSequence> T defaultIfBlank(T str, T defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    public static <T extends CharSequence> T defaultIfEmpty(T str, T defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    public static String join(List<String> strings, String separator) {
        Iterator<String> iterator = strings.iterator();
        if (!iterator.hasNext()) {
            return "";
        } else {
            Object first = iterator.next();
            if (!iterator.hasNext()) {
                return Objects.toString(first, "");
            } else {
                StringBuilder buf = new StringBuilder(256);
                if (first != null) {
                    buf.append(first);
                }
                while (iterator.hasNext()) {
                    if (separator != null) {
                        buf.append(separator);
                    }
                    Object obj = iterator.next();
                    if (obj != null) {
                        buf.append(obj);
                    }
                }
                return buf.toString();
            }
        }
    }

    public static String substringBeforeLast(String str, String separator) {
        if (!isEmpty(str) && !isEmpty(separator)) {
            int pos = str.lastIndexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }

}
