package algorithm;

import entity.LineSegment;
import entity.Point;

import java.util.ArrayList;
import java.util.List;

public class OrderingPoints {
    /**
     * 提取一个轮廓的有序点
     * @param contour
     * @return
     */
    public static List<Point> orderPoints(List<LineSegment> contour){
        List<Point> points = new ArrayList<>();
        Point start = null;
        Point middle = null;
        Point startPoint1 = contour.get(0).getFirst();
        Point endPoint1 = contour.get(0).getSecond();
        Point startPoint2 = contour.get(1).getFirst();
        Point endPoint2 = contour.get(1).getSecond();
        if (startPoint1.equals(startPoint2) || startPoint1.equals(endPoint2)){
            middle = startPoint1;
            start = endPoint1;
        }
        if (endPoint1.equals(startPoint2) || endPoint1.equals(endPoint2)){
            middle = endPoint1;
            start = startPoint1;
        }

        points.add(start);
        points.add(middle);
        for (int i = 1; i < contour.size()-1; i++){
            Point top = points.get(points.size() - 1);
            if (contour.get(i).getFirst().equals(top)){
                points.add(contour.get(i).getSecond());
            }else {
                points.add(contour.get(i).getFirst());
            }
        }
        return points;
    }
}
