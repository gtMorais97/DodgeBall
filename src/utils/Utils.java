package utils;

import java.awt.Point;
import java.util.List;

public class Utils {

    public static Point copyPoint(Point p){
        return new Point(p.x, p.y);
    }
    
    public static <T> List<T> cast(List list) {
        return list;
    }
}
