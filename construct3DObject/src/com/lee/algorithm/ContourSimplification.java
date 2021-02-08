package com.lee.algorithm;

import com.lee.entity.LineDistortionError;
import com.lee.entity.LineSegment;
import com.lee.entity.Point;
import com.lee.entity.Square;
import com.lee.util.MathUtil;
import com.lee.util.StepUtil;
import org.ujmp.core.Matrix;

import java.util.*;

/** 轮廓简化 */
public class ContourSimplification {

    /**
     * 使用D-P方法创建区域
     *
     * @param contours
     * @return
     */
    public static List<List<LineSegment>> createRegionOfDP(List<List<LineSegment>> contours) throws CloneNotSupportedException {
        List<List<LineSegment>> res = new ArrayList<>();

        Map<String,List<Integer>> map = new HashMap<>();
        map.put("start",new ArrayList<>());
        map.put("end",new ArrayList<>());
        for (List<LineSegment> contour : contours) {
            // 获取轮廓的顺序边
            Point[] points = new Point[contour.size()];
            extractPointsFromAContour(contour).toArray(points);
            // D-P方法划分区域
            createRegionOfDP2(points,0,points.length-1,map);
            // 将region加入结果中
            for (int i = 0; i < map.get("start").size(); i++){
                List<LineSegment> region = new ArrayList<>();
                for (int st = map.get("start").get(i); st < map.get("end").get(i); st++){
                    region.add(contour.get(st));
                }
                res.add(region);
            }
            // 将最后一条轮廓线段加入region中，否则轮廓有缺口
            List<LineSegment> last = new ArrayList<>();
            last.add(contour.get(contour.size()-1));
            res.add(last);
            // 清空map
            map.get("start").clear();
            map.get("end").clear();
        }

        return res;
    }

    private static void createRegionOfDP2(Point[] points,int start,int end,Map<String,List<Integer>> res){
//        System.out.println("start="+start+",end="+end);
        if (end - start == 1){
            res.get("start").add(start);
            res.get("end").add(end);
            return;
        }
        Point startPoint = points[start];
        Point endPoint = points[end];
        // 求直线方程
        double[] line = MathUtil.getLineFromTwoPoints(startPoint, endPoint);
        // 获取最大距离以及对应点
        double maxDistance = -1.0;
        int mid = -1;
        for (int i = start; i <= end; i++){
            double distance = MathUtil.distanceBetweenPointAndLine(line, points[i]);
            if (Double.compare(distance,maxDistance) >= 0){
                maxDistance = distance;
                mid = i;
            }
        }
        // 验证是否可以简化
        if (Double.compare(maxDistance,0.125) >= 0){
            createRegionOfDP2(points,start,mid,res);
            createRegionOfDP2(points,mid,end,res);
        }else {
            res.get("start").add(start);
            res.get("end").add(end);
        }
    }

