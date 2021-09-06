package com.mou.ppm.util;

import com.mou.ppm.Ppm;

import java.io.File;
import java.net.URISyntaxException;

public final class PathsUtil {
    private PathsUtil() {
    }

    /**
     * @return the path to the program jar file
     */
    public static String getJarPath() {
        try {
            return new File(Ppm.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the path to current working directory which started this VM session
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * @return the path for the directory that contains the program jar
     */
    public static String getJarDirectory() {
        var jarPath = getJarPath();
        return jarPath.substring(0, jarPath.lastIndexOf('\\', jarPath.length() - 2));
    }
}
