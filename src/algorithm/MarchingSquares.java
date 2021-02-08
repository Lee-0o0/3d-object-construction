package algorithm;

import entity.Conditions;
import entity.LineSegment;
import entity.Point;
import entity.Square;

import java.util.ArrayList;
import java.util.List;

public class MarchingSquares {
    public static List<LineSegment> marchingSquares(int rows,int columns,double horizontalGap,double verticalGap,double[][] data,double threshold){
        List<LineSegment> lineSegments = new ArrayList<>(20);
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {
                Point upleft = new Point(10 + horizontalGap * j, 10 + verticalGap * i);
                Point lowright =
                        new Point(10 + horizontalGap * (j + 1), 10 + verticalGap * (i + 1));
                double[] squareData = {
                        data[i][j], data[i][j + 1], data[i + 1][j], data[i + 1][j + 1]
                };
                Square square = new Square(upleft, lowright, squareData, threshold);
                switch (square.getType()) {
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
