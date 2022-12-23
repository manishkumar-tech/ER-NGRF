package com.encardio.er_ngrf.activity;

public class UploadFilesModel {
    private String fileName;
    private boolean isSelected;


    public UploadFilesModel(String fileName, boolean isSelected) {
        this.fileName = fileName;
        this.isSelected = isSelected;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
