package com.encardio.er_ngrf.activity;

public class ViewDataModel {
    private String date, parameter;

    public ViewDataModel(String date, String parameter) {
        this.date = date;
        this.parameter = parameter;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
