package com.messagelogix.anonymousalerts.model;

/**
 * Created by Richard on 10/1/2015.
 * This is a model to help displaying the help and ressources
 */
public class HelpResource {

    private String title;
    private String value;
    private String type;

    public HelpResource(String title, String value, String type) {
        this.title = title;
        this.value = value;
        this.type = type;
    }

    public HelpResource() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "title " + title + " value = " + value;
    }
}
