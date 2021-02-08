package com.lee.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *  16种情况
 *  按照 左上--》右上--》左下--》右下 的顺序，编号 xxxx
 *  从0000-->1111共有十六种情况
 *  由于各点状态反转得到的交线不变，只有八种情况
 */
public class Conditions {
    /**
     * 0000情况一 与 1111 情况十六
     * o---o     *---*
     * |   |  或 |   |
     * o---o     *---*
     * @param square 正方形信息
     * @return 没有轮廓线，返回null
     */
    public static List<LineSegment> firstCondition(Square square ) {
        return null;
    }

    /**
     * 情况二 0001 与 情况十五 1110
     * o---o     *---*
     * |   | 或  |   |
     * o---*     *---o
     * @param square
     * @return 有一条轮廓线
     */
    public static List<LineSegment> secondCondition(Square square ){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point((x1+x2)/2,y2);
        Point end = new Point(x2,(y1+y2)/2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);

        result.add(lineSegment);
        return result;
    }

    /**
     * 情况三 0010  与  1101
     * o---o      *---*
     * |   |  或  |   |
     * *---o      o---*
     * @param square
     * @return 有一条轮廓线
     */
    public static List<LineSegment> thirdCondition(Square square){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point(x1,(y1+y2)/2);
        Point end = new Point((x1+x2)/2,y2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);

        result.add(lineSegment);
        return result;
    }

    /**
     * 0011  或   1100
     * o---o    *---*
     * |   | 或 |   |
     * *---*    o---o
     * @param square
     * @return
     */
    public static List<LineSegment> forthCondition(Square square){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point(x1,(y1+y2)/2);
        Point end = new Point(x2,(y1+y2)/2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);

        result.add(lineSegment);
        return result;
    }

    /**
     * 0100  或   1011
     * o---*        *---o
     * |   |   或   |   |
     * o---o        *---*
     * @param square
     * @return
     */
    public static List<LineSegment> fifthCondition(Square square){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point((x1+x2)/2,y1);
        Point end = new Point(x2,(y1+y2)/2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);

        result.add(lineSegment);
        return result;
    }

    /**
     * 0101     或   1010
     * o---*           *---o
     * |   |    或     |   |
     * o---*           *---o
     * @param square
     * @return
     */
    public static List<LineSegment> sixthCondition(Square square ){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point((x1+x2)/2,y1);
        Point end = new Point((x1+x2)/2,y2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);

        result.add(lineSegment);
        return result;
    }

    /**
     * 0110
     * o---*
     * |   |
     * *---o
     * @param square
     * @return
     */
    public static List<LineSegment> seventhCondition(Square square ){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point((x1+x2)/2,y1);
        Point end = new Point(x2,(y1+y2)/2);
        Point start1 = new Point(x1,(y1+y2)/2);
        Point end1 = new Point((x1+x2)/2,y2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);
        result.add(lineSegment);
        LineSegment lineSegment1 = new LineSegment(start1,end1);
        lineSegment1.setSquare(square);
        result.add(lineSegment1);

        return result;
    }

    /**
     * 1001
     * *---o
     * |   |
     * o---*
     * @param square
     * @return
     */
    public static List<LineSegment> eighthCondition(Square square ){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double x2 = square.getLowright().getX();
        double y1 = square.getUpleft().getY();
        double y2 = square.getLowright().getY();

        Point start = new Point(x1,(y1+y2)/2);
        Point end = new Point((x1+x2)/2,y1);
        Point start1 = new Point((x1+x2)/2,y2);
        Point end1 = new Point(x2,(y1+y2)/2);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);
        result.add(lineSegment);
        LineSegment lineSegment1 = new LineSegment(start1,end1);
        lineSegment1.setSquare(square);
        result.add(lineSegment1);

        return result;
    }

    /**
     *   0111    或    1000
     *   o---*         *---o
     *   |   |   或    |   |
     *   *---*         o---o
     */
    public static List<LineSegment> ninthCondition(Square square ){
        List<LineSegment> result = new ArrayList<>();
        double x1 = square.getUpleft().getX();
        double y1 = square.getUpleft().getY();
        double x2 = square.getLowright().getX();
        double y2 = square.getLowright().getY();

        Point start = new Point(x1,(y1+y2)/2);
        Point end = new Point((x1+x2)/2,y1);

        LineSegment lineSegment = new LineSegment(start,end);
        lineSegment.setSquare(square);
        result.add(lineSegment);

        return result;
    }
}
