package com.example.model;

import java.time.LocalDate;

public class MoonMission {
    public int id;
    public String spacecraft;
    public java.time.LocalDate launchDate;

    public int getId() {
        return id;
    }
    public String getSpacecraft() {
        return spacecraft;
    }

    public LocalDate getLaunchDate() {
        return launchDate;
    }
}

