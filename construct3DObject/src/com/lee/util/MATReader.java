package com.lee.util;

import com.jmatio.io.MatFileHeader;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * MAT文件读取类
 */
public class MATReader {
    /**
     * 根据文件路径读取三维数组数据
     * @param path mat文件路径
     * @return
     * @throws IOException
     */
    public static double[][][] readMAT(String path) throws IOException {

        MatFileReader matFileReader = new MatFileReader(path);
        // 读取MAT Header信息
        MatFileHeader matFileHeader = matFileReader.getMatFileHeader();
        System.out.println("*****mat header*****");
        System.out.println(matFileHeader.toString());

        // 读取矩阵信息,通过矩阵名来获取矩阵信息
        System.out.println("*****mat body*****");

        Map<String, MLArray> content = matFileReader.getContent();

        double[][][] transferMLArray = null;
        for (String key : content.keySet()) {
            System.out.print("key=" + key);
            MLArray mlArray = content.get(key);
            System.out.println("， value=" + mlArray.toString());
            System.out.println();
            Properties properties = new Properties();
            properties.setProperty("xDim",String.valueOf(mlArray.getDimensions()[0]+2));
            properties.setProperty("yDim",String.valueOf(mlArray.getDimensions()[1]+2));
            properties.setProperty("zDim",String.valueOf(mlArray.getDimensions()[2]));
            properties.setProperty("threshold",String.valueOf(0.5));
            properties.setProperty("parameterName",key);
            properties.storeToXML(new FileOutputStream("construct3DObject/file/matrix.xml"),"matrix info");

            // 转换为java数组
            transferMLArray = transferMLArray(mlArray);
        }
        System.out.println("Read MAT Finished!");
        return transferMLArray;
    }

    /**
     * 将MLArray转换为Java数组
     *
     * @param mlArray
     * @return
     */
    private static double[][][] transferMLArray(MLArray mlArray) throws IOException{
        int[] dimensions = mlArray.getDimensions();
        double[][][] result = null;
        if (mlArray.isSingle()) {
            System.out.println("mlArray is MLSingle");
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("construct3DObject/file/matrix.xml"));
            properties.setProperty("fileType","MLSingle");
            properties.storeToXML(new FileOutputStream("construct3DObject/file/matrix.xml"),"matrix info");

            MLSingle mlSingle = (MLSingle) mlArray;
            result = new double[dimensions[0]][dimensions[1]][dimensions[2]];

            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    for (int k = 0; k < dimensions[2]; k++) {
                        // 列优先，注意转换
                        result[i][j][k] = mlSingle.get(i, k * dimensions[1] + j);
                    }
                }
            }
        } else if (mlArray.isDouble()) {
            System.out.println("mlArray is MLDouble");
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("construct3DObject/file/matrix.xml"));
            properties.setProperty("fileType","MLDouble");
            properties.storeToXML(new FileOutputStream("construct3DObject/file/matrix.xml"),"matrix info");

            MLDouble mlSingle = (MLDouble) mlArray;
            result = new double[dimensions[0]][dimensions[1]][dimensions[2]];

            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    for (int k = 0; k < dimensions[2]; k++) {
                        // 列优先，注意转换
                        result[i][j][k] = mlSingle.get(i, k * dimensions[1] + j);
                    }
                }
            }
        }

        return result;
    }
}
