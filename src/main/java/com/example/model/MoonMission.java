package com.example.model;

import java.time.LocalDate;

public class MoonMission {
    public int id;
    public String spacecraft;
    public java.time.LocalDate launchDate;
    public String carrier_rocket;
    public String operator;
    public String mission_type;
    public String outcome;

    public int getId() {
        return id;
    }
    public String getSpacecraft() {
        return spacecraft;
    }

    public LocalDate getLaunchDate() {
        return launchDate;
    }
    public String getCarrier_rocket() {
        return carrier_rocket;
    }
    public String getOperator() {
        return operator;
    }
    public String getMission_type() {
        return mission_type;
    }
    public String getOutcome() {
        return outcome;
    }
}

