package test;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class Test08 {
    public static void main(String[] args) throws SigarException {
        Sigar sigar = new Sigar();
        Mem mem = sigar.getMem();
        long total = mem.getTotal();
        System.out.println(total);
    }
}
