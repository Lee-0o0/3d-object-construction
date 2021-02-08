package com.lee.algorithm;

import com.lee.entity.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class GCodeParameters {
    static Double lengthUsed;
    static Double layerHeight;
    static Integer scaling; // 缩放比例
    static Double filamentDiameter;
    static Integer fileLine;
    static Integer bottomFileLine, innerFileLine, topFileLine, lastTopFileLine;
    static ArrayList<String> zStrings = new ArrayList<>(); // 高度list
    static Double maxDim; // 最大维度值
    static Boolean isRetract = false;

    static ArrayList<Double> zPrintList = new ArrayList<>(); // 高度list
    static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();
    static ArrayList<Point> infillBoundPointList = new ArrayList<>();
    static HashMap<Integer, ArrayList<Point>> layerVirtualPointList = new HashMap<>();
    static HashMap<Integer, Double> layerGapInfillLines = new HashMap<>();
    //    private static ArrayList<Line> infillLineList = new ArrayList<>();
    static ArrayList<Point> pointTempList = new ArrayList<>(); // 临时存放单层点list
    static ArrayList<ArrayList<Point>> shellList = new ArrayList<>(); // 边界list
    static ArrayList<ArrayList<Point>> infillLineNewList = new ArrayList<>();
    static HashMap<Integer, ArrayList<ArrayList<Point>>> layerInfillLineList = new HashMap<>();
    static HashMap<Integer, HashMap<String, ArrayList<ArrayList<Point>>>> supportLineList = new HashMap<>();

    static Double firstBedTemperature;
    static Double firstExtruderTemperature;
    static Double bedTemperature;
    static Double extruderTemperature;
    static Double travelSpeedValue;
    static Double retractSpeed;
    static Double retractLengthValue;
    static Double firstLayerSpeed;
    static Integer shellLayers;
    static Double avgX, avgY;                    // 单层边界的平均坐标（x，y）
    static Double extruderWidth;
    static Point previousPrintPoint;
    static Double infillLineAngle;
    static HashMap<Integer, Double> layerInfillLineAngle = new HashMap<>();
    static Integer bottomLayers, innerLayers, topLayers;
    static Double lineGap;
    static Double shellSpeed, externalShellSpeed;
    static Double solidInfillSpeed, infillSpeed;
    static Double lastTopSpeed;
    static HashMap<String, String> codeConditionStore = new HashMap<>();
}
