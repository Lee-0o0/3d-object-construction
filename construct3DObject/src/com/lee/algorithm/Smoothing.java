package com.lee.algorithm;


import com.lee.entity.LineSegment;
import com.lee.entity.Point;
import com.lee.entity.Square;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 平滑算法 */
public class Smoothing {
    /**
     * 平滑算法
     * @param lineSegments 确定了next的轮廓线段
     * @return 平滑后的轮廓线段
     */
    public static List<LineSegment> smoothing(List<LineSegment> lineSegments,double smoothThreshold) {

        if (lineSegments==null||lineSegments.size()==0) return null;
        Square square1 = lineSegments.get(0).getSquare();
        double width = square1.getLowright().getX() - square1.getUpleft().getX();
//        System.out.println(width);
        double threshold = width*lineSegments.size()*smoothThreshold;
        double movingDistance = 10000.0;
        double ratio = 0.4;
        boolean[] isVisited = new boolean[lineSegments.size()];

        List<LineSegment> computedLineSegment = new ArrayList<>();
        for (LineSegment lineSegment:lineSegments){
            LineSegment lineSegment1 = new LineSegment(lineSegment.getStartPoint(),lineSegment.getEndPoint());
            computedLineSegment.add(lineSegment1);
        }

//        int num = 0;
        while (Double.compare(movingDistance, threshold) > 0) {
//            num++;
//            System.out.println("------------"+num+"------------");
//            System.out.println("movingDistance"+movingDistance+"\t threshold"+threshold);
            // 一次迭代过程
            Arrays.fill(isVisited, false);
            movingDistance = 0.0;
            for (int i = 0; i < lineSegments.size(); i++) {
                if (!isVisited[i]) {
                    LineSegment start = lineSegments.get(i);
                    LineSegment last = start;
                    LineSegment next = start.getNext();
                    // 计算一个闭环的移动距离
                    while (next != start && !isVisited[lineSegments.indexOf(last)]) {
//                        System.out.println(Arrays.toString(isVisited));
                        int indexOfLast = lineSegments.indexOf(last);
                        int indexOfNext = lineSegments.indexOf(next);
//                        System.out.println(indexOfLast+"**"+indexOfNext);
//                        System.out.println("last："+last.getFirst()+"--"+last.getSecond());
//                        System.out.println("next："+next.getFirst()+"--"+next.getSecond());
                        // 计算三个点的顺序,points[1]是两条线的交点
                        Point[] points = new Point[3];
                        if (last.getStartPoint().equals(next.getStartPoint())) {
                            points[0] = last.getEndPoint();
                            points[1] = last.getStartPoint();
                            points[2] = next.getEndPoint();
                        } else if (last.getStartPoint().equals(next.getEndPoint())) {
                            points[0] = last.getEndPoint();
                            points[1] = last.getStartPoint();
                            points[2] = next.getStartPoint();
                        } else if (last.getEndPoint().equals(next.getStartPoint())) {
                            points[0] = last.getStartPoint();
                            points[1] = last.getEndPoint();
                            points[2] = next.getEndPoint();
                        } else if (last.getEndPoint().equals(next.getEndPoint())) {
                            points[0] = last.getStartPoint();
                            points[1] = last.getEndPoint();
                            points[2] = next.getStartPoint();
                        }
//                        System.out.println(Arrays.toString(points));
                        // 计算交点坐标 V^int^
                        // 1. 计算points[0]和points[2]代表的直线方程 ax+by+c=0
                        // coeffients1[0]=a   coeffients1[1]=b  coeffients1[2]=c
                        double[] coeffients1 = new double[3];
                        double x1 = points[0].getX();
                        double y1 = points[0].getY();
                        double x2 = points[2].getX();
                        double y2 = points[2].getY();

                        coeffients1[0] = y2 - y1;
                        coeffients1[1] = x1 - x2;
                        coeffients1[2] = y1 * (x2 - x1) - x1 * (y2 - y1);
                        // 2. 计算points[1]所在的直线
                        Square square = last.getSquare();
                        double[] coeffients2 = getLine(square, points[1]);
                        // 3. 计算两条直线的交点
                        Point intersection = getIntersectedPoint(coeffients1, coeffients2);
                        // 计算移动距离和新的点
                        double movement = 0.0;
                        Point newPoint = new Point(0.0,0.0);
                        if (Double.compare(intersection.getX(),points[1].getX())==0){
                            movement = ratio*(intersection.getY() - points[1].getY());
                            movingDistance += Math.abs(movement);
                            newPoint.setX(points[1].getX());
                            newPoint.setY(points[1].getY()+movement);
                        }else if (Double.compare(intersection.getY(),points[1].getY())==0){
                            movement = ratio*(intersection.getX()-points[1].getX());
                            movingDistance += Math.abs(movement);
                            newPoint.setX(points[1].getX()+movement);
                            newPoint.setY(points[1].getY());
                        }
//                        System.out.println(newPoint);
                        // 更新移动后的点，注意更新两条线上的点
                        if (points[1].equals(last.getStartPoint())){
                            computedLineSegment.get(indexOfLast).setStartPoint(newPoint);
                        }else {
                            computedLineSegment.get(indexOfLast).setEndPoint(newPoint);
                        }
                        if (points[1].equals(next.getStartPoint())) {
                            computedLineSegment.get(indexOfNext).setStartPoint(newPoint);
                        }else {
                            computedLineSegment.get(indexOfNext).setEndPoint(newPoint);
                        }

                        // 移动到下一个
                        isVisited[indexOfLast] = true;
                        last = next;
                        next = next.getNext();
                    }
                }
            }

            // 迭代完成后，将迭代后的结果写回
            for (int i = 0; i <lineSegments.size(); i++) {
                LineSegment lineSegment = lineSegments.get(i);
                lineSegment.setStartPoint(computedLineSegment.get(i).getStartPoint());
                lineSegment.setEndPoint(computedLineSegment.get(i).getEndPoint());
            }
        }

        return lineSegments;
    }

