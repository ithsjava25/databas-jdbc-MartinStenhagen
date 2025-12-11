package com.example.model;

public class Account {
    public int id;
    public String name;
    public String password;
    public String firstName;
    public String lastName;
    public String ssn;

    public Account(String name, String password, String firstName, String lastName, String ssn) {
        this.name = name;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
    }
    public Account(){}
}
