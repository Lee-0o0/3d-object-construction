package com.lee.entity;

import java.util.Arrays;

public class LineDistortionError {
    private LineSegment line;
    private double[] proxy;
    private double distortion;

    public LineDistortionError(LineSegment line, double[] proxy) {
        this.line = line;
        this.proxy = proxy;
    }

    public LineSegment getLine() {
        return line;
    }

    public void setLine(LineSegment line) {
        this.line = line;
    }

    public double[] getProxy() {
        return proxy;
    }

    public void setProxy(double[] proxy) {
        this.proxy = proxy;
    }

    public double getDistortion() {
        return distortion;
    }

    public void setDistortion(double distortion) {
        this.distortion = distortion;
    }

    @Override
    public String toString() {
        return "LineDistortionError{" +
                "line=" + line +
                ", proxy=" + Arrays.toString(proxy) +
                ", distortion=" + distortion +
                '}';
    }
}