    /**
     * 轮廓简化之获取区域
     *
     * @param contours
     * @return Region就是一个List<LineSegment>
     */
    public static List<List<LineSegment>> createRegion(List<List<LineSegment>> contours) {
        int alpha = 5; // 每个闭环轮廓的几分之一成为种子边
        List<List<LineSegment>> res = new ArrayList<>();

        for (List<LineSegment> contour : contours) {
            // 对于每个闭环轮廓，有多个region
            // 1. 确立种子边，随机确定
            List<List<LineSegment>> regions = new ArrayList<>();
            boolean[] isvisited = new boolean[contour.size()];
            Arrays.fill(isvisited, false);
            int numOfSeedEdge = contour.size() / alpha; // 种子边数目
            if (numOfSeedEdge <= 1) {
                numOfSeedEdge = contour.size();
            }

            for (int i = 0; i < numOfSeedEdge; ) {
                int seedEdgeIndex = new Random().nextInt(contour.size());
                if (!isvisited[seedEdgeIndex]) {
                    i++;
                    LineSegment seedEdge = contour.get(seedEdgeIndex);
                    isvisited[seedEdgeIndex] = true;
                    List<LineSegment> region = new ArrayList<>();
                    region.add(seedEdge);
                    regions.add(region);
                }
            }
            //            System.out.println("__——___种子边是哪些");
            //            System.out.println(Arrays.toString(isvisited));

            // 2. 确立代理边，初始化为种子边，代理边为直线
            List<double[]> proxies = new ArrayList<>(regions.size());
            for (int i = 0; i < regions.size(); i++) {
                double[] proxy =
                        MathUtil.getLineFromTwoPoints(
                                regions.get(i).get(0).getStartPoint(),
                                regions.get(i).get(0).getEndPoint());
                proxies.add(proxy);
            }

            for (int it = 0; it < 21; it++) {
                //                System.out.println("轮廓简化第"+(it+1)+"次。");
                // 3. 将每个种子边的邻边加入最小队列中
                PriorityQueue<LineDistortionError> minimalQueue =
                        new PriorityQueue<>(
                                new Comparator<LineDistortionError>() {
                                    @Override
                                    public int compare(
                                            LineDistortionError o1, LineDistortionError o2) {
                                        return Double.compare(
                                                o1.getDistortion(), o2.getDistortion());
                                    }
                                });
                for (int i = 0; i < regions.size(); i++) {
                    // 种子边的索引
                    int indexOfSeedEdge = contour.indexOf(regions.get(i).get(0));
                    // 邻边的索引
                    int last = StepUtil.preIndex(contour, indexOfSeedEdge);
                    int next = StepUtil.nextIndex(contour, indexOfSeedEdge);
                    // 获取两条邻边及相应的代理边
                    LineSegment lastLineSegment = contour.get(last);
                    LineSegment nextLineSegment = contour.get(next);
                    double[] proxy = proxies.get(i);
                    // 计算邻边与代理的形变误差并加入到最小队列中
                    LineDistortionError lastLine = new LineDistortionError(lastLineSegment, proxy);
                    lastLine.setDistortion(distortionErrorBetweenTwoLines(lastLineSegment, proxy));
                    minimalQueue.add(lastLine);
                    LineDistortionError nextLine = new LineDistortionError(nextLineSegment, proxy);
                    nextLine.setDistortion(distortionErrorBetweenTwoLines(nextLineSegment, proxy));
                    minimalQueue.add(nextLine);
                }

                // 4.遍历直到队列为空
                while (!minimalQueue.isEmpty()) {
                    LineDistortionError minDistortionLine = minimalQueue.poll();
                    int indexInContour = contour.indexOf(minDistortionLine.getLine());
                    if (!isvisited[indexInContour]) {
                        double[] proxy = minDistortionLine.getProxy();
                        int index = proxies.indexOf(proxy);
                        regions.get(index).add(minDistortionLine.getLine());
                        isvisited[indexInContour] = true;
                        // 将才出队列的边的邻边加入队列
                        int lastIndex = StepUtil.preIndex(contour, indexInContour);
                        int nextIndex = StepUtil.nextIndex(contour, indexInContour);
                        if (!isvisited[lastIndex]) {
                            LineDistortionError lastIndexLine =
                                    new LineDistortionError(contour.get(lastIndex), proxy);
                            lastIndexLine.setDistortion(
                                    distortionErrorBetweenTwoLines(contour.get(lastIndex), proxy));
                            minimalQueue.add(lastIndexLine);
                        }

                        if (!isvisited[nextIndex]) {
                            LineDistortionError nextIndexLine =
                                    new LineDistortionError(contour.get(nextIndex), proxy);
                            nextIndexLine.setDistortion(
                                    distortionErrorBetweenTwoLines(contour.get(nextIndex), proxy));
                            minimalQueue.add(nextIndexLine);
                        }
                    }
                }

                if (it == 20) break;

                // 5. 更新代理边
                for (List<LineSegment> region : regions) {
                    // 区域重心
                    Point xi = StepUtil.computeBarycenter(region);
                    // 计算区域的协方差矩阵
                    Matrix Mi = StepUtil.computeCovarianceMatrix(region, xi);
                    // 协方差矩阵的最小特征值所对应的特征向量，大小为 1x2
                    Matrix eigVector = MathUtil.minEigvector(Mi);
                    // 计算新的代理边
                    double[] newProxy = MathUtil.getLineFromAPointAndNormal(xi, eigVector);
                    // 更新
                    proxies.set(regions.indexOf(region), newProxy);
                }

                // 6. 再次寻找每个区域中的种子边
                List<List<LineSegment>> newRegions = new ArrayList<>();
                Arrays.fill(isvisited, false);
                for (int i = 0; i < regions.size(); i++) {
                    List<LineSegment> region = regions.get(i);
                    List<LineSegment> newRegion = new ArrayList<>();
                    PriorityQueue<LineDistortionError> lineDistortionErrors =
                            new PriorityQueue<>(
                                    new Comparator<LineDistortionError>() {
                                        @Override
                                        public int compare(
                                                LineDistortionError o1, LineDistortionError o2) {
                                            return Double.compare(
                                                    o1.getDistortion(), o2.getDistortion());
                                        }
                                    });
                    for (LineSegment lineSegment : region) {
                        LineDistortionError lineDistortionError =
                                new LineDistortionError(lineSegment, proxies.get(i));
                        lineDistortionError.setDistortion(
                                distortionErrorBetweenTwoLines(lineSegment, proxies.get(i)));
                        lineDistortionErrors.add(lineDistortionError);
                    }
                    LineSegment line = lineDistortionErrors.poll().getLine();
                    newRegion.add(line);
                    isvisited[contour.indexOf(line)] = true;
                    newRegions.add(newRegion);
                }
                regions = newRegions;
            }
            res.addAll(regions);
        }

        return res;
    }

