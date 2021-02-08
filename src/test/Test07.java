package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Test07 {
    public static void main(String[] args) throws Exception{
        Properties properties = new Properties();
        properties.setProperty("name","zs");
        properties.storeToXML(new FileOutputStream("file/test.xml"),"comment");

        Properties properties1 = new Properties();
        properties.setProperty("age","16");
        properties.storeToXML(new FileOutputStream("file/test1.xml"),"comment");

        Properties properties2 = new Properties();
        properties2.loadFromXML(new FileInputStream("file/test.xml"));
        properties2.loadFromXML(new FileInputStream("file/test1.xml"));
        System.out.println(properties2.getProperty("name"));
        System.out.println(properties2.getProperty("age"));
    }
}
