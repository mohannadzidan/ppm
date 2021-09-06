package com.mou.ppm;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.gson.Gson;
import com.mou.ppm.util.Console;
import com.mou.ppm.util.PathsUtil;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PpmTest {
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


    //@AfterEach
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
    public void createProject() {


        var ppm = new Ppm();
        CommandLine cmd = new CommandLine(new Ppm());
        final String mkgenPath = "E:/mplabx-ide/mplab_platform/bin/prjMakefilesGenerator.bat";
        final String xc8Path = "C:/Program Files/Microchip/xc8/v2.32";
        cmd.execute("config", "-xc8", xc8Path, "-mkgen", mkgenPath);
        cmd.execute("init", "-f", "test", "pic16f877a", "-entry", "src/main.c");
        /* ********** FOLDER STRUCTURE **********
        lib/
            lib1.h (includes lib2.h)
            lib1.c (includes lib1.h)
            lib2.h (includes lib3.h)
            lib2.c (includes lib2.h)
            lib3.h (includes definitions.h)
            lib3.c (includes lib3.h )
            definitions.h
        src/
            main.c (includes ../lib/lib1.h)
        ***************************************** */
        workingDirectory.write("libs/lib1.h", "#include \"lib2.h\"\nvoid f1();");
        workingDirectory.write("libs/lib1.c", "#include \"lib1.h\"\nvoid f1(){}");
        workingDirectory.write("libs/lib2.h", "#include \"lib3.h\"\nvoid f2();");
        workingDirectory.write("libs/lib2.c", "#include \"lib2.h\"\nvoid f2(){}");
        workingDirectory.write("libs/lib3.h", "#include \"definitions.h\"\nvoid f3();");
        workingDirectory.write("libs/lib3.c", "#include \"lib3.h\"\nvoid f3(){}");
        workingDirectory.write("libs/definitions.h", "#define SOME_DEFINITION 52");
        workingDirectory.write("src/main.c", "#include \"../libs/lib1.h\"\nvoid main(){f1();f2();f3();}");
        cmd.execute("build");
    }
}