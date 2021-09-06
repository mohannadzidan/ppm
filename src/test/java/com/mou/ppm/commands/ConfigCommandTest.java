package com.mou.ppm.commands;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.gson.Gson;
import com.mou.ppm.Config;
import com.mou.ppm.Ppm;
import com.mou.ppm.SourceDirectory;
import com.mou.ppm.util.PathsUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

class ConfigCommandTest {
    private static SourceDirectory programDirectory;
    private static SourceDirectory workingDirectory;
    private static String originalUserDir;
    private static String targetDir;
    private static final Gson GSON = new Gson();

    @BeforeEach
    public void beforeEach() throws URISyntaxException, IOException {
        targetDir = PathsUtil.getJarDirectory();
        programDirectory = new SourceDirectory(targetDir + "/test-installation-dir");
        workingDirectory = new SourceDirectory(targetDir + "/test-working-dir");
        System.out.println("programDirectory=" + programDirectory.getPath());
        System.out.println("workingDirectory=" + workingDirectory.getPath());
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", workingDirectory.getPath());
    }


    @AfterEach
    public void afterEach() throws IOException {
        var programDirFile = new File(programDirectory.getPath());
        var workingDirFile = new File(workingDirectory.getPath());
        new File(targetDir+"/config.json").delete();
        if (programDirFile.exists() && programDirFile.isDirectory())
            MoreFiles.deleteDirectoryContents(programDirFile.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
        if (workingDirFile.exists() && workingDirFile.isDirectory())
            MoreFiles.deleteDirectoryContents(workingDirFile.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
        System.setProperty("user.dir", originalUserDir);
    }


    @Test
    public void configXc8() {
        var config = new PpmConfig();
        final String xc8Path = "C:/Program Files/Microchip/xc8/v2.32";
        config.setWorkingDirectory(workingDirectory);
        config.setProgramDirectory(programDirectory); // no configuration
        CommandLine cmd = new CommandLine(config);
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            Assertions.fail("Expecting no exception but " + e.getClass().getName() + " was thrown!");
            return 1;
        });
        Assertions.assertEquals(0, cmd.execute("-xc8", xc8Path), "Expecting status code of 0");
        Config c = GSON.fromJson(programDirectory.read("config.json"), Config.class);
        Assertions.assertEquals(c.getXc8Path(), xc8Path);
    }


    @Test
    public void configMkgen() {
        var config = new PpmConfig();
        final String mkgenPath = "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat";
        config.setWorkingDirectory(workingDirectory);
        config.setProgramDirectory(programDirectory); // no configuration
        CommandLine cmd = new CommandLine(config);
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            Assertions.fail("Expecting no exception but " + e.getClass().getName() + " was thrown!");
            return 1;
        });
        Assertions.assertEquals(0, cmd.execute("-mkgen", mkgenPath), "Expecting status code of 0");
        Config c = GSON.fromJson(programDirectory.read("config.json"), Config.class);
        Assertions.assertEquals(mkgenPath, c.getMkGeneratorPath());
    }

    @Test
    public void configXc8AndMkgen() {
        var config = new PpmConfig();
        final String mkgenPath = "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat";
        final String xc8Path = "C:/Program Files/Microchip/xc8/v2.32";
        config.setWorkingDirectory(workingDirectory);
        config.setProgramDirectory(programDirectory); // no configuration
        CommandLine cmd = new CommandLine(config);
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            Assertions.fail("Expecting no exception but " + e.getClass().getName() + " was thrown!");
            return 1;
        });
        Assertions.assertEquals(0, cmd.execute("-mkgen", mkgenPath, "-xc8", xc8Path), "Expecting status code of 0");
        Config c = GSON.fromJson(programDirectory.read("config.json"), Config.class);
        Assertions.assertEquals(mkgenPath, c.getMkGeneratorPath());
        Assertions.assertEquals(xc8Path, c.getXc8Path());

    }
}