package test;

import java.math.BigDecimal;

public class Test06 {
    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal("10.01");
        BigDecimal bigDecimal1 = new BigDecimal("10.02");
        System.out.println(bigDecimal);
        System.out.println(bigDecimal1);
        BigDecimal plus = bigDecimal.add(bigDecimal1);
        System.out.println(plus);
        System.out.println(bigDecimal.divide(bigDecimal1,20,0));
        System.out.println(BigDecimal.valueOf(1.0/3));

        double a = 10.01;
        double b = 10.02;
        double c = a+b;
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(a/b);

    }
}
