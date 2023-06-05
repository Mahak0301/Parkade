package com.example.project;

import java.io.Serializable;

public class UserDetails implements Serializable {
public String name,phone,email;
public int UserType;

    //public UserDetails(){}
    public UserDetails(String name , String phone,String email, int userType ) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.UserType = userType;
    }

    public UserDetails() {
    }


}
