package com.credits.wallet.desktop.utils.sourcecode.building;


public class BuildSourceCodeError {

    private String className;
    private String text;
    private Integer line;

    public BuildSourceCodeError(String className, Integer line, String text) {
        this.className = className;
        this.text = text;
        this.line = line;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
