package com.lee.algorithm;

//Main interpolation algorithm
public class TrilinearInterpAlgorithm {
    private static double value;
    private static double localX, localY, localZ;
    private static int xBase, yBase, zBase;
    private static double[][][] thisMatrix;

    public static void main(String[] args) {
        double[][][] data = new double[2][2][2];
        for (int i = 0;i<2; i ++){
            for (int j = 0; j < 2; j++){
                for (int k = 0; k < 2; k++){
                    data[j][k][i] = i%2;
                }
            }
        }
        System.out.println(interpolation(data,0,0.5,0.3));

    }

    /**
     * 线性插值 , 返回matrix中（xPos,yPos,zPos)处的值
     *
     * @param matrix
     * @param xPos
     * @param yPos
     * @param zPos
     * @return
     */
    public static double interpolation(double[][][] matrix, double xPos, double yPos, double zPos) {
        thisMatrix = matrix;
        localX = xPos;
        localY = yPos;
        localZ = zPos;
        xBase = (int) xPos;
        yBase = (int) yPos;
        zBase = (int) zPos;
        doCalculate();
//        System.out.println("插值结果："+value);
        return value;
    }

    private static void doCalculate() {
        // 获取相应坐标的小数部分
        double deltaX = localX - (double) xBase;
        double deltaY = localY - (double) yBase;
        double deltaZ = localZ - (double) zBase;
//        System.out.println("zbase="+zBase);
        double i1 = thisMatrix[xBase][yBase][zBase] * (1 - deltaZ)
                + thisMatrix[xBase][yBase][zBase + 1] * deltaZ;
        double i2 = thisMatrix[xBase][yBase + 1][zBase] * (1 - deltaZ)
                + thisMatrix[xBase][yBase + 1][zBase + 1] * deltaZ;
        double j1 = thisMatrix[xBase + 1][yBase][zBase] * (1 - deltaZ)
                + thisMatrix[xBase + 1][yBase][zBase + 1] * deltaZ;
        double j2 = thisMatrix[xBase + 1][yBase + 1][zBase] * (1 - deltaZ)
                + thisMatrix[xBase + 1][yBase + 1][zBase + 1] * deltaZ;
        double w1 = i1 * (1 - deltaY) + i2 * deltaY;
        double w2 = j1 * (1 - deltaY) + j2 * deltaY;
        value = w1 * (1 - deltaX) + w2 * deltaX;
    }
}
