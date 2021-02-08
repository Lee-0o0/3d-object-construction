package com.lee.algorithm;

import com.lee.entity.LineSegment;
import com.lee.entity.Point;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 将轮廓保存到文件中
 */
public class StoreContours {
    public static void storeContours(double height, List<List<LineSegment>> contours) {
        FileWriter fileX = null;
        FileWriter fileY = null;
        FileWriter fileZ = null;
        try {
            fileX = new FileWriter("construct3DObject/file/xListFile.txt",true);
            fileY = new FileWriter("construct3DObject/file/yListFile.txt",true);
            fileZ = new FileWriter("construct3DObject/file/zListFile.txt",true);
            for (List<LineSegment> contour : contours) {
                StringBuffer stringBufferX = new StringBuffer("height:" + height + " ");
                StringBuffer stringBufferY = new StringBuffer("height:" + height + " ");
                StringBuffer stringBufferZ = new StringBuffer("height:" + height + " ");
                List<Point> points = extractOrderedPoints(contour);
//                System.out.println("height="+height);
//                System.out.println("points="+points);
                for (Point point : points) {
                    stringBufferX.append(point.getX()+" ");
                    stringBufferY.append(point.getY()+" ");
                    stringBufferZ.append(height+" ");
                }
                fileX.write(stringBufferX.toString() + "\r\n");
                fileY.write(stringBufferY.toString() + "\r\n");
                fileZ.write(stringBufferZ.toString() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileX != null) {
                try {
                    fileX.flush();
                    fileX.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (fileY != null) {
                try {
                    fileY.flush();
                    fileY.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (fileZ != null) {
                try {
                    fileZ.flush();
                    fileZ.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void clearFile(){
        FileWriter fileX = null;
        FileWriter fileY = null;
        FileWriter fileZ = null;
        try {
            fileX = new FileWriter("construct3DObject/file/xListFile.txt");
            fileY = new FileWriter("construct3DObject/file/yListFile.txt");
            fileZ = new FileWriter("construct3DObject/file/zListFile.txt");
            fileX.write("");
            fileY.write("");
            fileZ.write("");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileX != null) {
                try {
                    fileX.flush();
                    fileX.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (fileY != null) {
                try {
                    fileY.flush();
                    fileY.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (fileZ != null) {
                try {
                    fileZ.flush();
                    fileZ.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static List<Point> extractOrderedPoints(List<LineSegment> contour){
        List<Point> points = new ArrayList<>();

        Point point = contour.get(0).getStartPoint();

        for (int i = 0 ; i < contour.size(); i++){
            points.add(point);
            if (contour.get(i).getStartPoint().equals(point)){
                point = contour.get(i).getEndPoint();
            }else {
                point = contour.get(i).getStartPoint();
            }
        }

        return points;
    }
}
