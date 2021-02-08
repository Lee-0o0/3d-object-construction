package com.lee.paint;

import com.lee.algorithm.ContourSimplification;
import com.lee.entity.LineSegment;
import com.lee.entity.Point;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.List;


public class DrawingPaint extends JPanel {

    private List<LineSegment> lineSegments;
    private List<LineSegment> smoothLineSegments;
    private List<List<LineSegment>> regions;
    private List<LineSegment> verificatedLineSegments;
    private double minX = 1000000;
    private double maxX = -100000;
    private double minY = 1000000;
    private double maxY = -100000;

    public DrawingPaint(List<LineSegment> lineSegments, List<LineSegment> smoothLineSegments, List<List<LineSegment>> regions, List<LineSegment> verificatedLineSegments) {
//        setPreferredSize(new Dimension(982,600));

        setBackground(Color.WHITE);
        this.lineSegments = lineSegments;
        this.smoothLineSegments = smoothLineSegments;
        this.regions = regions;
        this.verificatedLineSegments = verificatedLineSegments;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int scale = getScale();
        double moveX = minX*scale;
        double moveY = minY*scale;

        // 黑色Marching Squares产生的轮廓
        g.setColor(Color.BLACK);
        for (LineSegment lineSegment : lineSegments) {
            Point startPoint = lineSegment.getStartPoint();
            Point endPoint = lineSegment.getEndPoint();
            g.drawLine((int) (startPoint.getX() * scale-moveX), (int) (startPoint.getY() * scale-moveY), (int) (endPoint.getX() * scale-moveX), (int) (endPoint.getY() * scale-moveY));

        }
        // 蓝色 平滑后的轮廓
        g.setColor(Color.BLUE);
        for (LineSegment lineSegment : smoothLineSegments) {
            Point startPoint = lineSegment.getStartPoint();
            Point endPoint = lineSegment.getEndPoint();
            g.drawLine((int) (startPoint.getX() * scale-moveX), (int) (startPoint.getY() * scale-moveY), (int) (endPoint.getX() * scale-moveX), (int) (endPoint.getY() * scale-moveY));
        }
        // 绿色 简化后未经验证的轮廓
        g.setColor(Color.GREEN);
        for (List<LineSegment> region:regions){
            List<Point> points = ContourSimplification.extractPointsFromARegion(region);
            Point startPoint = points.get(0);
            Point endPoint = points.get(points.size() - 1);
            g.drawLine((int) (startPoint.getX() * scale-moveX), (int) (startPoint.getY() * scale-moveY), (int) (endPoint.getX() * scale-moveX), (int) (endPoint.getY() * scale-moveY));
        }
        // 红色： 简化后验证后的轮廓
        g.setColor(Color.RED);
        for (LineSegment lineSegment : verificatedLineSegments) {
            Point startPoint = lineSegment.getStartPoint();
            Point endPoint = lineSegment.getEndPoint();
            g.drawLine((int) (startPoint.getX() * scale-moveX), (int) (startPoint.getY() * scale-moveY), (int) (endPoint.getX() * scale-moveX), (int) (endPoint.getY() * scale-moveY));
        }
    }

    /**
     * 自动获取缩放倍数
     * @return
     */
    private int getScale(){
        int width = getWidth();
        int height = getHeight();
//        System.out.println(width + "  " + height);

        for (LineSegment lineSegment : lineSegments) {
            Point startPoint = lineSegment.getStartPoint();
            Point endPoint = lineSegment.getEndPoint();
            if (Double.compare(startPoint.getX(), minX) < 0) {
                minX = startPoint.getX();
            }
            if (Double.compare(startPoint.getX(), maxX) > 0) {
                maxX = startPoint.getX();
            }
            if (Double.compare(startPoint.getY(), minY) < 0) {
                minY = startPoint.getY();
            }
            if (Double.compare(startPoint.getY(), maxY) > 0) {
                maxY = startPoint.getY();
            }
            if (Double.compare(endPoint.getX(), minX) < 0) {
                minX = endPoint.getX();
            }
            if (Double.compare(endPoint.getX(), maxX) > 0) {
                maxX = endPoint.getX();
            }
            if (Double.compare(endPoint.getY(), minY) < 0) {
                minY = endPoint.getY();
            }
            if (Double.compare(endPoint.getY(), maxY) > 0) {
                maxY = endPoint.getY();
            }
        }
        minX -= 1.0;maxX += 1.0;minY -= 1.0;maxY += 1.0;
//        System.out.println(minX+" "+maxX+" "+minY+" "+maxY);
        int scale = (int)(width/(maxX-minX))<(int)(height/(maxY-minY))?(int)(width/(maxX-minX)):(int)(height/(maxY-minY));
        return scale;
    }
}
