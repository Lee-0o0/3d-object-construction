package test;


import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;
import org.ujmp.core.doublematrix.DoubleMatrix;
import util.MathUtil;

public class Test04 {
    public static void main(String[] args) {
        double[][] data01 = {{1},{2}};
        double[][] data02 = {{3},{4}};
        DenseDoubleMatrix2D matrix01 = DoubleMatrix.Factory.importFromArray(data01);
        DenseDoubleMatrix2D matrix02 = DoubleMatrix.Factory.importFromArray(data02);
        DenseDoubleMatrix2D zeros = DoubleMatrix.Factory.zeros(2, 1);
        System.out.println(matrix01);
        System.out.println(matrix02);

        Matrix minus = matrix01.minus(matrix02);
        Matrix matrix = minus.appendHorizontally(Calculation.Ret.NEW, zeros);
        System.out.println(matrix);
        System.out.println("--------------------------");
        double[][] data03={{1,2},{4,3}};
        DenseDoubleMatrix2D dense = DoubleMatrix.Factory.importFromArray(data03);
        Matrix[] eigenValueDecompostion = dense.eig();
        for (int i = 0; i < eigenValueDecompostion.length; ++i){
            System.out.println("eigenValueDecompostion " + i + "= \n" + eigenValueDecompostion[i]);
        }

        Matrix matrix1 = MathUtil.minEigvector(dense);
        System.out.println(matrix1);
        System.out.println(matrix1.getRowCount());
        System.out.println(matrix1.getColumnCount());
    }
}