    /**
     * 轮廓简化之验证
     *
     * @param regions
     * @return
     */
    public static List<LineSegment> verification(
            List<List<LineSegment>> regions, double threshold) {
        List<LineSegment> result = new ArrayList<>();
        for (List<LineSegment> region : regions) {
//            System.out.println(regions.indexOf(region));
            //            System.out.println("******************region***************");
            // 获取顺序点
//            System.out.println("获取顺序点");
            List<Point> points = extractPointsFromARegion(region);

            // 获取顺序边
//            System.out.println("获取顺序边");
            LineSegment[] lineSegments = new LineSegment[points.size() - 1];
            extractOrderedLinesFromARegion(region, points).toArray(lineSegments);

            Point[] startEnd = new Point[2];
            startEnd[0] = points.get(0);
            startEnd[1] = points.get(points.size() - 1);
            List<LineSegment> verification =
                    verification(
                            points, startEnd, lineSegments, 0, lineSegments.length - 1, threshold);
            result.addAll(verification);
        }

        // 去重
        Set<LineSegment> set = new HashSet<>();
        for (LineSegment lineSegment : result) {
            if (!set.contains(lineSegment)) {
                set.add(lineSegment);
            }
        }

        result.clear();
        //
        for (LineSegment lineSegment : set) {
            if (!lineSegment.getStartPoint().equals(lineSegment.getEndPoint())) {
                lineSegment.setNext(null);
                result.add(lineSegment);
            }
        }

        return result;
    }

