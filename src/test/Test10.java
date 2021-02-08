package test;

import algorithm.ScalarContour;
import com.sun.scenario.effect.Offset;
import entity.Point;
import sun.util.resources.sl.CalendarData_sl;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test10 {
    /**
     * 读取闭合曲线（原始轮廓）数据
     *
     * @param path 数据路径
     * @return
     */
    public Map<String, List<Double>> readData(String path) throws IOException {
        Map<String, List<Double>> map = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(path), 1024);
        String x = bufferedReader.readLine();
        String y = bufferedReader.readLine();
        String[] xstrings = x.split(" ");
        String[] ystrings = y.split(" ");
        List<Double> xlist = new ArrayList<>();
        for (String s : xstrings) {
            xlist.add(Double.valueOf(s));
        }
        List<Double> ylist = new ArrayList<>();
        for (String s : ystrings) {
            ylist.add(Double.valueOf(s));
        }
        map.put("x", xlist);
        map.put("y", ylist);
        return map;
    }

    public static void main(String[] args) throws IOException {
        Test10 test10 = new Test10();
        // 读取数据
        Map<String, List<Double>> map = test10.readData("file/test.txt");
        // 构造点集
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < map.get("x").size(); i++) {
            Point point = new Point(map.get("x").get(i), map.get("y").get(i));
            points.add(point);
        }
        // 原轮廓和缩进轮廓点集
        List<List<Point>> res = new ArrayList<>();
        res.add(points);
        // offset algorithm
        List<List<Point>> offset = ScalarContour.offsetAlgorithm(points, 0.4);
        if (offset != null) {
            for (List<Point> contour : offset) {
                res.add(contour);
            }
        }

        // 将结果保存
        for (List<Point> contour : res) {
            System.out.println(contour.size());
            System.out.println(contour);
        }
    }
}
