package com.messagelogix.anonymousalerts.model;

/**
 * Created by Program on 6/24/2015.
 * this model is for the locator contact manager
 */
public class Contact {

    private String _name, _phoneNumber, _email;
    private int _id;

    public Contact(int id, String name, String phoneNumber, String email) {
        _id = id;
        _name = name;
        _phoneNumber = phoneNumber;
        _email = email;
    }

    public int getId() {
        return this._id;
    }

    public String getName() {
        return this._name;
    }

    public String getPhoneNumber() {
        return this._phoneNumber;
    }

    public String getEmail() {
        return this._email;
    }

    @Override
    public String toString(){
        return "_name = " + this._name + " | _phoneNumber = " + this._phoneNumber  +" | _email = " + this._email;

    }
}