    private static List<LineSegment> verification(
            List<Point> points,
            Point[] startEnd,
            LineSegment[] lineSegments,
            int begin,
            int end,
            double threshold) {
//                System.out.println("起始点"+Arrays.toString(startEnd));
//                System.out.println("begin="+begin+",end="+end);
        List<LineSegment> res = new ArrayList<>();
        if (end - begin <= 1) {
            LineSegment lineSegment = new LineSegment(startEnd[0], startEnd[1]);
            res.add(lineSegment);
        } else {
            double[] line = MathUtil.getLineFromTwoPoints(startEnd[0], startEnd[1]);
            // test01 是否相交
            for (int i = begin + 1; i <= end; i++) {
//                                System.out.println("第"+i+"条边开始检查。");
                Point first = lineSegments[i].getStartPoint();
                LineSegment edge1 = getLineSegmentHoldingPoint(first, lineSegments[i].getSquare());
//                                System.out.println("first点="+first);
//                                System.out.println("first点所在的边="+edge1);

                if (!MathUtil.isCrossALine(line, edge1)) {
                    // 递归
                    int begin1 = begin;
                    int end1 = begin1;
                    int begin2 = begin1;
                    int end2 = end;
                    if (points.get(i).equals(first)) {
                        end1 = i - 1;
                        begin2 = i;
                    } else {
                        end1 = i;
                        begin2 = i + 1;
                    }
                    Point[] newPoints1 = new Point[2];
                    newPoints1[0] = startEnd[0];
                    newPoints1[1] = first;
                    List<LineSegment> one =
                            verification(points, newPoints1, lineSegments, begin1, end1, threshold);
                    Point[] newPoints2 = new Point[2];
                    newPoints2[0] = first;
                    newPoints2[1] = startEnd[1];
                    List<LineSegment> two =
                            verification(points, newPoints2, lineSegments, begin2, end2, threshold);
                    res.addAll(one);
                    res.addAll(two);
                    return res;
                }
                Point second = lineSegments[i].getEndPoint();
                LineSegment edge2 = getLineSegmentHoldingPoint(second, lineSegments[i].getSquare());
                if (!MathUtil.isCrossALine(line, edge2)) {
                    // 递归
                    int begin1 = begin;
                    int end1 = begin1;
                    int begin2 = begin1;
                    int end2 = end;
                    if (points.get(i).equals(first)) {
                        end1 = i - 1;
                        begin2 = i;
                    } else {
                        end1 = i;
                        begin2 = i + 1;
                    }
                    Point[] newPoints1 = new Point[2];
                    newPoints1[0] = startEnd[0];
                    newPoints1[1] = second;
                    List<LineSegment> one =
                            verification(points, newPoints1, lineSegments, begin1, end1, threshold);
                    Point[] newPoints2 = new Point[2];
                    newPoints2[0] = second;
                    newPoints2[1] = startEnd[1];
                    List<LineSegment> two =
                            verification(points, newPoints2, lineSegments, begin2, end2, threshold);
                    res.addAll(one);
                    res.addAll(two);
                    return res;
                }
            }
            // 误差是否足够小
            double totalDistortion = 0.0;
            for (LineSegment lineSegment : lineSegments) {
                totalDistortion += distortionErrorBetweenTwoLines(lineSegment, line);
            }
//                        System.out.println("误差: "+totalDistortion);
            if (Double.compare(totalDistortion, threshold) > 0) {
                //                System.out.println("误差"+totalDistortion);
                // 误差不够小，继续递归
                // 从中间端开
                int middle = (begin + end) / 2;
                LineSegment middleLineSegment = lineSegments[middle];
                Point first = middleLineSegment.getStartPoint();
                int begin1 = begin;
                int end1 = begin1;
                int begin2 = begin1;
                int end2 = end;
                if (points.get(middle).equals(first)) {
                    end1 = middle - 1;
                    begin2 = middle;
                } else {
                    end1 = middle;
                    begin2 = middle + 1;
                }
                Point[] newPoints1 = new Point[2];
                newPoints1[0] = startEnd[0];
                newPoints1[1] = first;
                List<LineSegment> one =
                        verification(points, newPoints1, lineSegments, begin1, end1, threshold);
                Point[] newPoints2 = new Point[2];
                newPoints2[0] = first;
                ;
                newPoints2[1] = startEnd[1];
                List<LineSegment> two =
                        verification(points, newPoints2, lineSegments, begin2, end2, threshold);
                res.addAll(one);
                res.addAll(two);
                return res;
            }
        }
        LineSegment lineSegment = new LineSegment(startEnd[0], startEnd[1]);
        res.add(lineSegment);
        return res;
    }

    /**
     * 返回point在正方形哪条边上
     *
     * @param point
     * @param square
     * @return
     */
    private static LineSegment getLineSegmentHoldingPoint(Point point, Square square) {
        double x1 = square.getUpleft().getX();
        double y1 = square.getUpleft().getY();
        double x2 = square.getLowright().getX();
        double y2 = square.getLowright().getY();

        if (Double.compare(point.getX(), x1) == 0) {
            return new LineSegment(new Point(x1, y1), new Point(x1, y2));
        } else if (Double.compare(point.getX(), x1) > 0 && Double.compare(point.getX(), x2) < 0) {
            if (Double.compare(point.getY(), y1) == 0) {
                return new LineSegment(new Point(x1, y1), new Point(x2, y1));
            }
            if (Double.compare(point.getY(), y2) == 0) {
                return new LineSegment(new Point(x1, y2), new Point(x2, y2));
            }
        }
        return new LineSegment(new Point(x2, y1), new Point(x2, y2));
    }

