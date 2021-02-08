package algorithm;

import entity.LineSegment;
import java.util.List;

/** 从轮廓线中按顺序提取轮廓点 */
public class ExtractPointAlgorithm {
    public static void extractPoint(List<LineSegment> lineSegments) {
//        int count = 0;
        for (int i = 0; i < lineSegments.size(); i++) {
//            System.out.println("---");
            LineSegment lineSegment = lineSegments.get(i);

            int current = i;
            while (lineSegment.getNext() == null) {
//                System.out.println(
//                        (current+1)+"：*******"
//                                + lineSegment.getFirst()
//                                + "--"
//                                + lineSegment.getSecond()
//                                + "*******");

                for (int j = 0; j < lineSegments.size(); j++) {
                    LineSegment target = lineSegments.get(j);
//                    System.out.print((j+1)+"："+target.getFirst() + "  " + target.getSecond()+"    ");
//                    System.out.println(target.getNext()==null);

                    boolean flag1 = target.getNext() != lineSegment && target != lineSegment;
//                    System.out.print(flag1+"   ");
                    if (flag1) {
                        boolean flag2 = lineSegment.isEndPoint(target.getFirst()) || lineSegment.isEndPoint(target.getSecond());
//                        System.out.print(flag2);
//                        System.out.println();
                        if (flag2) {
//                            System.out.println(
//                                    (j+1)+"：找到了" + target.getFirst() + "  " + target.getSecond());
                            lineSegment.setNext(target);
                            lineSegment = lineSegment.getNext();
                            current = j;
//                            count++;
//                            System.out.println(count+"条线段找到了下一条");
                            break;
                        }
                    }
                }
            }
        }
    }
}
