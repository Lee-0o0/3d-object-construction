package com.lee.algorithm;

import com.lee.entity.Conditions;
import com.lee.entity.LineSegment;
import com.lee.entity.Point;
import com.lee.entity.Square;
import com.lee.util.ArithUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Marching Squares算法
 */
public class MarchingSquares {
    /**
     * @param data      数据，二维数组代表一个平面
     * @param threshold 阈值
     * @return 所有的轮廓线,无序的轮廓线
     */
    public static List<LineSegment> marchingSquares(double[][] data, double threshold,double accuracy) {
        if (data == null || data.length == 0) {
            return null;
        }
        // 计算小正方形边长
        double width = 1.0;
        while (Double.compare(width,accuracy) >= 0){
            width = ArithUtil.div(width,2);
        }
        int rows = data.length;
        int columns = data[0].length;
//        System.out.println("Marching Squares算法：rows="+rows+",columns="+columns);

        // 遍历数组，获取轮廓线段
        List<LineSegment> lineSegments = new ArrayList<>(50);
        for (int i = 0; i < rows-1; i++) {
            for (int j = 0; j < columns-1; j++) {
//                System.out.println("i="+i+",j="+j);
                double[] squareData = {data[i][j], data[i+1][j], data[i][j+1], data[i + 1][j + 1]};
//                System.out.println(Arrays.toString(squareData));

                boolean in = false;
                boolean out = false;
                for (int m = 0; m < 4; m++){
                    if (Double.compare(squareData[m],threshold) >= 0){
                        in = true;
                    }
                    if (Double.compare(squareData[m],threshold) < 0){
                        out = true;
                    }
                }

                if (in && out){
//                    System.out.println("i="+i+",j="+j);
                }else {
                    continue;
                }
                Point upleft = new Point(i*width, j*width);
                Point lowright = new Point((i + 1)*width, (j + 1)*width);

                Square square = new Square(upleft, lowright, squareData, threshold);
                switch (square.getType()) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        lineSegments.addAll(Conditions.secondCondition(square));
                        break;
                    case 3:
                        lineSegments.addAll(Conditions.thirdCondition(square));
                        break;
                    case 4:
                        lineSegments.addAll(Conditions.forthCondition(square));
                        break;
                    case 5:
                        lineSegments.addAll(Conditions.fifthCondition(square));
                        break;
                    case 6:
                        lineSegments.addAll(Conditions.sixthCondition(square));
                        break;
                    case 7:
                        lineSegments.addAll(Conditions.seventhCondition(square));
                        break;
                    case 8:
                        lineSegments.addAll(Conditions.eighthCondition(square));
                        break;
                    case 9:
                        lineSegments.addAll(Conditions.ninthCondition(square));
                        break;
                    default:
                        break;
                }
            }
        }

        return lineSegments;
    }
}
