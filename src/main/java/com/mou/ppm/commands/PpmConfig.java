package com.mou.ppm.commands;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.mou.ppm.Config;
import com.mou.ppm.Ppm;
import com.mou.ppm.exceptions.BadConfigurationException;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@CommandLine.Command(name = "config", description = "Configures ppm")
public class PpmConfig extends PpmCommand {
    @CommandLine.Option(names = {"-xc8"}, description = "Sets path for xc8 compiler bin directory")
    private String xc8Path;
    @CommandLine.Option(names = {"-mkgen", "-makeFilesGenerator"}, description = "Sets path for prjMakefilesGenerator.bat file")
    private String mkgenPath;

    @Override
    public void run() {
        if (xc8Path == null && mkgenPath == null) {
            throw new RuntimeException("Missing option!");
        }
        var gson = new Gson();
        var config = gson.fromJson(Objects.requireNonNullElse(getProgramDirectory().read("config.json"), "{}"), Config.class);
        if (xc8Path != null) {
            if(!Files.exists(Path.of(xc8Path+"/bin/xc8.exe"))){
                throw new BadConfigurationException("this path doesn't refer to a valid xc8 directory!");
            }
            config.setXc8Path(xc8Path);
        }
        if (mkgenPath != null) {
            // validate path
            if(!Files.exists(Path.of(mkgenPath))){
                throw new BadConfigurationException("this path doesn't refer to prjMakefilesGenerator.bat!");
            }
            config.setMkGeneratorPath(mkgenPath);
        }
        getProgramDirectory().write("config.json", gson.toJson(config));
    }
}
