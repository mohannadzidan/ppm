package com.mou.ppm.commands;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.gson.Gson;
import com.mou.ppm.Config;
import com.mou.ppm.SourceDirectory;
import com.mou.ppm.exceptions.BadConfigurationException;
import com.mou.ppm.exceptions.UnsupportedChipException;
import com.mou.ppm.util.PathsUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

class InitCommandTest {
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
        if (programDirFile.exists() && programDirFile.isDirectory())
            MoreFiles.deleteDirectoryContents(programDirFile.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
        if (workingDirFile.exists() && workingDirFile.isDirectory())
            MoreFiles.deleteDirectoryContents(workingDirFile.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    public void failBadConfiguration() {
        var init = new PpmInit();
        init.setWorkingDirectory(workingDirectory);
        init.setProgramDirectory(programDirectory); // no configuration
        CommandLine cmd = new CommandLine(init);
        AtomicReference<Exception> x = new AtomicReference<>();
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            x.set(e);
            return 1;
        });
        cmd.execute("-f", "test", "pic16f877a");
        Assertions.assertInstanceOf(BadConfigurationException.class, x.get());
    }

    @Test
    public void failWrongChip() {
        Config config = new Config(
                "C:/Program Files/Microchip/xc8/v2.32",
                "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat"
        );
        var init = new PpmInit();
        programDirectory.write("config.json", GSON.toJson(config));
        init.setWorkingDirectory(workingDirectory);
        init.setProgramDirectory(programDirectory);
        var cmd = new CommandLine(init);
        var x = new AtomicReference<Exception>();
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            x.set(e);
            return 1;
        });
        cmd.execute("test", "wrongchip");
        Assertions.assertInstanceOf(UnsupportedChipException.class, x.get());
    }

    @Test
    public void askWhenOverwrite() {
        Config config = new Config(
                "C:/Program Files/Microchip/xc8/v2.32",
                "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat"
        );
        programDirectory.write("config.json", GSON.toJson(config));
        workingDirectory.write("project.json", "{}"); // file to be overwritten
        var init = new PpmInit();
        init.setWorkingDirectory(workingDirectory);
        init.setProgramDirectory(programDirectory);
        CommandLine cmd = new CommandLine(init);
        var exception = new AtomicReference<Exception>();
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            e.printStackTrace();
            exception.set(e);
            return 1;
        });

        var projectFile = new File(workingDirectory.getPath(), "project.json");
        var notModifiedTime = projectFile.lastModified();
        var in = new ByteArrayInputStream("n".getBytes());
        var oldIn = System.in;
        System.setIn(in);
        cmd.execute("test", "pic16f877a");
        System.setIn(oldIn);
        Assertions.assertNull(exception.get(), "Not expecting any exceptions!");
        Assertions.assertEquals(projectFile.lastModified(), notModifiedTime, "project.json modified!");
    }

    @Test
    void init() {
        Config config = new Config(
                "C:/Program Files/Microchip/xc8/v2.32",
                "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat"
        );
        programDirectory.write("config.json", GSON.toJson(config));
        var init = new PpmInit();
        init.setWorkingDirectory(workingDirectory);
        init.setProgramDirectory(programDirectory);
        CommandLine cmd = new CommandLine(init);
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            e.printStackTrace();
            Assertions.fail("Expecting no exceptions but " + e.getClass().getName() + " was thrown!");
            return 1;
        });
        Assertions.assertEquals(cmd.execute("test", "pic16f877a"), 0, "Expecting 0 status code");
    }

    @Test
    public void forceInit() {
        Config config = new Config(
                "C:/Program Files/Microchip/xc8/v2.32",
                "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat"
        );
        programDirectory.write("config.json", GSON.toJson(config));
        workingDirectory.write("project.json", "{}"); // file to be overwritten
        var init = new PpmInit();
        init.setWorkingDirectory(workingDirectory);
        init.setProgramDirectory(programDirectory);
        CommandLine cmd = new CommandLine(init);
        cmd.setExecutionExceptionHandler((e, i, n) -> {
            e.printStackTrace();
            Assertions.fail("Expecting no exceptions but " + e.getClass().getName() + " was thrown!");
            return 1;
        });
        var projectFile = new File(workingDirectory.getPath(), "project.json");
        var notModifiedTime = projectFile.lastModified();
        cmd.execute("-f", "test", "pic16f877a");
        Assertions.assertNotEquals(notModifiedTime, projectFile.lastModified(), "project.json not modified!");
    }

}