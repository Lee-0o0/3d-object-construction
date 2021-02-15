package com.lee;

import com.lee.algorithm.*;
import com.lee.entity.LineSegment;
import com.lee.listener.CancelListener;
import com.lee.listener.StartListener;
import com.lee.util.ArithUtil;
import com.lee.util.DataUtil;
import com.lee.util.MATReader;
import com.lee.util.SigarUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 主程序
 */
public class Main1 {
    public static void main(String[] args) {
        // SWING显示每层轮廓
        JFrame frame = new JFrame("3D Object Construction");
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // north 设置选择参数的地方,选择文件、设置阈值、层高、
        JPanel jPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(8, 2);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);
        jPanel.setLayout(gridLayout);

        jPanel.add(new JLabel("请选择MAT文件："));
        JComboBox jComboBox = new JComboBox();
        File file = new File("construct3DObject/data");
        if (file.isDirectory()) {
            String[] lists = file.list();
            for (String list : lists) {
                jComboBox.addItem(list.substring(0, list.lastIndexOf(".")));
            }
        }
        jPanel.add(jComboBox);
        jPanel.add(new JLabel("请输入阈值："));
        JTextField thresholdField = new JTextField("0.5");
        jPanel.add(thresholdField);

        jPanel.add(new JLabel("请输入精度："));
        JTextField accuracyField = new JTextField("0.5");
        jPanel.add(accuracyField);

        jPanel.add(new JLabel("请输入层高："));
        JTextField layerHeightField = new JTextField("0.2");
        jPanel.add(layerHeightField);

        jPanel.add(new JLabel("请输入平滑阈值："));
        JTextField smoothingField = new JTextField("0.1");
        jPanel.add(smoothingField);

        jPanel.add(new JLabel("请输入验证阈值："));
        JTextField verificationField = new JTextField("0.1");
        jPanel.add(verificationField);

        JButton ok = new JButton("确定");
        ok.addActionListener(
                new StartListener(
                        frame,
                        jComboBox,
                        thresholdField,
                        accuracyField,
                        layerHeightField,
                        smoothingField,
                        verificationField));
        jPanel.add(ok);
        JButton cancel = new JButton("退出");
        cancel.addActionListener(new CancelListener());
        jPanel.add(cancel);

