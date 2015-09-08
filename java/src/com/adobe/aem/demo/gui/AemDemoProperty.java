package com.adobe.aem.demo.gui;

public class AemDemoProperty {

    private String value;
    private String label;

    public AemDemoProperty(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return this.value;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return label;
    }
}