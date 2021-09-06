package com.mou.ppm.commands;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.mou.ppm.Project;
import com.mou.ppm.exceptions.ProjectNotFoundException;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(name = "clean", description = "Cleans the current project.")
public class PpmClean extends PpmCommand implements Runnable {
    @Override
    public void run() {
        Project project = getWorkingDirectory().readJson("project.json", Project.class);
        if (project == null) {
            throw new ProjectNotFoundException("Couldn't find ppm project at current directory!");
        }
        try {
            MoreFiles.deleteDirectoryContents(Path.of(getWorkingDirectory().getPath() + "\\dist"), RecursiveDeleteOption.ALLOW_INSECURE);
            MoreFiles.deleteDirectoryContents(Path.of(getWorkingDirectory().getPath() + "\\build"), RecursiveDeleteOption.ALLOW_INSECURE);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