    /**
     * 获取两条直线的交点
     *
     * @param line1
     * @param line2
     * @return
     */
    private static Point getIntersectedPoint(double[] line1, double[] line2) {
        Point point = new Point(0.0, 0.0);
        if (Double.compare(line2[0], 0.0) == 0) {
            point.setX((-line1[2] + line1[1] * line2[2]) / line1[0]);
            point.setY(-line2[2]);
        } else if (Double.compare(line2[1], 0.0) == 0) {
            point.setX(-line2[2]);
            point.setY((-line1[2] + line1[0] * line2[2]) / line1[1]);
        }
        return point;
    }

    /**
     * point 在该正方形的哪条边上
     *
     * @param square
     * @param point 直线方程式
     * @return
     */
    private static double[] getLine(Square square, Point point) {
        double[] res = new double[3];
        double x = point.getX();
        double y = point.getY();

        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        if (Double.compare(x, x1) == 0 && Double.compare(y, y1) > 0 && Double.compare(y, y2) < 0) {
            res[0] = 1;
            res[1] = 0;
            res[2] = -x1;
        } else if (Double.compare(x, x2) == 0
                && Double.compare(y, y1) > 0
                && Double.compare(y, y2) < 0) {
            res[0] = 1;
            res[1] = 0;
            res[2] = -x2;
        } else if (Double.compare(y, y1) == 0
                && Double.compare(x, x1) > 0
                && Double.compare(x, x2) < 0) {
            res[0] = 0;
            res[1] = 1;
            res[2] = -y1;
        } else if (Double.compare(y, y2) == 0
                && Double.compare(x, x1) > 0
                && Double.compare(x, x2) < 0) {
            res[0] = 0;
            res[1] = 1;
            res[2] = -y2;
        }

        return res;
    }
}
