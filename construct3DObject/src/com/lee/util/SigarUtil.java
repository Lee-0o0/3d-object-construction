package com.lee.util;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.io.File;

public class SigarUtil {

    /**
     * 获取内存总量，以字节（B）为单位
     * @return
     */
    public static long getTotalMemory() {
        Sigar sigar = initSigar();
        if (sigar != null) {
            Mem mem = null;
            try {
                mem = sigar.getMem();
            }catch (SigarException s){
                s.printStackTrace();
            }
            long total = mem.getTotal();
            System.out.println("内存总量："+total/1024/1024/1024+"GB");
            return total;
        }else {
            return -1;
        }
    }

    /**
     * 将sigar依赖库文件加入到Sigar初始化中
     * @return
     */
    public static Sigar initSigar() {
        try {
            //此处只为得到依赖库文件的目录，可根据实际项目自定义
            File classPath = new File("construct3DObject/sigar");

            String path = System.getProperty("java.library.path");
            String sigarLibPath = classPath.getCanonicalPath();
            //为防止java.library.path重复加，此处判断了一下
            if (!path.contains(sigarLibPath)) {
                if (isWin()) {
                    path += ";" + sigarLibPath;
                    System.out.println(path);
                } else {
                    path += ":" + sigarLibPath;
                }
                System.setProperty("java.library.path", path);
            }
            return new Sigar();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断操作系统是否为Windows
     * @return true if os is Windows
     */
    public static boolean isWin(){
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            return true;
        } else return false;
    }
}
