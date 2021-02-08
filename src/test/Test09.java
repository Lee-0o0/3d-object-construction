package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test09 {
    public static void main(String[] args) {
        Map<String, List<Integer>> map = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        map.put("a",list);

        List<Integer> list1 = new ArrayList<>();
        list1.add(2);
        map.put("a",list1);

        for (String key : map.keySet()) {
            System.out.println(map.get(key));
        }

    }
}
