package com.example.project;

public class BookedSlotKey {
    public BookedSlots bookedSlots;
    public String key;
    public String NumberPlate;

    public BookedSlotKey(){}
    public BookedSlotKey(BookedSlots bookedSlots, String key,String NumberPlate){
        this.bookedSlots=bookedSlots;
        this.key=key;
        this.NumberPlate=NumberPlate;
    }
}
