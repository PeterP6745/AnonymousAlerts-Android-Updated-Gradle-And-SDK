package com.messagelogix.anonymousalerts.model;

import android.content.Context;
import android.content.res.TypedArray;

import com.messagelogix.anonymousalerts.R;

import java.util.ArrayList;

public class Parent {
    private final Context context;
    private String mTitle;
    private ArrayList<String> mArrayChildren;
    private int iconIndex;
    private TypedArray navMenuIcons;

    public Parent(Context context) {
        this.context = context;


        navMenuIcons = context.getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
    }

    @SuppressWarnings("ResourceType")
    public int getDrawable() {

        if (this.iconIndex == 0) {
            return navMenuIcons.getResourceId(0, -1);
        } else if (this.iconIndex == 1) {
            return navMenuIcons.getResourceId(1, -1);
        } else if (this.iconIndex == 2) {
            return navMenuIcons.getResourceId(2, -1);
        }else if (this.iconIndex == 3) {
            return navMenuIcons.getResourceId(3, -1);
        } else {
            return navMenuIcons.getResourceId(0, -1);
        }

    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public ArrayList<String> getArrayChildren() {
        return mArrayChildren;
    }

    public void setArrayChildren(ArrayList<String> arrayChildren) {
        mArrayChildren = arrayChildren;
    }
}