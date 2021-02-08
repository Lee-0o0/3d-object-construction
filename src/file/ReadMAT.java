package file;


import com.jmatio.io.MatFileHeader;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;

import java.io.IOException;
import java.util.Map;

public class ReadMAT {
    public static double[][][] readMAT(String path) throws IOException {
        System.out.println("----------------" + path + "----------------");

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
            System.out.println("key=" + key);
            MLArray mlArray = content.get(key);
            System.out.println("value=" + mlArray.toString());
            System.out.println();

            // 转换为java数组
            transferMLArray = transferMLArray(mlArray);

            // 打印数据
//            int[] dimensions = mlArray.getDimensions();
//            for (int k = 0; k < dimensions[2]; k++) {
//                System.out.println("第" + (k + 1) + "页数据");
//
//                for (int i = 0; i < dimensions[0]; i++) {
//                    for (int j = 0; j < dimensions[1]; j++) {
//                        System.out.print(transferMLArray[i][j][k] + "  ");
//                    }
//                    System.out.println();
//                }
//                System.out.println();
//            }
        }
        return transferMLArray;
    }

    /**
     * 将MLArray转换为Java数组
     *
     * @param mlArray
     * @return
     */
    public static double[][][] transferMLArray(MLArray mlArray) {
        int[] dimensions = mlArray.getDimensions();
        double[][][] result = null;
        if (mlArray.isSingle()) {
            System.out.println("mlArray.isSingle()");
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
            System.out.println("mlArray.isDouble()");
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
