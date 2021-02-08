package algorithm;

import entity.LineSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtractContours {
    /**
     * 从所有的线段中提取闭环轮廓
     *
     * @param lineSegments 所有边杂糅在一起，没有顺序
     * @return
     */
    public static List<List<LineSegment>> extractContours(List<LineSegment> lineSegments) {
        if (lineSegments == null || lineSegments.size() == 0) return null;
        List<List<LineSegment>> contours = new ArrayList<>();
        boolean[] isvisited = new boolean[lineSegments.size()];
        Arrays.fill(isvisited, false);

        for (int i = 0; i < lineSegments.size(); i++) {
            if (!isvisited[i]) {
                List<LineSegment> contour = new ArrayList<>();
                LineSegment start = lineSegments.get(i);
                LineSegment next = start.getNext();
                LineSegment newStart = new LineSegment(start.getFirst(), start.getSecond());
                newStart.setSquare(start.getSquare());
                contour.add(newStart);
                isvisited[i] = true;
                while (next != start) {
                    LineSegment newNext = new LineSegment(next.getFirst(), next.getSecond());
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
