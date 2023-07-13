package com.example.getip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtils {

    private static final String CONFIG_FILE_PATH = "~/CIP_demo/shared-config.properties";

//    public static String getIP() {
//        Properties properties = loadProperties();
//        return properties.getProperty("ip");
//    }

    public static void saveIP(String ip) {
        Properties properties = loadProperties();
        properties.setProperty("ip", ip);
        saveProperties(properties);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static void saveProperties(Properties properties) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(fos, "Shared Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
