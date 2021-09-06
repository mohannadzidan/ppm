package com.mou.ppm.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathsUtilTest {
    @Test
    public void testGetJarPath(){
        Assertions.assertEquals("E:\\Projects-Embedded\\ppm\\target\\classes", PathsUtil.getJarPath());
        System.out.println(PathsUtil.getJarPath());
    }

    @Test
    public void testGetJarDirectory(){
        Assertions.assertEquals("E:\\Projects-Embedded\\ppm\\target", PathsUtil.getJarDirectory());
    }

    @Test
    public void testGetWorkingDirectory(){
        Assertions.assertEquals(System.getProperty("user.dir"), PathsUtil.getWorkingDirectory());
    }
}