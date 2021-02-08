package test;

import entity.LineSegment;
import entity.Point;
import sun.nio.cs.ext.MacSymbol;

/**
 * 测试线段端点
 */
public class Test01 {
    public static void main(String[] args) {
//        2：找到了Point{x=122.8, y=64.1}  Point{x=179.2, y=118.2}
//        2：*******Point{x=122.8, y=64.1}--Point{x=179.2, y=118.2}*******
//        1：Point{x=66.4, y=118.2}  Point{x=122.8, y=64.1}    false
//        false   2：Point{x=122.8, y=64.1}  Point{x=179.2, y=118.2}    true
//        false   3：Point{x=292.0, y=118.2}  Point{x=348.4, y=64.1}    true
//        true   true
//        3：找到了Point{x=292.0, y=118.2}  Point{x=348.4, y=64.1}

        Point point1 = new Point(235.6,64.1);
        Point point2 = new Point(292.0,118.2);
        LineSegment lineSegment = new LineSegment(point1,point2);
        System.out.println("linesegment");
        System.out.println(lineSegment.getFirst()+"  "+lineSegment.getSecond());

        Point point3 = new Point(66.4,118.2);
        Point point4 = new Point(122.8,64.1);
        LineSegment target = new LineSegment(point3,point4);
        System.out.println("target");
        System.out.println(target.getFirst()+"  "+target.getSecond());

        boolean flag1 = lineSegment.isEndPoint(target.getFirst());
        boolean flag2 = lineSegment.isEndPoint(target.getSecond());
        System.out.println(flag1);
        System.out.println(flag2);
        System.out.println(lineSegment.isEndPoint(target.getFirst()) || lineSegment.isEndPoint(target.getSecond()));
    }
}
