package com.lee.algorithm;

import com.lee.util.ArithUtil;

/**
 * 插值增加层数
 */
public class Interpolation {

    /**
     * 获取指定高度的该层数据
     * @param data 原始数据
     * @param height 指定高度
     * @param layerHeight 层高
     * @return
     */
    public static double[][] getLayerData(double[][][] data, double height,double layerHeight){
        double[][] result = new double[data.length][data[0].length];

        if (isInteger(height)){
            // 高度为整数，不需要插值
            for (int i = 0; i < data.length; i ++){
                for (int j = 0; j < data[0].length; j++){
                    result[i][j] = data[i][j][(int)height];
                }
            }
        }else {
            // 需要插值
            for (int x = 0 ; x < data.length; x++){
                for (int y = 0; y <data[0].length; y++){
                    if (x==data.length-1 || y == data[0].length-1) {
                        result[x][y] = TrilinearInterpAlgorithm.interpolation(data, x-0.01, y-0.01, height);
                    }else {
                        result[x][y] = TrilinearInterpAlgorithm.interpolation(data, x, y, height);
                    }
                }
            }
        }

        return result;

    }


    /**
     * 线性插值
     * @param data
     * @param height
     * @return
     */
    public static double[][][] linearInterpolate(double[][][] data,double height){
        int layer = 0;
        // 计算层数
        int totalLayer = 0;
        if (isInteger(1.0/height)){
            totalLayer = (data[0][0].length-1)*(int)(1.0/height);
        }else {
            totalLayer = (data[0][0].length-1)*((int)(1.0/height)+1);
        }
//        System.out.println(totalLayer);
        double[][][] result = new double[data.length][data[0].length][totalLayer+1];

        for (int i = 0; i < data[0][0].length; i++){
//            System.out.println("layer = "+layer);
            // 整数层不需要插值
            for (int x = 0 ; x < data.length; x++){
                for (int y = 0; y <data[0].length; y++){
                    result[x][y][layer] = data[x][y][i];
                }
            }
            layer++;
            // 插值
            if (i != data[0][0].length -1) {
                for (double zHeight = ArithUtil.add(i,height); Double.compare(zHeight,i+1) < 0; zHeight = ArithUtil.add(zHeight,height)){
                    for (int x = 0 ; x < data.length; x++){
                        for (int y = 0; y <data[0].length; y++){
                            if (x==data.length-1 || y == data[0].length-1) {
//                            double minValue = data[x][y][i];
//                            double maxValue = data[x][y][i+1];
//                            double value =ArithUtil.mul(ArithUtil.sub(maxValue,minValue) , ArithUtil.sub(zHeight,i));
//                            result[x][y][layer] =  ArithUtil.add(minValue,value);
                                result[x][y][layer] = TrilinearInterpAlgorithm.interpolation(data, x-0.01, y-0.01, zHeight);
                            }else {
                                result[x][y][layer] = TrilinearInterpAlgorithm.interpolation(data, x, y, zHeight);
                            }
                        }
                    }
                    layer++;
                }
            }
        }

        return result;
    }

    private static boolean isInteger(double d){
        return Double.compare(d - (int)d,0) == 0;
    }

//    public static void main(String[] args) {
//        for (double i = 0.001; Double.compare(i,2.01) < 0; i = ArithUtil.add(i,0.001)){
//            System.out.println(i + " 是不是整数 "+isInteger(i));
//        }
//    }
}
