package javautils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    private static void appendProperties(String resourceFile, Properties properties){
        InputStream propStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(resourceFile);
        try {
            properties.load(propStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadProperties(String resourcePath){
        Properties properties = new Properties();
        appendProperties(resourcePath, properties);
        return properties;
    }

}
