package algorithm;

import entity.LineSegment;
import entity.Point;
import entity.PointLink;
import entity.Vector;
import util.ArithUtil;
import util.MathUtil;
import util.OffsetUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ScalarContour {
    public static void scalar(List<List<LineSegment>> contours, Graphics g, double distance) {
        // 缩放处理
        for (List<LineSegment> contour : contours) {
            // 获取轮廓顺序点
            List<Point> points = OrderingPoints.orderPoints(contour);

            List<List<Point>> offset = offsetAlgorithm(points, distance);

//            if (offset != null){
//                for (List<Point> offsetContour : offset) {
////                    g.setColor(Color.red);
//                    drawContour(offsetContour,g);
//                }
//            }
        }
    }

    /**
     * 生成缩进轮廓算法
     * @param points 闭合轮廓
     * @param distance 缩进距离
     * @return
     */
    public static List<List<Point>> offsetAlgorithm(List<Point> points, double distance){
        if (points == null || points.size() < 3 || Double.compare(distance,0.0) == 0){
            return null;
        }

        // 将points中的重复点删除，保持顺序
        LinkedHashSet<Point> linkedHashSet1 = new LinkedHashSet<>(points);
        points = new ArrayList<>(linkedHashSet1);
//        System.out.println("------points------");
//        for (Point p : points) {
//            System.out.println(p);
//        }

        // 计算缩进方向
        Map<String, List<Vector>> direction = getDirection(points);
        List<Vector> in = direction.get("in");
        List<Vector> out = direction.get("out");

        // 确定内缩点
        List<Point> inShell = getInfillPoints(points, in, distance);

        // 确定每条缩进线段的有效性
        List<Boolean> validOffsetLine = isValidOffsetLine(points, inShell);
//        System.out.println("validOffsetLine=" + validOffsetLine);

        // 是否访问
        boolean[] isVisited = new boolean[validOffsetLine.size()];
        Arrays.fill(isVisited, false);

        List<Point> finalInshell = new ArrayList<>();
        // 是否所有线段都是无效线段的标志
        boolean flag = false;
        for (int i = 0; i < inShell.size(); ) {
            if (validOffsetLine.get(i)) {
                // 以i点开头的线段是有效边
                finalInshell.add(inShell.get(i));
                isVisited[i] = true;
                i++;
            } else {
                // 以i点开头的线段是无效边
                if (isVisited[i]) {
                    // 访问过了，不再处理
                    continue;
                }
                // 确定case1 或 case2
                int backIndex = (i - 1 + validOffsetLine.size()) % validOffsetLine.size();
                while (!validOffsetLine.get(backIndex)) {
                    backIndex =
                            (backIndex - 1 + validOffsetLine.size()) % validOffsetLine.size();
                    if (backIndex == i) {
//                        System.out.println("backward:all invalid offset lines");
                        flag = true;
                        break;
                    }
                }
                int current = i;
                int next = (i + 1) % validOffsetLine.size();
                while (!validOffsetLine.get(next)) {
                    next = (next + 1) % validOffsetLine.size();
                    if (next == i) {
//                        System.out.println("forward:all invalid offset lines");
                        flag = true;
                        break;
                    }
                }
//                System.out.println("back = " + backIndex + ", i = " + i + ", next = " + next);
                // 根据case 1 和case 2寻找交点
                if (next == backIndex) {
                    // all edges are invalid edges
                    return null;
//                    break;
                } else if (next - backIndex == 2
                        || (next == 1 && backIndex == validOffsetLine.size() - 1)
                        || (next == 0 && backIndex == validOffsetLine.size() - 2)) {
                    // case 1 只有一条无效边
                    // 1. 计算backward edge 和forward edge的交点
                    double[] backwardEdge =
                            MathUtil.getLineFromTwoPoints(
                                    inShell.get(backIndex), inShell.get(current));
                    double[] forwardEdge =
                            MathUtil.getLineFromTwoPoints(
                                    inShell.get(next),
                                    inShell.get((next + 1) % validOffsetLine.size()));

                    Point intersection =
                            MathUtil.getIntersectionOfTwoLines(backwardEdge, forwardEdge);
//                    System.out.println("intersection=" + intersection);
                    // 2. 更新数据
                    if (next == 0) {
                        finalInshell.set(next, intersection);
                    } else {
                        inShell.set(next, intersection);
                    }
                    isVisited[i] = true;
                    i++;
                } else {
                    // case 2: 有连续的无效边
                    LineSegment backwardEdge =
                            new LineSegment(inShell.get(backIndex), inShell.get(current));
                    LineSegment forwardEdge =
                            new LineSegment(
                                    inShell.get(next),
                                    inShell.get((next + 1) % inShell.size()));
                    // 计算backward和forward与轮廓是否有交点

                    for (int j = current; j != next; j = (j + 1) % validOffsetLine.size()) {
                        isVisited[j] = true;
                        LineSegment boundaryEdge =
                                new LineSegment(
                                        points.get(j), points.get((j + 1) % points.size()));
//                        System.out.println("j=" + j + ",j_next=" + (j + 1) % points.size());
//                        System.out.println("boundary edge=" + boundaryEdge);

                        // 计算boundary的pair-wise offset
                        // 1. 偏移方向
                        Vector normalVector = getNormalVector(boundaryEdge);
                        if (Double.compare(MathUtil.getMulOfVector(normalVector, in.get(j)), 0)
                                < 0) {
                            normalVector.mul(-1);
                        }
                        normalVector.normalize();
                        normalVector.mul(3 * distance);
//                        System.out.println("偏移方向=" + normalVector);
                        // 2. 计算偏移线段
                        Point first =
                                new Point(
                                        boundaryEdge.getFirst().getX() + normalVector.getX(),
                                        boundaryEdge.getFirst().getY() + normalVector.getY());
                        Point second =
                                new Point(
                                        boundaryEdge.getSecond().getX() + normalVector.getX(),
                                        boundaryEdge.getSecond().getY() + normalVector.getY());
                        LineSegment offsetEdgeOfPairwise = new LineSegment(first, second);
                        //                            drawLineSegment(offsetEdgeOfPairwise,g);
                        // 计算偏移线段与backward、forward的交点，并更新
                        // 此处应该计算偏移线段offsetEdgeOfPairwise 与所有有效偏移边的交点
                        // 不只是backward 和 forward
                        //
                        // System.out.println("offsetEdge="+offsetEdgeOfPairwise+",
                        // backwardEdge="+backwardEdge);

                        Point point2 = new OffsetUtil().intersection(offsetEdgeOfPairwise, backwardEdge);
//                        Point point2 =
//                                MathUtil.intersectionOfTwoLineSegment(
//                                        offsetEdgeOfPairwise, backwardEdge);
                        //                            System.out.println(point2);
                        if (point2 != null) {
                            backwardEdge.setSecond(point2);
                        }

                        // System.out.println("offsetEdge="+offsetEdgeOfPairwise+",
                        // forwardEdge="+forwardEdge);
                        Point point1 =new OffsetUtil().intersection(offsetEdgeOfPairwise, forwardEdge);
                        //                            System.out.println(point1);
                        if (point1 != null) {
                            forwardEdge.setFirst(point1);
                        }
                    }
                    // 所有的无效边处理完成后
                    // 计算backward 和forward的交点
//                    System.out.println(
//                            "backwardEdge=" + backwardEdge + ",forwardEdge=" + forwardEdge);
                    Point point =new OffsetUtil().intersection(backwardEdge, forwardEdge);
                    Point preForwardStartPoint = forwardEdge.getFirst();
                    if (point != null) {
                        backwardEdge.setSecond(point);
                        forwardEdge.setFirst(point);
                    }

                    finalInshell.add(backwardEdge.getSecond());

                    if (next < current) {
                        i = inShell.size();
                        if (point != null) {
                            for (int k = 0; k < finalInshell.size(); k++){
                                if (finalInshell.get(k).equals(preForwardStartPoint)){
                                    finalInshell.set(k, point);
                                    break;
                                }
                            }
                        }
                    } else {
                        inShell.set(next, forwardEdge.getFirst());
                        i = next;
                    }
                }
            }
        }

        // 所有的边都是无效边，没有缩进轮廓，可以不用进行全局无效环处理
        if (!flag) {
            // 将finalInshell中处于轮廓外的点消除
            List<Point> finalFinalInshell = new ArrayList<>();
            List<LineSegment> contour = new ArrayList<>();
            for (int m = 0; m < points.size(); m++){
                LineSegment lineSegment = new LineSegment(points.get(m), points.get((m + 1) % points.size()));
                contour.add(lineSegment);
            }
            for (Point point: finalInshell){
                if (Double.compare(distance, 0.0) > 0) {
                    // 向内缩进
                    if (MathUtil.isInPolygon(contour, point)) {
                        finalFinalInshell.add(point);
                    }
                }else if (Double.compare(distance,0.0) < 0){
                    // 向外扩张
                    if (!MathUtil.isInPolygon(contour, point)) {
                        finalFinalInshell.add(point);
                    }
                }

            }
            finalInshell = finalFinalInshell;
            // finalInshell 可能有相同的点，去除重复点
            LinkedHashSet<Point> linkedHashSet = new LinkedHashSet<>(finalInshell);
            finalInshell = new ArrayList<>(linkedHashSet);

            System.out.println("----------finalInshell");
            for (Point point : finalInshell) {
                System.out.println(point);
            }

            // 消除全局无效环
            //            System.out.println(finalInshell.size());
            //            System.out.println(finalInshell);
            // 获取所有的交点
//            System.out.println("获取所有的自交点");
            Map<Point, List<Point>> intersection = getIntersection(finalInshell);
            for (Point point : intersection.keySet()) {
                System.out.println("point="+point+" : "+intersection.get(point).size());
                System.out.println(intersection.get(point));
            }
            // 如果没有自交点，说明不存在全局无效环，不用处理
            if (intersection.size() == 0) {
//                System.out.println("没有全局无效环");
                List<List<Point>> res = new ArrayList<>();
                res.add(finalInshell);
                return res;
            } else {
//                System.out.println("有全局无效环");
                // 准备数据
                List<Point> allPoints = new ArrayList<>();
                List<Boolean> isIntersectionPoint = new ArrayList<>();
                for (int i = 0; i < finalInshell.size(); i++) {
                    allPoints.add(finalInshell.get(i));
                    isIntersectionPoint.add(false);
                    if (intersection.get(finalInshell.get(i)) != null) {
                        for (int j = 0; j < intersection.get(finalInshell.get(i)).size(); j++) {
                            allPoints.add(intersection.get(finalInshell.get(i)).get(j));
                            isIntersectionPoint.add(true);
                        }
                    }
                }

                System.out.println("自交点位置");
                for(int i = 0;i < isIntersectionPoint.size(); i++){
                    if (isIntersectionPoint.get(i)){
                        System.out.println(i+"  --  "+allPoints.get(i));
                    }
                }

                System.out.println("allPoints = " + allPoints);
                System.out.println("是否自交点=" + isIntersectionPoint);
                System.out.println(allPoints.size() + "  " + isIntersectionPoint.size());

                List<List<Point>> lists =
                        processGlobalInvalidLoop(points, allPoints, isIntersectionPoint);
                System.out.println("结果：" + lists.size());
                return lists;
            }
        }
        return null;
    }

    /**
     * 消除全局无效环
     *
     * @param contour 原轮廓
     * @param allPoints 包含自交点的缩进轮廓
     * @param isIntersectionPoint 判断allPoints是否为自交点
     * @return
     */
    public static List<List<Point>> processGlobalInvalidLoop(
            List<Point> contour, List<Point> allPoints, List<Boolean> isIntersectionPoint) {
        // 判断原轮廓的方向
        boolean isClockwiseOfContour = isClockwise(contour);
        // 判断该点是否被访问过
        boolean[] isVisited = new boolean[allPoints.size()];
        Arrays.fill(isVisited, false);
        // 任选一个起始点
        Point start = null;
        int indexOfStartPoint = -1;
        for (int i = 0; i < allPoints.size(); i++) {
            if (!isIntersectionPoint.get(i)) {
                start = allPoints.get(i);
                indexOfStartPoint = i;
                break;
            }
        }
        //
        Stack<Point> stack = new Stack<>();
        Stack<Integer> indexOfStartPointStack = new Stack<>();
        stack.push(start);
        indexOfStartPointStack.push(indexOfStartPoint);
//        System.out.println("起始点选取：stack=" + stack);

        List<List<Point>> res = new ArrayList<>();

        while (!stack.isEmpty()) {
            System.out.println("stack=" + stack);
            System.out.println("indexOfStartPointStack = " + indexOfStartPointStack);
            Point startPoint = stack.pop();
            System.out.println("startPoint = "+startPoint);
            int index = indexOfStartPointStack.pop();
            System.out.println("indexOfStartPoint = " + index);
            isVisited[index] = true;

            List<Point> loop = new ArrayList<>();
            loop.add(startPoint);

            Point nextPoint = allPoints.get((index + 1) % allPoints.size());
            int indexOfNextPoint = (index + 1) % allPoints.size();
            while (!nextPoint.equals(startPoint)) {
//                int indexOfNextPoint = allPoints.indexOf(nextPoint);
//                System.out.println("indexOfNextPoint="+indexOfNextPoint);
//                System.out.println(isIntersectionPoint.get(indexOfNextPoint) +"  " + isVisited[indexOfNextPoint]);
//                System.out.println(indexOfNextPoint + " : nextPoint=" + nextPoint + "  startPoint=" + startPoint);
                if (isVisited[indexOfNextPoint]){
                    System.out.println("访问过的点: "+indexOfNextPoint+" "+allPoints.get(indexOfNextPoint)+"  "+isIntersectionPoint.get(indexOfNextPoint));
                    indexOfNextPoint = (indexOfNextPoint+1)%allPoints.size();
                    nextPoint = allPoints.get(indexOfNextPoint);
                }else {
                    if (isIntersectionPoint.get(indexOfNextPoint)) {
//                    System.out.println("是自相交的点");
                        // nextPoint是自相交的点
                        stack.push(nextPoint);
                        indexOfStartPointStack.push(indexOfNextPoint);
                        // 切换到另一个方向
                        for (int k = 0; k < allPoints.size(); k++) {
                            if (nextPoint.equals(allPoints.get(k))) {
                                if (k != indexOfNextPoint) {
//                                System.out.println("k==" + k);
                                    isVisited[k] = true;
                                    loop.add(allPoints.get(k));
                                    nextPoint = allPoints.get((k + 1) % allPoints.size());
                                    indexOfNextPoint = (k + 1) % allPoints.size();
//                                System.out.println("nextPoint="+nextPoint);
                                    break;
                                }
                            }
                        }
                    } else {
                        // nextPoint 不是自相交的点
                        loop.add(nextPoint);
                        isVisited[indexOfNextPoint] = true;
                        nextPoint = allPoints.get((indexOfNextPoint + 1) % allPoints.size());
                        indexOfNextPoint = (indexOfNextPoint + 1) % allPoints.size();
                    }
                }
            }

            if (isClockwise(loop) == isClockwiseOfContour && loop.size() >= 3) {
                res.add(loop);
            }
        }

        return res;
    }

    /**
     * 获取所有的自交点
     *
     * @param points
     * @return
     */
    private static Map<Point, List<Point>> getIntersection(List<Point> points) {
        Map<Point, List<Point>> intersections = new HashMap<>();

        for (int i = 0; i < points.size(); i++) {
            LineSegment lineSegment =
                    new LineSegment(points.get(i), points.get((i + 1) % points.size()));

            for (int j = 0; j < points.size(); j++) {
                if (Math.abs(i-j) > 1) {
                    LineSegment a =
                            new LineSegment(points.get(j), points.get((j + 1) % points.size()));
                    OffsetUtil offsetUtil = new OffsetUtil();
                    Point point =  offsetUtil.intersection(lineSegment, a);

                    if (point != null) {
                        if (!point.equals(lineSegment.getFirst())
                                && !point.equals(lineSegment.getSecond())
                                && !point.equals(a.getFirst())
                                && !point.equals(a.getSecond())) {
//                            System.out.println("linesegment1="+lineSegment);
//                            System.out.println("linesegment2="+a);
//                            System.out.println(i+":"+points.get(i)+"   "+j+":"+points.get(j)+"  point:"+point);

                            if (intersections.get(points.get(i)) != null){
                                intersections.get(points.get(i)).add(point);
                            }else {
                                List<Point> intersection = new ArrayList<>();
                                intersection.add(point);
                                intersections.put(points.get(i), intersection);
                            }

                            if (intersections.get(points.get(j)) != null){
                                intersections.get(points.get(j)).add(point);
                            }else {
                                List<Point> intersection = new ArrayList<>();
                                intersection.add(point);
                                intersections.put(points.get(j), intersection);
                            }
                        }
                    }
                }
            }
        }

        // 去除重复的点
        for (Point point:intersections.keySet()){
            LinkedHashSet<Point> linkedHashSet = new LinkedHashSet<>(intersections.get(point));
            intersections.put(point, new ArrayList<>(linkedHashSet));
        }
        // 自交点排序
        for (Point point : intersections.keySet()) {
            List<Point> pointList = intersections.get(point);
            Collections.sort(
                    pointList,
                    new Comparator<Point>() {
                        @Override
                        public int compare(Point o1, Point o2) {
                            double distance1 = MathUtil.getDistanceBetweenTwoPoints(point, o1);
                            double distance2 = MathUtil.getDistanceBetweenTwoPoints(point, o2);
                            return Double.compare(distance1, distance2);
                        }
                    });
        }

        return intersections;
    }

    private static PointLink toPointLink(List<Point> points) {
        List<PointLink> pointLinks = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            PointLink pointLink = new PointLink();
            pointLink.setPoint(points.get(i));
            pointLink.setIntersection(false);
            pointLinks.add(pointLink);
        }

        for (int i = 0; i < pointLinks.size(); i++) {
            pointLinks.get(i).setNext(pointLinks.get((i + 1) % pointLinks.size()));
        }

        return pointLinks.get(0);
    }

    /**
     * 判断多边形是否为顺时针方向
     * https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
     *
     * @param contour
     * @return true if contour is in clockwise
     */
    private static boolean isClockwise(List<Point> contour) {
        double sum = 0;
        for (int i = 0; i < contour.size(); i++) {
            int next = (i + 1) % contour.size();

            double x1 = contour.get(i).getX();
            double y1 = contour.get(i).getY();
            double x2 = contour.get(next).getX();
            double y2 = contour.get(next).getY();

            sum += (x2 - x1) * (y1 + y2);
        }

        if (Double.compare(sum, 0) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取一条线段的法线方向
     *
     * @param lineSegment
     * @return
     */
    private static Vector getNormalVector(LineSegment lineSegment) {
        double[] line = MathUtil.getLineFromLineSegment(lineSegment);
        Vector vector = new Vector();
        vector.setX(line[0]);
        vector.setY(line[1]);
        return vector;
    }

    /**
     * 确定缩进轮廓线是否有效
     *
     * @param points 原轮廓线中的点
     * @param offset 缩进点
     * @return res.get(i)表示 （i，i+1)线是否有效，true表示有效
     */
    public static List<Boolean> isValidOffsetLine(List<Point> points, List<Point> offset) {
        List<Boolean> res = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            int next = (i + 1) % points.size();
            Vector originalVector =
                    MathUtil.getNormalVectorFromTwoPoints(points.get(i), points.get(next));
            Vector offsetVector =
                    MathUtil.getNormalVectorFromTwoPoints(offset.get(i), offset.get(next));
            double mul = MathUtil.getMulOfVector(originalVector, offsetVector);
            if (Double.compare(mul, 0) == -1) {
                res.add(false);
            } else {
                res.add(true);
            }
        }

        return res;
    }

    /**
     * 获取轮廓的内缩点
     *
     * @param points
     * @param in
     * @param distance
     * @return
     */
    public static List<Point> getInfillPoints(
            List<Point> points, List<Vector> in, double distance) {
        List<Point> inPoints = new ArrayList<>();
//        System.out.println("getInfillPoints---------------");
        for (int i = 0; i < points.size(); i++) {
//            System.out.println(i + " : " + points.get(i));
//            System.out.println(in.get(i));
            inPoints.add(
                    new Point(
                            points.get(i).getX() + in.get(i).getX() * distance,
                            points.get(i).getY() + in.get(i).getY() * distance));
        }
        return inPoints;
    }

    /**
     * 获取轮廓的外扩点
     *
     * @param points 原轮廓点
     * @param out 外扩方向
     * @param distance 外扩距离
     * @return 外扩点
     */
    public static List<Point> getExtendPoints(
            List<Point> points, List<Vector> out, double distance) {
        List<Point> outPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            outPoints.add(
                    new Point(
                            points.get(i).getX() + out.get(i).getX() * distance,
                            points.get(i).getY() + out.get(i).getY() * distance));
        }
        return outPoints;
    }

    /**
     * 确定一个轮廓向内和向外的缩进方向
     *
     * @param points
     * @return map.get("in") 向内缩进的方向，map.get("out") 外扩的方向
     */
    public static Map<String, List<Vector>> getDirection(List<Point> points) {
        Map<String, List<Vector>> map = new HashMap<>();

        List<Vector> in = new ArrayList<>();
        List<Vector> out = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
//            System.out.println("点----------");
//            System.out.println(points.get((i-1+points.size())%points.size())+"  "+points.get(i)+"  "+points.get((i+1)%points.size()));

            Vector vector1;
            Vector vector2;
            if (i == 0) {
                vector1 =
                        MathUtil.getNormalVectorFromTwoPoints(
                                points.get(i), points.get(points.size() - 1));
                vector2 = MathUtil.getNormalVectorFromTwoPoints(points.get(i), points.get(i + 1));
            } else if (i == points.size() - 1) {
                vector1 = MathUtil.getNormalVectorFromTwoPoints(points.get(i), points.get(i - 1));
                vector2 = MathUtil.getNormalVectorFromTwoPoints(points.get(i), points.get(0));
            } else {
                vector1 = MathUtil.getNormalVectorFromTwoPoints(points.get(i), points.get(i - 1));
                vector2 = MathUtil.getNormalVectorFromTwoPoints(points.get(i), points.get(i + 1));
            }
            // 计算缩进方向
            Vector vector = new Vector();
            vector.setX(ArithUtil.add(vector1.getX(), vector2.getX()));
            vector.setY(ArithUtil.add(vector1.getY(), vector2.getY()));

            //            System.out.print("vector1="+vector1);
            //            System.out.println("  vector2="+vector2);
            if (Double.compare(0, vector.getLength()) == 0) {
                if (Double.compare(vector1.getX(), 0) == 0
                        && Double.compare(vector2.getX(), 0) == 0) {
                    vector.setX(1.0);
                    vector.setY(0.0);
                } else if (Double.compare(vector1.getY(), 0) == 0
                        && Double.compare(vector2.getY(), 0) == 0) {
                    vector.setX(0.0);
                    vector.setY(1.0);
                } else {
                    vector.setX(points.get(i).getX());
                    double y =
                            ArithUtil.mul(
                                    vector.getX(),
                                    ArithUtil.div(
                                            -1, ArithUtil.div(vector1.getY(), vector1.getX())));
                    vector.setY(y);
                }
            }
            //            System.out.println("vector="+vector);
            // 计算偏移距离
            double cos =
                    ArithUtil.div(
                            MathUtil.getMulOfVector(vector1, vector2),
                            ArithUtil.mul(vector1.getLength(), vector2.getLength()));
            // 半角公式
            double sin2 = Math.sqrt((1 - cos) / 2);
            double L = 1.0;
            if (Double.compare(0.1, sin2) <= 0) {
                L = 1.0 / sin2;
            }
            vector.normalize();
//            System.out.println("sin2="+sin2);
//            System.out.println("L="+L);
            vector.mul(L);
            //            System.out.println("vectorNormalization="+vector);

            // 结果点
            Point point =
                    new Point(
                            ArithUtil.add(points.get(i).getX(), vector.getX()),
                            ArithUtil.add(points.get(i).getY(), vector.getY()));
            // 判断结果点是在多边形外面还是里面，射线法
//            System.out.println("点及其内缩点" + points.get(i) + "  " + point);

            List<LineSegment> contour = new ArrayList<>();
            for (int m = 0; m < points.size(); m++){
                LineSegment lineSegment = new LineSegment(points.get(m), points.get((m + 1) % points.size()));
                contour.add(lineSegment);
            }

            boolean inPolygon = MathUtil.isInPolygon(contour, point);
//            System.out.println("是否在轮廓内：inPolygon=" + inPolygon);
            Vector pointToIn = null;
            Vector pointToOut = null;
            if (inPolygon) {
                // 在轮廓里，说明vector是指向轮廓里的方向
                pointToIn = vector;
                pointToOut = new Vector();
                pointToOut.setX(ArithUtil.mul(vector.getX(), -1));
                pointToOut.setY(ArithUtil.mul(vector.getY(), -1));
            } else {
                pointToOut = vector;
                pointToIn = new Vector();
                pointToIn.setX(ArithUtil.mul(vector.getX(), -1));
                pointToIn.setY(ArithUtil.mul(vector.getY(), -1));
            }
            //            System.out.println("pointToIn="+pointToIn);
            //            System.out.println("pointToOut="+pointToOut);
            //            pointToIn.normalize();
            //            pointToOut.normalize();

            in.add(pointToIn);
            out.add(pointToOut);
        }

        map.put("in", in);
        map.put("out", out);

        return map;
    }

//    private static void drawLineSegment(LineSegment lineSegment, Graphics g) {
//        double x1 = lineSegment.getFirst().getX();
//        double y1 = lineSegment.getFirst().getY();
//        double x2 = lineSegment.getSecond().getX();
//        double y2 = lineSegment.getSecond().getY();
//        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
//    }

//    public static void drawContour(List<Point> finalInShell1, Graphics g) {
//        if (finalInShell1 == null) return;
//        for (int i = 0; i < finalInShell1.size(); i++) {
//            if (i < finalInShell1.size() - 1) {
//                g.drawLine(
//                        (int) finalInShell1.get(i).getX(),
//                        (int) finalInShell1.get(i).getY(),
//                        (int) finalInShell1.get(i + 1).getX(),
//                        (int) finalInShell1.get(i + 1).getY());
//            } else {
//                g.drawLine(
//                        (int) finalInShell1.get(i).getX(),
//                        (int) finalInShell1.get(i).getY(),
//                        (int) finalInShell1.get(0).getX(),
//                        (int) finalInShell1.get(0).getY());
//            }
//        }
//    }
}
