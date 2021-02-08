package util;

import entity.LineSegment;
import entity.Point;
import entity.Vector;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 扩展的数学工具类 */
public class MathUtil {

    /**
     * 判断线段a和b是否相交
     * @param a
     * @param b
     * @return 如果a,b相交，返回交点，否则返回null
     */
    public static Point intersectionOfTwoLineSegment(LineSegment a,LineSegment b){
        double[] line1 = getLineFromLineSegment(a);
        double[] line2 = getLineFromLineSegment(b);
        Point intersection = getIntersectionOfTwoLines(line1, line2);
        if (intersection == null){
            return null;
        }
        double x1 = a.getFirst().getX();
        double x2 = a.getSecond().getX();
        double x = intersection.getX();
        double res1 = ArithUtil.mul(ArithUtil.sub(x, x1), ArithUtil.sub(x, x2));

        double y1 = a.getFirst().getY();
        double y2 = a.getSecond().getY();
        double y = intersection.getY();
        double res2 = ArithUtil.mul(ArithUtil.sub(y, y1), ArithUtil.sub(y, y2));

        if (Double.compare(res1,0) <= 0 && Double.compare(res2,0)<=0){
            return intersection;
        }
        return null;
    }

    /**
     * 从线段中获取该线段所在的直线
     * @param lineSegment
     * @return
     */
    public static double[] getLineFromLineSegment(LineSegment lineSegment){
        double[] line = new double[3];
        Point first = lineSegment.getFirst();
        Point second = lineSegment.getSecond();
        if (Double.compare(first.getX(),second.getX()) == 0){
            // 该线段垂直于x轴
            line[0] = 1.0;
            line[1] = 0.0;
            line[2] = ArithUtil.mul(-1,first.getX());
        }else if (Double.compare(first.getY(),second.getY()) == 0){
            // 该线段垂直于y轴
            line[0] = 0.0;
            line[1] = 1.0;
            line[2] = ArithUtil.mul(-1,first.getY());
        }else {
            double k = ArithUtil.div(ArithUtil.sub(second.getY(),first.getY()),ArithUtil.sub(second.getX(),first.getX()));
            line[0] = k;
            line[1] = -1;
            line[2] = ArithUtil.sub(first.getY() , ArithUtil.mul(first.getX(),k));
        }

        return line;
    }

    /**
     * 获取一个点到轮廓点的最小距离
     * @param points
     * @param point
     * @return
     */
    public static double getMinDistance(List<Point> points,Point point){
        double min = 1e10;
        for (Point p : points) {
            double distanceBetweenTwoPoints = getDistanceBetweenTwoPoints(p, point);
            if (Double.compare(distanceBetweenTwoPoints,min)<0){
                min = distanceBetweenTwoPoints;
            }
        }
        return min;
    }

    /**
     * 获取两个点之间的距离
     * @param point1
     * @param point2
     * @return
     */
    public static double getDistanceBetweenTwoPoints(Point point1,Point point2){
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        return Math.sqrt(Math.pow(ArithUtil.sub(x1,x2),2)+Math.pow(ArithUtil.sub(y1,y2),2));
    }

    /**
     * 获取两条直线的交点
     * @param line1
     * @param line2
     * @return
     */
    public static Point getIntersectionOfTwoLines(double[] line1,double[] line2){
        // 两直线平行，没有交点
        if (Double.compare(line1[0],0.0) ==0 && Double.compare(line2[0],0.0) ==0){
            return  null;
        }
        if (Double.compare(line1[0], 0.0) != 0 && Double.compare(line2[0], 0.0) != 0) {
            if (Double.compare(ArithUtil.div(line1[1],line1[0]),ArithUtil.div(line2[1],line2[0]))==0){
                return null;
            }
        }
        // 两直线不平行，有交点
        double x = (line1[1]*line2[2] - line2[1]*line1[2])/(line1[0]*line2[1] - line2[0]*line1[1]);
        double y = (line2[0]*line1[2] - line1[0]*line2[2])/(line1[0]*line2[1] - line2[0]*line1[1]);
        Point point = new Point(x,y);
        return point;
    }

    /**
     * 根据点和向量确定直线方程
     * @param point
     * @param vector
     * @return
     */
    public static double[] getLineFromPointAndVector(Point point,Vector vector){
        double[] res = new double[3];
        if (Double.compare(vector.getX(), 0.0) != 0 && Double.compare(vector.getY(), 0.0) != 0) {
            double k = ArithUtil.div(vector.getY(), vector.getX());
            res[0] = k;
            res[1] = -1;
            res[2] = ArithUtil.sub(point.getY(),ArithUtil.mul(k,point.getX()));
        }else if (Double.compare(vector.getX(),0.0) == 0){
            res[0] = 1;
            res[1] = 0.0;
            res[2] = -1*point.getX();
        }else if(Double.compare(vector.getY(),0.0) == 0){
            res[0] = 0.0;
            res[1] = 1.0;
            res[2] = -1*point.getY();
        }
        return res;
    }

    /**
     * 获取两向量的点乘结果
     * @param vector1
     * @param vector2
     * @return
     */
    public static double getMulOfVector(Vector vector1,Vector vector2) {
        double mul1 = ArithUtil.mul(vector1.getX(), vector2.getX());
        double mul2 = ArithUtil.mul(vector1.getY(), vector2.getY());
        return ArithUtil.add(mul1,mul2);
    }

