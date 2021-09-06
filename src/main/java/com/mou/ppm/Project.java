package com.mou.ppm;

public class Project {
    private String name, chip, entry;
    private String[] dependencies;
    private boolean mplabSupport;

    public Project() {
    }

    public Project(String name, String chip, String entry, String[] dependencies) {
        this.name = name;
        this.chip = chip;
        this.entry = entry;

        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChip() {
        return chip;
    }

    public void setChip(String chip) {
        this.chip = chip;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

}
