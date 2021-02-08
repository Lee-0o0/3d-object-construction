package util;

import entity.LineSegment;
import entity.Point;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;
import org.ujmp.core.doublematrix.DoubleMatrix;

import java.util.List;

public class StepUtil {

    /**
     * 获取index在list中的前一个位置，循环链表
     * @param list
     * @param index
     * @return
     */
    public static int preIndex(List<? extends Object> list,int index){
        int preIndex = index-1;
        if (preIndex==-1){
            preIndex = list.size()-1;
        }
        return preIndex;
    }

    /**
     * 获取index在list中的后一个位置，循环链表
     * @param list
     * @param index
     * @return
     */
    public static int nextIndex(List<? extends Object> list,int index){
        int nextIndex = index+1;
        if (nextIndex == list.size()){
            nextIndex = 0;
        }
        return nextIndex;
    }

    /**
     * 计算一个区域的重心点
     * @param region
     * @return
     */
    public static Point computeBarycenter(List<LineSegment> region){
        double memberX = 0.0;       // 分子X
        double memberY = 0.0;
        double denominator = 0.0;  // 分母
        for (LineSegment lineSegment:region){
            double length = MathUtil.lengthOfSegment(lineSegment);
            memberX += length*(lineSegment.getFirst().getX()+lineSegment.getSecond().getX());
            memberY += length*(lineSegment.getFirst().getY()+lineSegment.getSecond().getY());
            denominator+= length*2;
        }
        return new Point(memberX/denominator,memberY/denominator);
    }

    /**
     * 计算一个区域的协方差矩阵
     * @param region 区域线段
     * @param xi 区域的重心点barycenter
     * @return 协方差矩阵
     */
    public static Matrix computeCovarianceMatrix(List<LineSegment> region, Point xi){
        // 矩阵C
        DenseMatrix C = Matrix.Factory.zeros(2, 2);
        C.setAsDouble(1.0/3,0,0);

        DenseMatrix firstMatrix = Matrix.Factory.zeros(2, 2);
        // xi*xi转置
        DenseMatrix xiMatrix = Matrix.Factory.zeros(2, 1);
        xiMatrix.setAsDouble(xi.getX(),0,0);
        xiMatrix.setAsDouble(xi.getY(),1,0);
        Matrix transposeXi = xiMatrix.transpose();

        Matrix secondMatrix = xiMatrix.mtimes(transposeXi);

        double totalLength = 0.0;

        for (LineSegment lineSegment:region){
            double length = MathUtil.lengthOfSegment(lineSegment);
            totalLength += length;
            // 矩阵ve
            DenseMatrix ve = Matrix.Factory.zeros(2, 1);
            ve.setAsDouble(lineSegment.getFirst().getX(),0,0);
            ve.setAsDouble(lineSegment.getFirst().getY(),1,0);
            // 矩阵vs
            DenseMatrix vs = Matrix.Factory.zeros(2, 1);
            vs.setAsDouble(lineSegment.getSecond().getX(),0,0);
            vs.setAsDouble(lineSegment.getSecond().getY(),1,0);
            // 矩阵vs的转置
            Matrix transposeVs = vs.transpose();
            // 矩阵vb及其转置
            Matrix vb = (ve.minus(vs)).times(0.5);
            Matrix transposeVb = vb.transpose();
            // 矩阵A及其转置
            DenseDoubleMatrix2D zeros = DoubleMatrix.Factory.zeros(2, 1);
            Matrix A = (ve.minus(vs)).appendHorizontally(Calculation.Ret.NEW, zeros);
            Matrix transposeA = A.transpose();
            // 开始计算
            Matrix part01 = A.mtimes(C).mtimes(transposeA);
            Matrix part02 = vs.mtimes(transposeVs);
            Matrix part03 = vs.mtimes(transposeVb);
            Matrix part04 = vb.mtimes(transposeVs);
            Matrix times = part01.plus(part02).plus(part03).plus(part04).times(length);
            firstMatrix.plus(times);
        }
        secondMatrix.times(totalLength);
        // 协方差矩阵
        Matrix Mi = firstMatrix.minus(secondMatrix);
        return Mi;
    }
}
