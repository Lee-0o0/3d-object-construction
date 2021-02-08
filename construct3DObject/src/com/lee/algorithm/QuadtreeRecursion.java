package com.lee.algorithm;

import com.lee.util.ArithUtil;

public class QuadtreeRecursion {

    public static double[][] layerQuadtree(double[][][] data,int k,double accuracy){
        // 计算一个单位为1的正方形分裂成几个小正方形
        double width = 1.0;
        // 小正方形的数量
        int num = 1;
        while (Double.compare(width,accuracy) >= 0){
            num = num*4;
            width = ArithUtil.div(width,2);
        }

        // 计算新的数据大小
        int anum = (int)Math.sqrt(num);
//        System.out.println("num="+num+",anum="+anum);
        int rows = (data.length)*anum;
        int columns = (data[0].length)* anum;

        double[][] result = new double[rows][columns];

        for (int i = 0; i < data.length-1; i++) {
            for (int j = 0; j < data[0].length-1; j++) {
                // 确定四个点
                int basei = i*anum;
                int basej = j*anum;

                result[basei][basej] = data[i][j][k];
                result[basei][basej+anum] = data[i][j+1][k];
                result[basei+anum][basej] = data[i+1][j][k];
                result[basei+anum][basej+anum] = data[i+1][j+1][k];

                double height = k;
                if (k == data[0][0].length-1){
                    height -= 0.01;
                }
                for (int a = 1; a < anum; a++){
                    result[basei][basej+a] = TrilinearInterpAlgorithm.interpolation(data,i,j+ArithUtil.div(a,anum),height);
                }
                // 确定内部点
                for (int a = 1; a < anum; a++){
                    for (int b = 0; b <= anum; b++){
                        if (b!=anum) {
                            result[basei + a][basej + b] = TrilinearInterpAlgorithm.interpolation(data, i + a*1.0 / anum, j + b*1.0 / anum, height);
                        }else {
                            result[basei + a][basej + b] = TrilinearInterpAlgorithm.interpolation(data, i + a*1.0 / anum, j + b*1.0 / anum-0.01, height);
                        }
                    }
                }
            }
        }

        return result;
    }

    public static double[][][] quadtreeRecurse(double[][][] data,double accuracy){
        // 计算一个单位为1的正方形分裂成几个小正方形
        double width = 1.0;
        // 小正方形的数量
        int num = 1;
        while (Double.compare(width,accuracy) >= 0){
            num = num*4;
            width = ArithUtil.div(width,2);
        }

        // 计算新的数据大小
        int anum = (int)Math.sqrt(num);
        System.out.println("num="+num+",anum="+anum);
        int rows = (data.length)*anum;
        int columns = (data[0].length)* anum;
        double[][][] result = new double[rows][columns][data[0][0].length];

        // 计算每个小正方形各点的数据

        for (int k = 0; k < data[0][0].length-1; k++) {
            for (int i = 0; i < data.length-1; i++) {
                for (int j = 0; j < data[0].length-1; j++) {
                    // 确定四个点
                    int basei = i*anum;
                    int basej = j*anum;

                    result[basei][basej][k] = data[i][j][k];
                    result[basei][basej+anum][k] = data[i][j+1][k];
                    result[basei+anum][basej][k] = data[i+1][j][k];
                    result[basei+anum][basej+anum][k] = data[i+1][j+1][k];
                    double height = k;
                    for (int a = 1; a < anum; a++){
                        result[basei][basej+a][k] = TrilinearInterpAlgorithm.interpolation(data,i,j+ArithUtil.div(a,anum),height);
                    }
                    // 确定内部点
                    for (int a = 1; a < anum; a++){
                        for (int b = 0; b <= anum; b++){
                            if (b!=anum) {
                                result[basei + a][basej + b][k] = TrilinearInterpAlgorithm.interpolation(data, i + a*1.0 / anum, j + b*1.0 / anum, height);
                            }else {
                                result[basei + a][basej + b][k] = TrilinearInterpAlgorithm.interpolation(data, i + a*1.0 / anum, j + b*1.0 / anum-0.01, height);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
