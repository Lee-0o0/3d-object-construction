package com.lee.algorithm;

import com.lee.entity.LineSegment;

import java.util.List;

/**
 * 轮廓线段有序算法，换句话说，就是确定每条轮廓线段的下一条
 */
public class LineSegmentOrdered {
    /**
     * 确定每条线段的next
     * @param lineSegments 无序的轮廓线
     */
    public static void orderingLineSegment(List<LineSegment> lineSegments){
        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment lineSegment = lineSegments.get(i);

            while (lineSegment.getNext() == null) {
                // 该条线段还没有确定next
//                System.out.println("**************");
//                System.out.println((lineSegments.indexOf(lineSegment)+1)+" : "+lineSegment.getStartPoint()+"  "+lineSegment.getEndPoint());
////                System.out.println(lineSegment.getSquare());
//                System.out.println("***************");
                for (int j = 0; j < lineSegments.size(); j++) {
                    // 从所有的轮廓线中找当前线段的next
                    LineSegment target = lineSegments.get(j);
//                    System.out.print((j+1)+" "+target);
//                    System.out.println("    next"+(lineSegments.indexOf(target.getNext())+1));
//                    System.out.println(target.getSquare());
                    boolean flag1 = target.getNext() != lineSegment && target != lineSegment;
                    if (flag1) {
                        boolean flag2 = lineSegment.isEndPoint(target.getStartPoint())
                                || lineSegment.isEndPoint(target.getEndPoint());
                        if (flag2) {
                            // 找到了
//                            System.out.println("找到了"+(j+1)+target);
                            lineSegment.setNext(target);
                            lineSegment = lineSegment.getNext();
                            break;
                        }
                    }
                }
            }
        }
    }
}
