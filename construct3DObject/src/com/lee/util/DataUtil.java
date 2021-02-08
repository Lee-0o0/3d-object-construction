package com.lee.util;

public class DataUtil {

    /**
     * 矩阵转置
     * @param data
     * @return
     */
    public static double[][] getTransposeMatrix(double[][] data){
        double[][] res = new double[data[0].length+2][data.length+2];

        for (int i = 0; i < res.length; i++){
            for (int j = 0; j < res[0].length; j++){
                if (i==0||j==0||i==res.length-1||j==res[0].length-1){
                    res[i][j]=-1.0;
                }else {
                    res[i][j] = data[j-1][i-1];
                }
            }
        }

        return res;
    }

    /**
     * 获取第layer层转置、扩展了的数据
     *
     * @param data
     * @param layer
     * @return
     */
    public static double[][] getXthLayer(double[][][] data, int layer) {
        int x = data.length;
        int y = data[0].length;
        int z = data[0][0].length;
        if (layer >= z) {
            throw new IllegalArgumentException("请输入正确的layer");
        }

//        double[][] newData = new double[x + 2][y + 2];
        double[][] newData = new double[y + 2][x + 2];
        for (int i = 0; i < x + 2; i++) {
            for (int j = 0; j < y + 2; j++) {
                if (i == 0 || j == 0 || i == x + 1 || j == y + 1) {
                    newData[j][i] = -1.0;
                } else {
                    newData[j][i] = data[i - 1][j - 1][layer];
                }
//                System.out.print(newData[j][i]+"  ");
            }
//            System.out.println();
        }

        return newData;
    }
}
