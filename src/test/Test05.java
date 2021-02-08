package test;

import java.util.LinkedList;
import java.util.List;

public class Test05 {
    public static void main(String[] args) {
        List<double[]> list = new LinkedList<>();
        double[] data = {1,2};
        double[] data1 = {1,2};
        double[] data2 = {1,2};
        list.add(data);
        list.add(data1);
        list.add(data2);
        System.out.println(list);
    }
}
