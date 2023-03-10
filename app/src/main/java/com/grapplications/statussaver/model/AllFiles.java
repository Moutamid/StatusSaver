package com.grapplications.statussaver.model;

import java.io.File;

public class AllFiles {
    private File file;

    public AllFiles(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
