package com.credits.wallet.desktop.utils.sourcecode.building;

/**
 * Created by goncharov-eg on 02.03.2018.
 */
public class BuildSourceCodeError {
    private String text;
    private Integer line;
    private Integer column;
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

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }
}