        frame.add(jPanel, BorderLayout.NORTH);
        frame.add(new JPanel(), BorderLayout.SOUTH);
//        frame.add(new JPanel(), BorderLayout.WEST);
        frame.setVisible(true);
    }

    public static void mainFunction(
            JFrame frame,
            String name,
            double layerHeight,
            double threshold,
            double accuracy,
            double smoothThreshold,
            double verificationThreshold)
            throws CloneNotSupportedException {
        // jPanel 是结果信息
        JPanel jPanel = new JPanel();
        jPanel.setPreferredSize(new Dimension(500, 200));
        GridLayout gridLayout = new GridLayout(15, 1);
        gridLayout.setVgap(10);
        jPanel.setLayout(gridLayout);
        frame.add(jPanel, BorderLayout.EAST);

        JLabel contourShowInfo = new JLabel("轮廓显示信息：");
        contourShowInfo.setForeground(Color.RED);
        jPanel.add(contourShowInfo);
        jPanel.add(new JLabel("黑线代表Marching Squares显示的轮廓"));
        jPanel.add(new JLabel("蓝线代表平滑后的轮廓"));
        jPanel.add(new JLabel("绿线代表平滑、简化但未经验证的轮廓"));
        jPanel.add(new JLabel("红线代表平滑、简化并经验证的轮廓"));

        JLabel contourConstructionInfo = new JLabel("轮廓构造信息：");
        contourConstructionInfo.setForeground(Color.RED);
        jPanel.add(contourConstructionInfo);

        // 获取系统内存,以字节为单位
        long totalMemory = SigarUtil.getTotalMemory();
        if (totalMemory < 407160){
            // 407160 是由最小文件2Br计算而来
            System.out.println("系统内存不足......");
            return;
        }

        long readFileStart = System.currentTimeMillis();
        // 读取数据
        double[][][] data = null;
        try {
            data = MATReader.readMAT("data/" + name + ".mat");
        } catch (IOException e) {
            System.out.println("读取数据失败!");
            e.printStackTrace();
        }

        // 清空对应的轮廓文件
        StoreContours.clearFile();

        for (double i = 0; Double.compare(i,data[0][0].length-1) <=0 ; i = ArithUtil.add(i,layerHeight)){
            System.out.print("高度: "+i+"  ");
            // 得到第i层的数据
            double[][] res = Interpolation.getLayerData(data,i,layerHeight);
            // 第i层数据四叉树插值，获取内部点数据
            double[][] layerData = QuadtreeRecursion.quadtree(data, res, accuracy, i);
            // 转置
            layerData = DataUtil.getTransposeMatrix(layerData);

            List<LineSegment> lineSegments =
                    MarchingSquares.marchingSquares(layerData, threshold, accuracy);
            if (lineSegments.size() == 0) continue;
            // 由于后续平滑算法会改变点的位置，所以此处复制一份最初的轮廓线段
            List<LineSegment> initLineSegments = new ArrayList<>();
            for (LineSegment lineSegment : lineSegments) {
                initLineSegments.add(
                        new LineSegment(
                                lineSegment.getStartPoint(), lineSegment.getEndPoint()));
            }

            LineSegmentOrdered.orderingLineSegment(lineSegments);

            List<LineSegment> smoothLineSegments =
                    Smoothing.smoothing(lineSegments, smoothThreshold);
            // 平滑算法完成后，还有next信息，即可以通过当前线段找到下一条线段

            List<List<LineSegment>> contours =
                    ExtractContours.extractContours(smoothLineSegments);
            //      List<List<LineSegment>> contours =
            // ExtractContours.extractContours(lineSegments);
            // contour.get(index)即为一个闭环,线段有序,即contour.get(i)的下一条边就是contour.get(i+1)

            //        System.out.println(0);
            //      List<List<LineSegment>> regions =
            // ContourSimplification.createRegion(contours);
            List<List<LineSegment>> regions = ContourSimplification.createRegionOfDP(contours);
            //        System.out.println(1);

            List<LineSegment> verificatedLineSegments =
                    ContourSimplification.verification(regions, verificationThreshold);

            //        System.out.println(2);
            LineSegmentOrdered.orderingLineSegment(verificatedLineSegments);

            //        System.out.println(3);
            List<List<LineSegment>> finalContours =
                    ExtractContours.extractContours(verificatedLineSegments);

            //        System.out.println(4);
            // 保存轮廓数据到文件中
            StoreContours.storeContours(i, finalContours);
            //                  StoreContours.storeContours(zHeight,contours);


        }
        System.out.println();
        System.out.println("提取轮廓消耗时间" + (System.currentTimeMillis() - readFileStart) + "毫秒。");

        jPanel.add(
                new JLabel(
                        name
                                + "提取轮廓消耗时间："
                                + (System.currentTimeMillis() - readFileStart)
                                + "毫秒"));
        // 生成Gcode文件
        long gcodeStartTime = System.currentTimeMillis();
        GCodeGenerator.generator();
        System.out.println(
                "生成Gcode文件消耗时间：" + (System.currentTimeMillis() - gcodeStartTime) + "毫秒。");

        jPanel.add(
                new JLabel(
                        "生成Gcode文件消耗时间："
                                + (System.currentTimeMillis() - gcodeStartTime)
                                + "毫秒"));

        JLabel resultInfo = new JLabel("结果信息：");
        resultInfo.setForeground(Color.RED);
        jPanel.add(resultInfo);
        File fileX = new File("construct3DObject/file/xListFile.txt");
        String sx = "文件名：" + fileX.getName() + ", 大小：" + fileX.length() / 1024 + "KB。";
        jPanel.add(new JLabel(sx));
        File fileY = new File("construct3DObject/file/yListFile.txt");
        String sy = "文件名：" + fileY.getName() + ", 大小：" + fileY.length() / 1024 + "KB。";
        jPanel.add(new JLabel(sy));
        File fileZ = new File("construct3DObject/file/zListFile.txt");
        String sz = "文件名：" + fileZ.getName() + ", 大小：" + fileZ.length() / 1024 + "KB。";
        jPanel.add(new JLabel(sz));

        File fileGcode = new File("construct3DObject/file/output.gcode");
        String sGcode = "文件名：" + fileGcode.getName() + ", 大小：" + fileGcode.length() / 1024 + "KB。";
        jPanel.add(new JLabel(sGcode));

        frame.setVisible(true);
    }
}
