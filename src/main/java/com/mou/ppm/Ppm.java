package com.mou.ppm;

import com.mou.ppm.commands.PpmBuild;
import com.mou.ppm.commands.PpmClean;
import com.mou.ppm.commands.PpmConfig;
import com.mou.ppm.commands.PpmInit;
import com.mou.ppm.exceptions.BusinessLogicException;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import java.io.File;
import java.net.URISyntaxException;

@CommandLine.Command(name = "ppm", subcommands = {PpmInit.class, PpmConfig.class, PpmBuild.class, PpmClean.class})
public class Ppm {


    public static final SourceDirectory PROGRAM_DIR= new SourceDirectory(getProgramDirectory());
    public static final SourceDirectory WORKING_DIR= new SourceDirectory(System.getProperty("user.dir"));

    public static void main(String... args) {
        CommandLine.IExecutionExceptionHandler errorHandler = (ex, cmd, parseResult) -> {
            if (BusinessLogicException.class.isAssignableFrom(ex.getClass())) {
                cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
                cmd.usage(cmd.getErr());
               // return 2;
            } else {
                ex.printStackTrace();
                return 1;
            }
            return cmd.getCommandSpec().exitCodeOnExecutionException();
        };
        CommandLine commandLine = new CommandLine(new Ppm()).setExecutionExceptionHandler(errorHandler);
        AnsiConsole.systemInstall(); // enable colors on Windows
        commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO)).execute(args);
        AnsiConsole.systemUninstall(); // cleanup when done

    }

    public static String getProgramDirectory() {
        try {
            String path = new File(Ppm.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + "\\";
            int index = path.lastIndexOf('/');
            if (index == -1) index = path.lastIndexOf('\\');
            return path.substring(0, index);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
