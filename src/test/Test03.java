package test;

import entity.Point;
import util.MathUtil;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Test03 {
    public static void main(String[] args) {
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });
        priorityQueue.add(10);
        priorityQueue.add(3);
        priorityQueue.add(7);
        priorityQueue.add(1);
        while (!priorityQueue.isEmpty()){
            System.out.println(priorityQueue.poll());
        }
    }
}
