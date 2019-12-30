package com.jerry.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * 平台工具类
 */
public final class Platform {

    /**
     * @return 系统类型
     * 返回{@code true}表示属于Windows系系统
     * 返回{@code false}表示属于Unix系系统
     */
    public static boolean isWindows() {
        return System.getProperties().getProperty("os.name")
                .toUpperCase().contains("WINDOWS");
    }

    /**
     * @return 系统环境信息
     */
    public static String environment() {
        Properties props = System.getProperties();
        return "Environmental information\n" +
                "SYSTEM_INFO   " +
                props.getProperty("os.name") +
                " version " +
                props.getProperty("os.version") + "\n" +
                "USER_NAME     " +
                props.getProperty("user.name") + "\n" +
                "WORKSPACE     " +
                props.getProperty("user.dir") + "\n" +
                "JAVA_VERSION  " +
                props.getProperty("java.version") + "\n" +
                "JVM_VERSION   " +
                props.getProperty("java.vm.name") + "\n" +
                "SUPPLIER      " +
                props.getProperty("java.vm.vendor") + "\n" +
                "JAVA_HOME     " +
                props.getProperty("java.home") + "\n";
    }

    /**
     * @return 本地IP地址
     */
    public static String address() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException ignored) {
        }
        return null;
    }
}
