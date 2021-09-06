package com.mou.ppm;

public class Config {
    private String xc8Path, mkGeneratorPath;

    public Config() {}

    public Config(String xc8Path, String mkGeneratorPath) {
        this.xc8Path = xc8Path;
        this.mkGeneratorPath = mkGeneratorPath;
    }

    public String getXc8Path() {
        return xc8Path;
    }

    public void setXc8Path(String xc8Path) {
        this.xc8Path = xc8Path;
    }

    public String getMkGeneratorPath() {
        return mkGeneratorPath;
    }

    public void setMkGeneratorPath(String mkGeneratorPath) {
        this.mkGeneratorPath = mkGeneratorPath;
    }
}
