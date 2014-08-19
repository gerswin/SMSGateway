package com.parawebs.smsgateway;

public class Counter {

    //private variables
    int _id;
    int _count;
    String _status;

    // Empty constructor
    public Counter(){

    }
    // constructor
    public Counter(int id, int count, String _status){
        this._id = id;
        this._count = count;
        this._status = _status;
    }

    // constructor
    public Counter(int count, String _status){
        this._count = count;
        this._status = _status;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting name
    public int getCount(){
        return this._count;
    }

    // setting name
    public void setCount(int name){
        this._count = name;
    }

    // getting phone number
    public String getStatus(){
        return this._status;
    }

    // setting phone number
    public void setStatus(String status){
        this._status = status;
    }
}