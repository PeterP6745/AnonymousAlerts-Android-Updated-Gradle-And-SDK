package com.messagelogix.anonymousalerts.model;

/**
 * Created by Program on 6/26/2015.
 * Item model to hold key pairs values
 */
public class Item {
    private String _id;
    private String _value;

    public Item(String id, String values) {
        _id = id;
        _value = values;
    }


    public String getId() {
        return this._id;
    }

    public String getValue() {
        return this._value;
    }

}
