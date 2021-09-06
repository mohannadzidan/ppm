package com.mou.ppm.commands;

import com.google.gson.Gson;
import com.mou.ppm.*;


public abstract class PpmCommand implements Runnable{
    private SourceDirectory workingDirectory = Ppm.WORKING_DIR;
    private SourceDirectory programDirectory = Ppm.PROGRAM_DIR;

    public SourceDirectory getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(SourceDirectory workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public SourceDirectory getProgramDirectory() {
        return programDirectory;
    }

    public void setProgramDirectory(SourceDirectory programDirectory) {
        this.programDirectory = programDirectory;
    }


}
