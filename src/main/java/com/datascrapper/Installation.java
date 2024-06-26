package com.datascrapper;

public class Installation {
    private int id;
    private String indicatorCode;

    public Installation() {
    }

    public Installation(int id, String indicatorCode) {
        this.id = id;
        this.indicatorCode = indicatorCode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIndicatorCode() {
        return indicatorCode;
    }

    public void setIndicatorCode(String indicatorCode) {
        this.indicatorCode = indicatorCode;
    }

    @Override
    public String toString() {
        return "Installation{" +
                "id=" + id +
                ", indicatorCode='" + indicatorCode + '\'' +
                '}';
    }
}
