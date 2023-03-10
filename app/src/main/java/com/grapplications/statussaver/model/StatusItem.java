package com.grapplications.statussaver.model;

import android.net.Uri;

import java.io.File;

public class StatusItem {

    private File file;
    private String fileName, filePath;
    private Uri fileUri;
    private boolean isDownloaded = false;
    private boolean isSelected = false;
    private long timeStamp;

    public StatusItem() {
    }

    public StatusItem(File file, String fileName, String filePath, Uri fileUri, long timeStamp) {
        this.file = file;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileUri = fileUri;
        this.timeStamp = timeStamp;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
