package com.lee.algorithm;

import com.lee.entity.LineSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtractContours {
    /**
     * 从所有的线段中提取闭环轮廓
     * @param lineSegments 具有next信息的线段，但无法区分闭环
     * @return
     */
    public static List<List<LineSegment>> extractContours(List<LineSegment> lineSegments){
        if (lineSegments==null||lineSegments.size()==0) return null;
        List<List<LineSegment>> contours = new ArrayList<>();

        boolean[] isvisited = new boolean[lineSegments.size()];
        Arrays.fill(isvisited,false);

        for (int i = 0; i < lineSegments.size(); i++){
            if (!isvisited[i]){
                List<LineSegment> contour = new ArrayList<>();
                LineSegment start = lineSegments.get(i);
                LineSegment newStart = new LineSegment(start.getStartPoint(),start.getEndPoint());
                newStart.setSquare(start.getSquare());

                LineSegment next = start.getNext();

                contour.add(newStart);
                isvisited[i] = true;
                while (next!=start){
//                    System.out.println(lineSegments.indexOf(next)+": "+next);
                    LineSegment newNext = new LineSegment(next.getStartPoint(),next.getEndPoint());
                    newNext.setSquare(next.getSquare());
                    contour.add(newNext);
                    isvisited[lineSegments.indexOf(next)] = true;
                    next = next.getNext();
                }
                contours.add(contour);
            }
        }

        return contours;
    }
}