    /**
     * 判断点point是否在轮廓内，射线法
     * @param contour
     * @param point
     * @return true if point is in contour
     */
    public static boolean isInPolygon(List<LineSegment> contour,Point point){

        // intersectionPoints为y=point.getY()直线与contour的交点
        Set<Point> intersectionPoints = new HashSet<>();
        for (LineSegment lineSegment:contour){
            Point point1 = getPoint(lineSegment, point.getY());
            if (point1!=null){
                intersectionPoints.add(point1);
            }
        }

        System.out.println(intersectionPoints);

        int count = 0;
        for (Point p:intersectionPoints){
            if (Double.compare(p.getX(),point.getX()) < 0){
                count++;
            }
        }

        // 是奇数，在轮廓里
        boolean flag = false;
        if (count%2!=0){
            flag = true;
        }
        return flag;
    }

    /**
     * 知道一个点的y坐标，获取该点坐标
     * @param lineSegment
     * @param y
     * @return
     */
    public static Point getPoint(LineSegment lineSegment,double y){
        Point first = lineSegment.getFirst();
        Point second = lineSegment.getSecond();
        double x1 = first.getX();
        double y1 = first.getY();
        double x2 = second.getX();
        double y2 = second.getY();

        if (Double.compare(y,y1) < 0 && Double.compare(y,y2) < 0){
            return null;
        }
        if (Double.compare(y,y1) > 0 && Double.compare(y,y2) > 0){
            return null;
        }
        if (Double.compare(y1,y2)==0 ){
            return null;
        }

        double a = ArithUtil.div(ArithUtil.mul(ArithUtil.sub(y, y1), ArithUtil.sub(x2, x1)), ArithUtil.sub(y2, y1));
        double x = ArithUtil.add(a,x1);

        Point point = new Point(x,y);
        return point;
    }

    /**
     * 返回A-->B的单位向量
     * @param A
     * @param B
     * @return
     */
    public static Vector getNormalVectorFromTwoPoints(Point A,Point B){
        Vector vector = new Vector();
        vector.setX(ArithUtil.sub(B.getX(),A.getX()));
        vector.setY(ArithUtil.sub(B.getY(),A.getY()));
        vector.normalize();
        return vector;
    }


    /**
     * 返回所有点的中心点
     * @param points
     * @return
     */
    public static Point getCenterPoint(List<Point> points){
        double x = 0.0;
        double y = 0.0;
        for (Point point:points){
            x = ArithUtil.add(x,point.getX());
            y = ArithUtil.add(y,point.getY());
        }
        x = ArithUtil.div(x,points.size());
        y = ArithUtil.div(y,points.size());

        return new Point(x,y);
    }


    /**
     * 判断直线与线段是否相交,包括线段端点
     * @param line 直线
     * @param lineSegment 线段
     * @return 如果相交，结果为true
     */
    public static boolean isCrossALine(double[] line,LineSegment lineSegment){
        boolean flag = false;
        Point first = lineSegment.getFirst();
        Point second = lineSegment.getSecond();
        double a = line[0] * first.getX() + line[1] * first.getY() + line[2];
        double b = line[0] * second.getX() + line[1] * second.getY() + line[2];
        if (Double.compare(a*b,0) <= 0){
            flag=true;
        }
        return flag;
    }

    /**
     * 计算点到直线的距离
     *
     * @param line ax+by+c=0 line[0]=a,line[1]=b,line[2]=c
     * @param point (x0,y0)
     * @return |a*x0+b*y0+c|/sqrt(a^2+b^2)
     */
    public static double distanceBetweenPointAndLine(double[] line, Point point) {
        double member = Math.abs(line[0] * point.getX() + line[1] * point.getY() + line[2]); // 分子
        double denominator = Math.sqrt(line[0] * line[0] + line[1] * line[1]); // 分母
        return member / denominator;
    }

    /**
     * 从两个点获取直线方程
     *
     * @param one
     * @param two
     * @return
     */
    public static double[] getLineFromTwoPoints(Point one, Point two) {
        double x1 = one.getX();
        double y1 = one.getY();
        double x2 = two.getX();
        double y2 = two.getY();

        double[] line = new double[3];
        line[0] = y2 - y1;
        line[1] = x1 - x2;
        line[2] = y1 * (x2 - x1) - x1 * (y2 - y1);

        return line;
    }

    /**
     * 从一个点及法线获取直线方程
     * @param point
     * @param matrix 1x2的矩阵
     * @return
     */
    public static double[] getLineFromAPointAndNormal(Point point,Matrix matrix){
        double[] line = new double[3];
        double k = -1/(matrix.getAsDouble(0,1)/matrix.getAsDouble(0,0));
        line[0] = k;
        line[1] = -1;
        line[2] = point.getY()-k*point.getX();
        return line;
    }

    /**
     * 线段的长度
     * @param lineSegment
     * @return
     */
    public static double lengthOfSegment(LineSegment lineSegment){
        Point first = lineSegment.getFirst();
        Point second = lineSegment.getSecond();

        double x1 = first.getX();
        double y1 = first.getY();
        double x2 = second.getX();
        double y2 = second.getY();

        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }

    /**
     * 计算matrix的最小特征值所对应的特征向量
     * @param matrix
     * @return
     */
    public static Matrix minEigvector(Matrix matrix)  {
        if (matrix.getRowCount()!=matrix.getColumnCount()){
            throw new IllegalArgumentException("请输入方阵");
        }
        // eig[0]是特征向量。eig[1]是特征值
        Matrix[] eig = matrix.eig();
        int minIndex = 0;
        double minEigvalue = eig[1].getAsDouble(0,0);
        for (int i = 1; i < eig[1].getColumnCount(); i++){
            if (eig[1].getAsDouble(i,i) < minEigvalue){
                minEigvalue = eig[1].getAsDouble(i,i);
                minIndex=i;
            }
        }
        return eig[0].transpose().selectRows(Calculation.Ret.NEW,minIndex);
    }


}
