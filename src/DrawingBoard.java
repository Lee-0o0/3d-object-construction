import algorithm.*;
import entity.*;
import entity.Point;
import entity.Vector;
import file.ReadMAT;
import sun.util.resources.cldr.pa.CalendarData_pa_Arab_PK;
import util.ArithUtil;
import util.MathUtil;
import util.StepUtil;

import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.*;
import java.util.List;

public class DrawingBoard extends JPanel {

    private double[][] data;
    private double threshold;

    public DrawingBoard(double[][] data, double threshold) {
        this.data = data;
        this.threshold = threshold;
    }

    @Override
    public void paint(Graphics g) {
        //        super.paint(g);
        int rows = this.data.length;
        int columns = this.data[0].length;
        int width = getWidth();
        int height = getHeight();
        double verticalGap = (double) (height - 20) / (rows - 1);
        double horizontalGap = (double) (width - 20) / (columns - 1);

        // 画线
        for (int i = 0; i < rows; i++) {
            g.drawLine(10, (int) (10 + verticalGap * i), width - 10, (int) (10 + verticalGap * i));
        }
        for (int i = 0; i < columns; i++) {
            g.drawLine(
                    (int) (10 + horizontalGap * i),
                    10,
                    (int) (10 + horizontalGap * i),
                    height - 10);
        }

        // 画小圆
        int r = (int) (verticalGap / 5);
        for (int i = 0; i < rows; i++) {
            int y = (int) (10 - r / 2 + verticalGap * i);
            for (int j = 0; j < data[i].length; j++) {
                int x = (int) (10 - r / 2 + horizontalGap * j);
                if (Double.compare(data[i][j], threshold) >= 0) {
                    g.fillOval(x, y, r, r);
                } else {
                    g.drawOval(x, y, r, r);
                }
            }
        }

        // 画轮廓
        System.out.println("1. Marching Squares算法");
        List<LineSegment> lineSegments =
                MarchingSquares.marchingSquares(
                        rows, columns, horizontalGap, verticalGap, data, threshold);
        System.out.println("轮廓线数量" + lineSegments.size());
        if (lineSegments.size() == 0) return;

        System.out.println("2. 轮廓线有序算法");
        ExtractPointAlgorithm.extractPoint(lineSegments);

        g.setColor(Color.BLUE);
        boolean[] isvisited = new boolean[lineSegments.size()];
        Arrays.fill(isvisited, false);

        for (int i = 0; i < lineSegments.size(); i++) {
            if (!isvisited[i]) {
                LineSegment start = lineSegments.get(i);
                LineSegment next = start.getNext();
                while (next != start) {
                    isvisited[i] = true;
                    int x1 = (int) next.getFirst().getX();
                    int x2 = (int) next.getSecond().getX();
                    int y1 = (int) next.getFirst().getY();
                    int y2 = (int) next.getSecond().getY();

//                    g.drawLine(x1, y1, x2, y2);
                    next = next.getNext();
                }
            }
        }

        // 平滑算法
        System.out.println("3. 平滑算法");
        List<LineSegment> smoothedLineSegments = Smoothing.smoothing(lineSegments);
        g.setColor(Color.RED);
        for (LineSegment lineSegment : smoothedLineSegments) {
            Point start = lineSegment.getFirst();
            Point end = lineSegment.getSecond();
//            g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
        }

        //        System.out.println("平滑算法后的边"+smoothedLineSegments.get(0));

        System.out.println("4. 提取闭环轮廓算法");
        List<List<LineSegment>> contours = ExtractContours.extractContours(smoothedLineSegments);

        // 轮廓简化之提取区域Region
        System.out.println("5. 提取区域算法");
        List<List<LineSegment>> regions = ContourSimplification.createRegion(contours);

        // 直接连接首尾点
        g.setColor(Color.ORANGE);
        for (List<LineSegment> region : regions) {
            List<Point> points = ContourSimplification.extractPointsFromARegion(region);
            Point start = points.get(0);
            Point end = points.get(points.size() - 1);
//            g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
        }

        // 验证算法
        //        System.out.println("--------------------------------");
        System.out.println("6. 验证算法");
        g.setColor(Color.green);
        List<LineSegment> verificatedLineSegments = ContourSimplification.verification(regions);
        for (LineSegment lineSegment : verificatedLineSegments) {
            Point start = lineSegment.getFirst();
            Point end = lineSegment.getSecond();
            g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
        }

        // 轮廓线有序
        System.out.println("轮廓线有序");
        ExtractPointAlgorithm.extractPoint(verificatedLineSegments);

        // 提取闭环轮廓
        System.out.println("提取闭环轮廓");
        List<List<LineSegment>> contours1 =
                ExtractContours.extractContours(verificatedLineSegments);

        System.out.println("缩放处理");
//        g.setColor(Color.red);
//        ScalarContour.scalar(contours1,g,-10);
//        ScalarContour.scalar(contours1,g,-10);
//        ScalarContour.scalar(contours1,g,-10);
        g.setColor(Color.red);
        ScalarContour.scalar(contours1,g,5);
//        ScalarContour.scalar(contours1,g,10);
//        ScalarContour.scalar(contours1,g,-10);

        System.out.println("蓝色：Marching Squares原始轮廓");
        System.out.println("红色：平滑后的轮廓");
        System.out.println("黄色：轮廓简化后未经验证的轮廓");
        System.out.println("绿色：轮廓简化、验证后的轮廓");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Marching Squares Algorithm");
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // 读取数据
        int size = 60;
        double[][] allData = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == 0 || j == 0 || i == size - 1 || j == size - 1) {
                    allData[i][j] = 0;
                } else {
                    allData[i][j] = new Random().nextDouble();
                }
            }
        }

//        allData[5][5]=1.0;
//        allData[5][4]=1.0;

//        allData[4][1] = 1.0;
//        allData[4][2] = 1.0;
//        allData[4][3] = 1.0;
//        allData[4][4] = 1.0;
//        allData[5][4] = 1.0;
//        allData[4][5] = 1.0;
//        allData[5][5] = 1.0;
//        allData[6][5] = 1.0;
//        allData[3][6] = 1.0;
//        allData[4][6] = 1.0;
//        allData[5][6] = 1.0;
//        allData[6][6] = 1.0;
//        allData[7][6] = 1.0;
//        allData[8][6] = 1.0;
//        allData[3][7] = 1.0;
//        allData[4][7] = 1.0;
//        allData[5][7] = 1.0;
//        allData[6][7] = 1.0;


        //                System.out.println(" threshold=" + total / (x * y));
        DrawingBoard drawingBoard = new DrawingBoard(allData, 0.5);
        drawingBoard.setBackground(Color.WHITE);
        frame.add(drawingBoard);
        frame.setVisible(true);

        try {
            Thread.sleep(1000);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