    /**
     * 从region中获取有序的边
     *
     * @param region
     * @param points
     * @return
     */
    private static List<LineSegment> extractOrderedLinesFromARegion(
            List<LineSegment> region, List<Point> points) {
//        System.out.println(region);
//        System.out.println(points);
        List<LineSegment> lines = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point start = points.get(i);
            Point end = points.get(i + 1);
            for (LineSegment lineSegment : region) {
                if (lineSegment.isEndPoint(start) && lineSegment.isEndPoint(end)) {
                    lines.add(lineSegment);
                }
            }
        }
        return lines;
    }

    /**
     * 获取一个region顺序排列的点
     *
     * @param region
     * @return
     */
    public static List<Point> extractPointsFromARegion(List<LineSegment> region) {
        Set<Point> points = new HashSet<>();
        for (LineSegment lineSegment : region) {
            points.add(lineSegment.getStartPoint());
            points.add(lineSegment.getEndPoint());
        }
        // 查找源头点
        //        System.out.println("查找起始点");
        Point start = null;
        LineSegment startLine = null;
        for (Point point : points) {
            List<LineSegment> startLineProbably = new ArrayList<>();
            for (LineSegment lineSegment : region) {
                if (lineSegment.getStartPoint().equals(point)
                        || lineSegment.getEndPoint().equals(point)) {
                    startLineProbably.add(lineSegment);
                }
            }
            if (startLineProbably.size() == 1) {
                start = point;
                startLine = startLineProbably.get(0);
                break;
            }
        }
        // 成环的region,随便取一个起始点
        boolean isCircle = false;
        if (start == null) {
            start = region.get(0).getStartPoint();
            startLine = region.get(0);
            isCircle = true;
        }
//                System.out.println(isCircle);
//                System.out.println("region"+region);
//                System.out.println("start"+start);
//                System.out.println("startLine"+startLine);
        // 点有序集合
//                System.out.println("点有序");
        List<Point> orderedPoints = new ArrayList<>();
        orderedPoints.add(start);
        Point next =
                startLine.getStartPoint().equals(start)
                        ? startLine.getEndPoint()
                        : startLine.getStartPoint();

        while (orderedPoints.size() != region.size()) {
//            System.out.println(orderedPoints.size()+"  "+region.size());
            for (LineSegment lineSegment : region) {
                if (orderedPoints.contains(lineSegment.getStartPoint())
                        || orderedPoints.contains(lineSegment.getEndPoint())) {
                    continue;
                } else {
                    if (lineSegment.getStartPoint().equals(next)) {
                        orderedPoints.add(next);
                        next = lineSegment.getEndPoint();
                        break;
                    }
                    if (lineSegment.getEndPoint().equals(next)) {
                        orderedPoints.add(next);
                        next = lineSegment.getStartPoint();
                        break;
                    }
                }
            }
        }
        if (!isCircle) {
            orderedPoints.add(next);
        }
        return orderedPoints;
    }

    /**
     * 从轮廓中提取顺序点
     * @param contour
     * @return
     */
    private static List<Point> extractPointsFromAContour(List<LineSegment> contour) {
        List<Point> points = new ArrayList<>();
        Point start = null;
        Point middle = null;
        Point startPoint1 = contour.get(0).getStartPoint();
        Point endPoint1 = contour.get(0).getEndPoint();
        Point startPoint2 = contour.get(1).getStartPoint();
        Point endPoint2 = contour.get(1).getEndPoint();
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
            if (contour.get(i).getStartPoint().equals(top)){
                points.add(contour.get(i).getEndPoint());
            }else {
                points.add(contour.get(i).getStartPoint());
            }
        }
        return points;
    }

    /**
     * 计算两条线段的形变误差
     *
     * @param line
     * @param proxy
     * @return
     */
    private static double distortionErrorBetweenTwoLines(LineSegment line, double[] proxy) {
        double d0 = MathUtil.distanceBetweenPointAndLine(proxy, line.getStartPoint());
        double d1 = MathUtil.distanceBetweenPointAndLine(proxy, line.getEndPoint());
        double length = MathUtil.lengthOfSegment(line);

        return (d0 * d0 + d1 * d1 + d0 * d1) * length / 3.0;
    }
}
