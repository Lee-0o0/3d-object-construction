package entity;

import java.util.Arrays;

/** 正方形类 */
public class Square {
    private Point upleft; // 左上角的顶点
    private Point lowright; // 右下角的顶点
    private double[] data ;   // 从左上角按照顺时针顺序，每个顶点的值
    int type;
    private double threshold;

    public Square(Point upleft,Point lowright ,double[] data,double threshold){
        this.upleft = upleft;
        this.lowright = lowright;
        this.data = data;
        this.threshold = threshold;

        // 确定类型
        StringBuffer stringBuffer = new StringBuffer("");
        for(int i = 0; i < data.length; i++){
            if (Double.compare(data[i] , threshold) < 0){
                stringBuffer.append("0");
            }else {
                stringBuffer.append("1");
            }
        }
        String res = stringBuffer.toString();

        switch (res){
            case "0000":
            case "1111":type=1;break;
            case "0001":
            case "1110":type=2;break;
            case "0010":
            case "1101":type=3;break;
            case "0011":
            case "1100":type=4;break;
            case "0100":
            case "1011":type=5;break;
            case "0101":
            case "1010":type=6;break;
            case "0110":type=7;break;
            case "1001":type=8;break;
            case "0111":
            case "1000":type=9;break;
            default:type=0;break;
        }
    }

    public Point getUpleft() {
        return upleft;
    }

    public void setUpleft(Point upleft) {
        this.upleft = upleft;
    }

    public Point getLowright() {
        return lowright;
    }

    public void setLowright(Point lowright) {
        this.lowright = lowright;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Square{" +
                "upleft=" + upleft +
                ", lowright=" + lowright +
                ", data=" + Arrays.toString(data) +
                ", type=" + type +
                ", threshold=" + threshold +
                '}';
    }
}
