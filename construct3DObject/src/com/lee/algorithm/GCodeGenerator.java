package com.lee.algorithm;

import com.lee.entity.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * 生成G-code
 */
public class GCodeGenerator {

    public static void generator() {
        // 开始时间
//        long startTime = System.currentTimeMillis();

        /** 获取、组装边界 **/
        Properties properties = new Properties();
        try {
            // 加载配置文件
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            properties.loadFromXML(new FileInputStream("construct3DObject/file/matrix.xml"));
            // 获取三个维度信息
            Double xDim = Double.parseDouble(properties.getProperty("xDim"));
            Double yDim = Double.parseDouble(properties.getProperty("yDim"));
            Double zDim = Double.parseDouble(properties.getProperty("zDim"));
            // 缩放比例
            GCodeParameters.scaling = Integer.parseInt(properties.getProperty("scaling"));
            GCodeParameters.maxDim = GCodeParameters.scaling * Math.max(xDim, Math.max(yDim, zDim));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将默认的开始和结束部分G-code信息写入对应txt件中
        storeStartEndGcode();
        // 重新构造高度文件（根据定义的输入高度）
        reconstructFile();
        // 组装单层点数据和层高数据
        storeData();

        /** 生成G-code **/
        try {
            // 先清空文件
            File file = new File("construct3DObject/file/Output.gcode");
            if (file.exists()){
                file.delete();
            }
            file.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        FileWriter fw = null;

        try{
            // 以向尾部添加的方式打开文件
            fw = new FileWriter("construct3DObject/file/Output.gcode", true);

            // 用于存储最终用于界面显示的G-code
            StringBuilder finalGcodeBuilder = new StringBuilder();
            // 每一步中间存储的G-code
            String code = "";

            /** 处理开始部分G-code **/
            // 生成开始部分G-code
            code = generateStart(code);
            // 将开始部分的G-code加入最终界面显示G-code
            finalGcodeBuilder.append(code);
            // 将开始部分的G-code写入文件
            fw.write(code);
            code = "";

            /** 处理基础部分G-code **/
            code = generateBasic(code);
            // 将基础部分的G-code加入最终界面显示G-code
            finalGcodeBuilder.append(code);
            // 将基础部分的G-code写入文件
            fw.write(code);
            code = "";

            /** 处理底部G-code **/
            code = generateBottom(code);
            // 将底部部分的G-code加入最终界面显示G-code
            finalGcodeBuilder.append(code);
            // 将底部部分的G-code写入文件
            fw.write(code);
            code = "";

            fw.flush();
            fw.close();

            /** 生成中间部分G-code **/
            finalGcodeBuilder = generateInner(finalGcodeBuilder);

            // 重新以向尾部添加的方式打开文件
            fw = new FileWriter("construct3DObject/file/Output.gcode", true);

            /** 处理顶部部分G-code **/
            code = generateTop(code);
            // 将顶部部分的G-code加入最终界面显示G-code
            finalGcodeBuilder.append(code);
            // 将顶部部分的G-code写入文件
            fw.write(code);
            code = "";

            /** 处理最后一层G-code **/
            code = generateLastTop(code);
            // 将最后一层部分的G-code加入最终界面显示G-code
            finalGcodeBuilder.append(code);
            // 将最后一层部分的G-code写入文件
            fw.write(code);
            code = "";

            /** 处理结束部分G-code **/
            code = generateEnd(code);
            // 将结束部分的G-code加入最终界面显示G-code
            finalGcodeBuilder.append(code);
            // 将结束部分的G-code写入文件
            fw.write(code);
            code = "";

            // 结束时间
//            long endTime = System.currentTimeMillis();
//            System.out.println("生成G-code消耗时间：" + (endTime - startTime)/1000.0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw!=null){
                try {
                    fw.flush();
                    fw.close();
                }catch (Exception ee){
                    ee.printStackTrace();
                }
            }
        }
        writeSupportLines();
//        System.out.println("end all !!!");
    }

    private static void writeSupportLines() {
        try {
            FileWriter fileWriter = new FileWriter(new File("construct3DObject/file/supportLines.txt"));
            fileWriter.write("");
            HashMap<Integer, HashMap<String, ArrayList<ArrayList<Point>>>> supportLineStore = GCodeParameters.supportLineList;
            for (int i = 0; i < supportLineStore.size(); i++) {
                Double printHeight = GCodeParameters.layerHeight * (i + 1);
                HashMap<String, ArrayList<ArrayList<Point>>> layerSupportLine = supportLineStore.get(i);
                ArrayList<ArrayList<Point>> leftLayerSupportLines = layerSupportLine.get("left");
                ArrayList<ArrayList<Point>> rightLayerSupportLines = layerSupportLine.get("right");
                for (ArrayList<Point> leftEachLine : leftLayerSupportLines) {
                    Point pointStart = leftEachLine.get(0);
                    Point pointEnd = leftEachLine.get(1);
                    String supportLinePrint = String.valueOf(pointStart.getX()) + " " +
                            String.valueOf(pointStart.getY()) + " " + String.valueOf(printHeight) + " " +
                            String.valueOf(pointEnd.getX()) + " " + String.valueOf(pointEnd.getY()) + " " +
                            String.valueOf(printHeight) + "\n";
                    fileWriter.write(supportLinePrint);
                }
                for (ArrayList<Point> rightEachLine : rightLayerSupportLines) {
                    Point pointStart = rightEachLine.get(0);
                    Point pointEnd = rightEachLine.get(1);
                    String supportLinePrint = String.valueOf(pointStart.getX()) + " " +
                            String.valueOf(pointStart.getY()) + " " + String.valueOf(printHeight) + " " +
                            String.valueOf(pointEnd.getX()) + " " + String.valueOf(pointEnd.getY()) + " " +
                            String.valueOf(printHeight) + "\n";
                    fileWriter.write(supportLinePrint);
                }
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将默认的开始和结束部分G-code信息写入对应txt文件中
     * 便于后续从txt文件中读取写入到Gcode中
     */
    private static void storeStartEndGcode() {
        try {
            FileWriter writerStart = new FileWriter(new File("construct3DObject/file/startGcode.txt"));
            FileWriter writerEnd = new FileWriter(new File("construct3DObject/file/endGcode.txt"));
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            String startGcode = properties.getProperty("startGcode");
            String endGcode = properties.getProperty("endGcode");
            writerStart.write(startGcode);
            writerEnd.write(endGcode);
            writerStart.flush();
            writerEnd.flush();
            writerStart.close();
            writerEnd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 重新构造高度文件（根据定义的输入高度）
     * 将zListFile文件中的高度保存到内存中，GCodeParameters.zStrings中
     */
    private static void reconstructFile() {
        try {
//            GCodeParameters.zStrings.clear();
            // 加载高度文件
            FileInputStream zFile = new FileInputStream("construct3DObject/file/zListFile.txt");
            BufferedReader readerZ = new BufferedReader(new InputStreamReader(zFile));
            // 加载配置文件
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            Double height = Double.parseDouble(properties.getProperty("height"));
            Double firstHeight = Double.parseDouble(properties.getProperty("firstHeight"));
            Integer count = 0;          // 总轮廓数
            Double zHeight;
            String line = readerZ.readLine();
            while (line != null) {
                // 分割数据
                String[] data = line.split(" ");
                // 如果是空数据，直接跳出
                if (data[0].equals("")) break;
                // 如果第一行的起始高度不是0，直接结束
                if (count == 0 && Double.parseDouble(data[1]) != 0) {
                    readerZ.close();
                    zFile.close();
                    return;
                }
                // 获取高度
                zHeight = Double.parseDouble(data[1]);
                // 高度累加
                if (count == 0) {
                    zHeight += firstHeight;
                } else {
                    zHeight += height;
                }
                String newLine = "";
                Integer length = data.length;
                for (int i = 0; i < length; i++) {
                    newLine += (zHeight + " ");
                }
                GCodeParameters.zStrings.add(newLine);
                count++;
                line = readerZ.readLine();
            }
            readerZ.close();
            zFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 组装单层点数据和层高数据
     */
    private static void storeData() {
        try {
//            GCodeParameters.pointPrintList.clear();
            // 加载配置文件
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            // 三个维度边界信息
            FileInputStream fileX = new FileInputStream("construct3DObject/file/xListFile.txt");
            FileInputStream fileY = new FileInputStream("construct3DObject/file/yListFile.txt");
            FileInputStream fileZ = new FileInputStream("construct3DObject/file/zListFile.txt");
            BufferedReader readerX = new BufferedReader(new InputStreamReader(fileX));
            BufferedReader readerY = new BufferedReader(new InputStreamReader(fileY));
            BufferedReader readerZ = new BufferedReader(new InputStreamReader(fileZ));
            String lineX = readerX.readLine();
            String lineY = readerY.readLine();
            String lineZ = readerZ.readLine();
            // 都非空说明该行对应有
            while ((lineX != null) && (lineY != null) && (lineZ != null)) {
                // 高度
                Double zData = Double.parseDouble(lineZ.split(" ")[1]);
                // 加入行高
                GCodeParameters.zPrintList.add(zData * GCodeParameters.scaling);
                String[] xPoints = lineX.split(" ");
                String[] yPoints = lineY.split(" ");
                ArrayList<Point> singleLayerPoints = new ArrayList<>();
                // 生成单轮廓数据
                for (int i = 1; i < xPoints.length; i++) {
                    Point point = new Point(Double.parseDouble(xPoints[i]) * GCodeParameters.scaling,
                            Double.parseDouble(yPoints[i]) * GCodeParameters.scaling);
                    singleLayerPoints.add(point);
                }
//                singleLayerPoints.remove(singleLayerPoints.size() - 1);
                GCodeParameters.pointPrintList.add(singleLayerPoints);
                lineX = readerX.readLine();
                lineY = readerY.readLine();
                lineZ = readerZ.readLine();
            }
            readerX.close();
            readerY.close();
            readerZ.close();
            fileX.close();
            fileY.close();
            fileZ.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 生成开始部分G-code
     *
     * @param code ： G-code
     * @return
     */
    private static String generateStart(String code) {
        String gcode = code;
        String tempCode = code;
        gcode += "M107\n";
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            String firstBedTemp = properties.getProperty("firstBedTemp");
            GCodeParameters.firstBedTemperature = Double.parseDouble(firstBedTemp);
            gcode += ("M190 S" + GCodeAlgorithm.formatNumber(GCodeParameters.firstBedTemperature, 0) + "\n");
            String firstExtruderTemp = properties.getProperty("firstExtruderTemp");
            GCodeParameters.firstExtruderTemperature = Double.parseDouble(firstExtruderTemp);
            gcode += ("M104 S" + GCodeAlgorithm.formatNumber(GCodeParameters.firstExtruderTemperature, 0) + "\n");
            FileInputStream fileStart = new FileInputStream("construct3DObject/file/startGcode.txt");
            BufferedReader readerStart = new BufferedReader(new InputStreamReader(fileStart));
            String startGcode = "";
            String line = readerStart.readLine();
            while (line != null) {
                line = line.trim();
                startGcode += (line + "\n");
                line = readerStart.readLine();
            }
            gcode += startGcode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder(gcode);
        sb.replace(0, tempCode.length(), "");
        GCodeParameters.codeConditionStore.put("start", sb.toString());
        return gcode;
    }

    /**
     * 生成基础部分 G-code
     *
     * @param code ： G-code
     * @return
     */
    private static String generateBasic(String code) {
        String gcode = code;
        String tempCode = code;
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            gcode += ("M109 S" + properties.getProperty("firstExtruderTemp") + "\n");
            gcode += "G21\nG90\nG82\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder(gcode);
        sb.replace(0, tempCode.length(), "");
        GCodeParameters.codeConditionStore.put("basic", sb.toString());
        return gcode;
    }


    /**
     * 生成底部G-code
     *
     * @param code
     * @return
     */
    private static String generateBottom(String code) {
        String gcode = code;
        String tempCode = code;
        gcode += "G92 E0\n";
        Properties properties = new Properties();
        try {
            GCodeParameters.layerHeight = 0.0;
            GCodeParameters.fileLine = 0;
            // 加载配置文件
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            // 获取参数数据
            GCodeParameters.bedTemperature = Double.parseDouble(properties.getProperty("bedTemp"));
            GCodeParameters.extruderTemperature = Double.parseDouble(properties.getProperty("extruderTemp"));
            GCodeParameters.bottomLayers = Integer.parseInt(properties.getProperty("bottomLayers"));
            GCodeParameters.shellSpeed = Double.parseDouble(properties.getProperty("shellSpeed")) * 60;
            GCodeParameters.externalShellSpeed = Double.parseDouble(properties.getProperty("externalShellSpeed")) * 60;
            GCodeParameters.solidInfillSpeed = Double.parseDouble(properties.getProperty("solidInfillSpeed")) * 60;
            String travelSpeed = properties.getProperty("travelSpeed");
            String firstHeight = properties.getProperty("firstHeight");
            GCodeParameters.travelSpeedValue = Double.parseDouble(travelSpeed) * 60;
            double height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
            gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 3) + "\n";
            // 回缩距离
            String retractLength = properties.getProperty("retractLength");
            GCodeParameters.retractLengthValue = Double.parseDouble(retractLength);
            GCodeParameters.retractSpeed = Double.parseDouble(properties.getProperty("retractSpeed")) * 60;
            gcode += "G1 E-" + GCodeAlgorithm.formatNumber(GCodeParameters.retractLengthValue, 5)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5) + "\n";
            gcode += "G92 E0\n";
            StringBuilder sb = new StringBuilder(gcode);
            sb.replace(0, tempCode.length(), "");
            GCodeParameters.codeConditionStore.put("beforeFirstBottom", sb.toString());
            tempCode = gcode;
            GCodeParameters.lengthUsed = -1 * GCodeParameters.retractLengthValue;

            // 产生第一层
            gcode = generateFirstBottom(gcode, Math.PI / 4);
            sb = new StringBuilder(gcode);
            sb.replace(0, tempCode.length(), "");
            GCodeParameters.codeConditionStore.put("firstBottom", sb.toString());
            tempCode = gcode;
            gcode += "M104 S" + GCodeAlgorithm.formatNumber(GCodeParameters.extruderTemperature, 0) + "\n";
            gcode += "M140 S" + GCodeAlgorithm.formatNumber(GCodeParameters.bedTemperature, 0) + "\n";
            sb = new StringBuilder(gcode);
            sb.replace(0, tempCode.length(), "");
            GCodeParameters.codeConditionStore.put("beforeBottom", sb.toString());
            // 产生底层最上面一层
            gcode = generateBottomTop(gcode, GCodeParameters.bottomLayers, "bottom");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateFirstBottom(String code, Double lineAngle) {
        String gcode = code;
        GCodeParameters.layerHeight = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
        ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
        Properties properties = new Properties();
        try {
            GCodeParameters.shellList.clear();
            GCodeParameters.pointTempList.clear();
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            GCodeParameters.filamentDiameter = Double.parseDouble(properties.getProperty("filamentDiameter"));
//            calculateAverage(pointList);
            GCodeParameters.shellLayers = Integer.parseInt(properties.getProperty("shellLayers"));
            GCodeParameters.extruderWidth = Double.parseDouble(properties.getProperty("extruderWidth"));

            GCodeParameters.firstLayerSpeed = Double.parseDouble(properties.getProperty("firstLayerSpeed")) * 60;
//            lengthUsed = retractLengthValue;
            GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
            GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
            GCodeParameters.shellList.add(GCodeParameters.pointTempList);
            // 生成内缩轮廓
            for (int i = 0; i < GCodeParameters.shellLayers - 1; i++) {
                boolean b = GCodeAlgorithm.generateEachShell();
                if (!b){
                    break;
                }
            }
            GCodeParameters.isRetract = true;
            gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.firstLayerSpeed, GCodeParameters.firstLayerSpeed);

            // 生成内部填充
//            gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, lineAngle, GCodeParameters.firstLayerSpeed,
//                    GCodeParameters.extruderWidth * GCodeParameters.scaling, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GCodeParameters.fileLine++;
        return gcode;
    }

    private static String generateBottomTop(String code, int layers, String ifBottomTop) {
        String gcode = code;
        String tempCode = code;
        Double height;
        StringBuilder sb;
        Integer countIndex = 0;
        for (int i = 1; i <= layers; i++) {
            GCodeParameters.shellList.clear();
            GCodeParameters.pointTempList.clear();
            height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
            gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 0) + "\n";
            ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
//            Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                    pointList.get(1), shellLayers, extruderWidth);
//            previousPrintPoint = pointStart;
//            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
//                    + " Y" + formatNumber(pointStart.getY(), 3)
//                    + " F" + formatNumber(travelSpeedValue, 3)
//                    + "\n";
            GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
            GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
            GCodeParameters.shellList.add(GCodeParameters.pointTempList);
            for (int j = 0; j < GCodeParameters.shellLayers - 1; j++) {
                boolean b = GCodeAlgorithm.generateEachShell();
                if (!b){
                    break;
                }
            }
//            generateEachShell();
            gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.shellSpeed, GCodeParameters.externalShellSpeed);

            // 生成内部填充
//            if (i % 2 == 1)
//                gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, 3 * Math.PI / 4, GCodeParameters.solidInfillSpeed,
//                        GCodeParameters.extruderWidth * GCodeParameters.scaling, false);
//            else
//                gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, Math.PI / 4, GCodeParameters.solidInfillSpeed,
//                        GCodeParameters.extruderWidth * GCodeParameters.scaling, false);
            countIndex++;
            sb = new StringBuilder(gcode);
            sb.replace(0, tempCode.length(), "");
            if (ifBottomTop.equals("bottom"))
                GCodeParameters.codeConditionStore.put("bottom" + String.valueOf(countIndex), sb.toString());
            else if (ifBottomTop.equals("top"))
                GCodeParameters.codeConditionStore.put("top" + String.valueOf(countIndex), sb.toString());
            tempCode = gcode;
            GCodeParameters.fileLine++;
        }
        return gcode;
    }


    /**
     * 生成填充（中间）部分G-code
     *
     * @param stringBuilder : 所有G-code
     * @return
     */
    private static StringBuilder generateInner(StringBuilder stringBuilder) {
        String gcode = "";
        Integer countIndex = 0;
        Properties properties = new Properties();
        try {
            // 以向尾部添加的方式打开文件
            FileWriter fw = new FileWriter(new File("construct3DObject/file/Output.gcode"), true);
            // 加载配置文件
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            // 顶部层数
            GCodeParameters.topLayers = Integer.parseInt(properties.getProperty("topLayers"));
            // 中间层数 = 总层数 - 顶部层数 - 底部层数 - 1
            GCodeParameters.innerLayers = GCodeParameters.zPrintList.size() - GCodeParameters.topLayers - GCodeParameters.bottomLayers - 1;
            // 填充速度
            GCodeParameters.infillSpeed = Double.parseDouble(properties.getProperty("infillSpeed")) * 60;
            // 填充线距离
            GCodeParameters.lineGap = Double.parseDouble(properties.getProperty("lineGap"));

            Double height;
            // 对中间的每一层
            for (int i = 1; i <= GCodeParameters.innerLayers; i++) {
                // 清空之前数据
                GCodeParameters.shellList.clear();
                GCodeParameters.pointTempList.clear();
                // 获取高度
                height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
                // 移动到该高度
                gcode = "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                        + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 0) + "\n";
                // 获取该高度边界点数据
                ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
                // 删除自交点
                GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
                // 计算获得单层边界的平均坐标（x，y）
                GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
                // 加入打印参数中
                GCodeParameters.shellList.add(GCodeParameters.pointTempList);
                // 生成外壳（shellLayers层外壳，减1是减去现有边界）
                for (int j = 0; j < GCodeParameters.shellLayers - 1; j++) {
                    boolean b = GCodeAlgorithm.generateEachShell();
                    if (!b){
                        break;
                    }
                }
                // 生成外壳G-code
                gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.shellSpeed, GCodeParameters.externalShellSpeed);

                // 填充
//                gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, 0.0, GCodeParameters.infillSpeed,
//                        GCodeParameters.lineGap * GCodeParameters.scaling, false);
                countIndex++;
                GCodeParameters.codeConditionStore.put("inner" + countIndex, gcode);
                stringBuilder.append(gcode);
//                System.out.println("inner " + i + " finished.");
                GCodeParameters.fileLine++;
                // 写入文件
                fw.write(gcode);
                // 清空
                gcode = "";
            }
            // 关闭文件
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    /**
     * 生成顶部部分G-code
     *
     * @param code
     * @return
     */
    private static String generateTop(String code) {
        String gcode = code;
//        String tempCode = code;
        gcode = generateBottomTop(gcode, GCodeParameters.topLayers - 1, "top");
//        StringBuilder sb = new StringBuilder(gcode);
//        sb.replace(0, tempCode.length(), "");
//        GCodeParameters.codeConditionStore.put("top", sb.toString());
        return gcode;
    }

    private static String generateLastTop(String code) {
        GCodeParameters.shellList.clear();
        GCodeParameters.pointTempList.clear();
        String gcode = code;
        String tempCode = code;
        Double height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
        gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 0)
                + "\n";
        GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
        gcode += "G1 E" + GCodeAlgorithm.formatNumber(GCodeParameters.lengthUsed, 5)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\n";
        ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
//        calculateAverage(pointList);
//        Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                pointList.get(1), shellLayers, extruderWidth);
//        previousPrintPoint = pointStart;
//        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
//                + " Y" + formatNumber(pointStart.getY(), 3)
//                + " F" + formatNumber(travelSpeedValue, 3)
//                + "\n";
//        lengthUsed = retractLengthValue;
//        gcode += "G1 E" + formatNumber(lengthUsed, 5)
//                + " F" + formatNumber(retractSpeed, 5)
//                + "\n";
        GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
        GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
        GCodeParameters.shellList.add(GCodeParameters.pointTempList);
        for (int j = 0; j < GCodeParameters.shellLayers - 1; j++) {
            boolean b = GCodeAlgorithm.generateEachShell();
            if (!b){
                break;
            }
        }
        GCodeParameters.isRetract = true;
        gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.shellSpeed, GCodeParameters.externalShellSpeed);
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            GCodeParameters.lastTopSpeed = Double.parseDouble(properties.getProperty("topLayerSpeed")) * 60;
            GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
            gcode += "G1 E" + GCodeAlgorithm.formatNumber(GCodeParameters.lengthUsed, 5)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5)
                    + "\n";
            gcode += "G92 E0\n";
            GCodeParameters.lengthUsed = GCodeParameters.retractLengthValue;
//            gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, Math.PI / 4, GCodeParameters.lastTopSpeed,
//                    GCodeParameters.extruderWidth * GCodeParameters.scaling, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder(gcode);
        sb.replace(0, tempCode.length(), "");
        GCodeParameters.codeConditionStore.put("lastTop", sb.toString());
        return gcode;
    }

    private static String generateEnd(String code) {
        String gcode = code;
        String tempCode = code;
        GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
        gcode += "G1 E" + GCodeAlgorithm.formatNumber(GCodeParameters.lengthUsed, 5)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\nM107\n";
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("construct3DObject/file/GcodeParameters.xml"));
            FileInputStream fileEnd = new FileInputStream("construct3DObject/file/endGcode.txt");
            BufferedReader readerEnd = new BufferedReader(new InputStreamReader(fileEnd));
            String endGcode = "";
            String line = readerEnd.readLine();
            while (line != null) {
                line = line.trim();
                endGcode += (line + "\n");
                line = readerEnd.readLine();
            }
            gcode += endGcode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        gcode += "M140 S0\n";
        StringBuilder sb = new StringBuilder(gcode);
        sb.replace(0, tempCode.length(), "");
        GCodeParameters.codeConditionStore.put("end", sb.toString());
        return gcode;
    }

    /**
     * This algorithm is based on the one-line infills.
     * As for the simplification, we don't use the paper's algorithm.
     */
    private static void constructSupport() {
        Double THRESHOLD_THETA = 5 * Math.PI / 12;
        Integer infillSize = GCodeParameters.layerInfillLineList.size();
        Double height = GCodeParameters.layerHeight;
        Double diameter = height * Math.tan(THRESHOLD_THETA);
        Double THRESHOLD_SQUARE = (Math.PI * Math.pow(diameter, 2)) / 4;
        for (int i = infillSize - 1; i >= 0; i--) {
            HashMap<String, ArrayList<ArrayList<Point>>> eachLayerSupportLines = new HashMap<>();
            ArrayList<ArrayList<Point>> leftEachLayerSupportLines = new ArrayList<>();
            ArrayList<ArrayList<Point>> rightEachLayerSupportLines = new ArrayList<>();
//            GCodeParameters.layerInfillLineList.get(i);
            GCodeParameters.infillLineNewList.clear();
            Double lineAngle = GCodeParameters.layerInfillLineAngle.get(i);
            Double gapOfInfillLines = GCodeParameters.layerGapInfillLines.get(i);
            GCodeParameters.infillBoundPointList = GCodeParameters.layerVirtualPointList.get(i);
            if (Math.abs(lineAngle) > 1e-6) {
                gapOfInfillLines *= 2;
                GCodeParameters.infillLineAngle = 0.0;
                GCodeAlgorithm.constructInfillLines(gapOfInfillLines);
                ArrayList<ArrayList<Point>> tempLineList;
                tempLineList = (ArrayList<ArrayList<Point>>) GCodeParameters.infillLineNewList.clone();
                GCodeParameters.layerInfillLineList.put(i, tempLineList);
            }
//            else {
//                GCodeParameters.infillLineAngle = 0.0;
//                GCodeAlgorithm.constructInfillLines(gapOfInfillLines);
//                ArrayList<ArrayList<Point>> tempLineList;
//                tempLineList = (ArrayList<ArrayList<Point>>) GCodeParameters.infillLineNewList.clone();
//                GCodeParameters.layerInfillLineList.put(i, tempLineList);
//            }
            if (i != infillSize - 1) {
                ArrayList<ArrayList<Point>> thisLayerInfillLines = GCodeParameters.layerInfillLineList.get(i);
                ArrayList<ArrayList<Point>> upperLayerInfillLines = GCodeParameters.layerInfillLineList.get(i + 1);
                Integer indexUpper = 0, indexThis = 0;
                while (Math.abs(upperLayerInfillLines.get(indexUpper).get(0).getY() -
                        thisLayerInfillLines.get(indexThis).get(0).getY()) > 1e-6) {
                    Double upperY = upperLayerInfillLines.get(indexUpper).get(0).getY();
                    Double thisY = thisLayerInfillLines.get(indexThis).get(0).getY();
                    if ((upperY - thisY) > 1e-6)
                        indexThis++;
                    if ((thisY - upperY) > 1e-6)
                        indexUpper++;
                }
                ArrayList<ArrayList<Point>> leftSupportPoints = new ArrayList<>();
                ArrayList<ArrayList<Point>> rightSupportPoints = new ArrayList<>();
                while (indexThis <= thisLayerInfillLines.size() - 1 &&
                        indexUpper <= upperLayerInfillLines.size() - 1) {
                    ArrayList<Point> thisLayerPoints = thisLayerInfillLines.get(indexThis);
                    ArrayList<Point> upperLayerPoints = upperLayerInfillLines.get(indexUpper);
                    Point upperPointStart = upperLayerPoints.get(1);
                    Point upperPointEnd = upperLayerPoints.get(0);
                    Point thisPointStart = thisLayerPoints.get(1);
                    Point thisPointEnd = thisLayerPoints.get(0);

                    if (upperPointStart.getX() - thisPointStart.getX() < -1e-6) {
                        ArrayList<Point> leftPoints = new ArrayList<>();
                        leftPoints.add(new Point(upperPointStart.getX(), thisPointStart.getY()));
                        if (upperPointEnd.getX() - thisPointStart.getX() > 1e-6) {
                            leftPoints.add(thisPointStart);
                        } else {
                            leftPoints.add(new Point(upperPointStart.getX(), thisPointStart.getY()));
                        }
                        leftSupportPoints.add(leftPoints);
                    }
                    if (upperPointEnd.getX() - thisPointEnd.getX() > 1e-6) {
                        ArrayList<Point> rightPoints = new ArrayList<>();
                        if (upperPointStart.getX() - thisPointEnd.getX() > 1e-6) {
                            rightPoints.add(new Point(upperPointStart.getX(), thisPointEnd.getY()));
                        } else {
                            rightPoints.add(thisPointEnd);
                        }
                        rightPoints.add(new Point(upperPointEnd.getX(), thisPointEnd.getY()));
                        rightSupportPoints.add(rightPoints);
                    }
                    indexThis++;
                    indexUpper++;
                }
                Double squareLeft = 0.0;
                Double squareRight = 0.0;
                for (ArrayList<Point> leftEachLines : leftSupportPoints) {
                    squareLeft += (leftEachLines.get(1).getX() - leftEachLines.get(0).getX()) *
                            GCodeParameters.extruderWidth * GCodeParameters.scaling;
                }
                for (ArrayList<Point> rightEachLines : rightSupportPoints) {
                    squareRight += (rightEachLines.get(1).getX() - rightEachLines.get(0).getX()) *
                            GCodeParameters.extruderWidth * GCodeParameters.scaling;
                }
                ArrayList<Point> leftInsertLines;
                ArrayList<Point> rightInsertLines;
                ArrayList<ArrayList<Point>> leftHigherLayerSupportLines = GCodeParameters.supportLineList.get(i + 1).get("left");
                ArrayList<ArrayList<Point>> rightHigherLayerSupportLines = GCodeParameters.supportLineList.get(i + 1).get("right");
                if (squareLeft - THRESHOLD_SQUARE >= -1e-6) {
                    for (ArrayList<Point> leftEachLines : leftSupportPoints) {
                        leftInsertLines = new ArrayList<>();
                        leftInsertLines.add(new Point(leftEachLines.get(0).getX() - (GCodeParameters.shellLayers + 1) * GCodeParameters.extruderWidth,
                                leftEachLines.get(0).getY()));
                        leftInsertLines.add(new Point(leftEachLines.get(1).getX() - (GCodeParameters.shellLayers + 1) * GCodeParameters.extruderWidth,
                                leftEachLines.get(1).getY()));
                        boolean markReplace = false;
                        ArrayList<ArrayList<Point>> leftResult = new ArrayList<>();
                        for (ArrayList<Point> compareLines : leftHigherLayerSupportLines) {
                            if (Math.abs(compareLines.get(0).getY() - leftInsertLines.get(0).getY()) < 1e-6) {
                                markReplace = true;
                                leftResult = unionPoints(leftInsertLines, compareLines);
                            }
                        }
                        if (markReplace) {
                            leftEachLayerSupportLines.addAll(leftResult);
//                            markReplace = false;
                        }
                        else {
                            leftEachLayerSupportLines.add(leftInsertLines);
                        }
                    }
                }
                ArrayList<ArrayList<Point>> copyLeftEachLayerSupportLines = (ArrayList<ArrayList<Point>>) leftEachLayerSupportLines.clone();
                for (ArrayList<Point> upperLeftLines : leftHigherLayerSupportLines) {
                    Integer indexThisLayerLeft;
                    for (indexThisLayerLeft = 0; indexThisLayerLeft < copyLeftEachLayerSupportLines.size(); indexThisLayerLeft++) {
                        if (Math.abs(upperLeftLines.get(0).getY() - copyLeftEachLayerSupportLines.get(indexThisLayerLeft).get(0).getY()) < 1e-6) {
                            break;
                        }
                    }
                    if (indexThisLayerLeft == copyLeftEachLayerSupportLines.size()) {
                        leftEachLayerSupportLines.add(upperLeftLines);
                    }
                }

                if (squareRight - THRESHOLD_SQUARE >= -1e-6) {
                    for (ArrayList<Point> rightEachLines : rightSupportPoints) {
                        rightInsertLines = new ArrayList<>();
                        rightInsertLines.add(new Point(rightEachLines.get(0).getX() + (GCodeParameters.shellLayers + 1) * GCodeParameters.extruderWidth,
                                rightEachLines.get(0).getY()));
                        rightInsertLines.add(new Point(rightEachLines.get(1).getX() + (GCodeParameters.shellLayers + 1) * GCodeParameters.extruderWidth,
                                rightEachLines.get(1).getY()));
                        boolean markReplace = false;
                        ArrayList<ArrayList<Point>> rightResult = new ArrayList<>();
                        for (ArrayList<Point> compareLines : rightHigherLayerSupportLines) {
                            if (Math.abs(compareLines.get(0).getY() - rightInsertLines.get(0).getY()) < 1e-6) {
                                markReplace = true;
                                rightResult = unionPoints(rightInsertLines, compareLines);
                            }
                        }
                        if (markReplace) {
                            rightEachLayerSupportLines.addAll(rightResult);
                        }
                        else {
                            rightEachLayerSupportLines.add(rightInsertLines);
                        }
                    }
//                    eachLayerSupportLines.add(rightInsertLines);
                }
                ArrayList<ArrayList<Point>> copyRightEachLayerSupportLines = (ArrayList<ArrayList<Point>>) rightEachLayerSupportLines.clone();
                for (ArrayList<Point> upperRightLines : rightHigherLayerSupportLines) {
                    Integer indexThisLayerRight;
                    for (indexThisLayerRight = 0; indexThisLayerRight < copyRightEachLayerSupportLines.size(); indexThisLayerRight++) {
                        if (Math.abs(upperRightLines.get(0).getY() - copyRightEachLayerSupportLines.get(indexThisLayerRight).get(0).getY()) < 1e-6) {
                            break;
                        }
                    }
                    if (indexThisLayerRight == copyRightEachLayerSupportLines.size()) {
                        rightEachLayerSupportLines.add(upperRightLines);
                    }
                }
            }
            eachLayerSupportLines.put("left", leftEachLayerSupportLines);
            eachLayerSupportLines.put("right", rightEachLayerSupportLines);
            GCodeParameters.supportLineList.put(i, eachLayerSupportLines);
        }
    }

    private static ArrayList<ArrayList<Point>> unionPoints(ArrayList<Point> lineBased, ArrayList<Point> lineCompared) {
        ArrayList<ArrayList<Point>> lineResult = new ArrayList<>();
        ArrayList<Point> eachLineResult;
        if ((lineBased.get(0).getX() - lineCompared.get(0).getX()) < -1e-6 &&
                (lineBased.get(1).getX() - lineCompared.get(1).getX()) > -1e-6) {
            eachLineResult = new ArrayList<>();
            eachLineResult.add(new Point(lineBased.get(0).getX(), lineBased.get(0).getY()));
            eachLineResult.add(new Point(lineBased.get(1).getX(), lineBased.get(1).getY()));
            lineResult.add(eachLineResult);
        }
        else if ((lineBased.get(0).getX() - lineCompared.get(0).getX()) > -1e-6 &&
                (lineBased.get(1).getX() - lineCompared.get(1).getX()) < -1e-6) {
            eachLineResult = new ArrayList<>();
            eachLineResult.add(new Point(lineCompared.get(0).getX(), lineCompared.get(0).getY()));
            eachLineResult.add(new Point(lineCompared.get(1).getX(), lineCompared.get(0).getY()));
            lineResult.add(eachLineResult);
        }
        else if ((lineBased.get(0).getX() - lineCompared.get(0).getX()) < -1e-6 &&
                (lineBased.get(1).getX() - lineCompared.get(1).getX()) < -1e-6) {
            if ((lineBased.get(1).getX() - lineCompared.get(0).getX()) > -1e-6) {
                eachLineResult = new ArrayList<>();
                eachLineResult.add(new Point(lineBased.get(0).getX(), lineBased.get(0).getY()));
                eachLineResult.add(new Point(lineCompared.get(1).getX(), lineCompared.get(1).getY()));
                lineResult.add(eachLineResult);
            }
            else if ((lineBased.get(1).getX() - lineCompared.get(0).getX()) < -1e-6) {
                eachLineResult = new ArrayList<>();
                eachLineResult.add(new Point(lineBased.get(0).getX(), lineBased.get(0).getY()));
                eachLineResult.add(new Point(lineBased.get(1).getX(), lineBased.get(1).getY()));
                lineResult.add(eachLineResult);
                eachLineResult = new ArrayList<>();
                eachLineResult.add(new Point(lineCompared.get(0).getX(), lineCompared.get(0).getY()));
                eachLineResult.add(new Point(lineCompared.get(1).getX(), lineCompared.get(1).getY()));
                lineResult.add(eachLineResult);
            }
        }
        else if ((lineBased.get(0).getX() - lineCompared.get(0).getX()) > -1e-6 &&
                (lineBased.get(1).getX() - lineCompared.get(1).getX()) > -1e-6) {
            if ((lineCompared.get(1).getX() - lineBased.get(0).getX()) > -1e-6) {
                eachLineResult = new ArrayList<>();
                eachLineResult.add(new Point(lineCompared.get(0).getX(), lineCompared.get(0).getY()));
                eachLineResult.add(new Point(lineBased.get(1).getX(), lineBased.get(1).getY()));
                lineResult.add(eachLineResult);
            }
            else if ((lineCompared.get(1).getX() - lineBased.get(0).getX()) < -1e-6) {
                eachLineResult = new ArrayList<>();
                eachLineResult.add(new Point(lineCompared.get(0).getX(), lineCompared.get(0).getY()));
                eachLineResult.add(new Point(lineCompared.get(1).getX(), lineCompared.get(1).getY()));
                lineResult.add(eachLineResult);
                eachLineResult = new ArrayList<>();
                eachLineResult.add(new Point(lineBased.get(0).getX(), lineBased.get(0).getY()));
                eachLineResult.add(new Point(lineBased.get(1).getX(), lineBased.get(1).getY()));
                lineResult.add(eachLineResult);
            }
        }
        return lineResult;
    }
}