package com.fastcsu.fastcampusnavigation;

/**
 * Created by Dawid on 11/22/16.
 */

public class Room {

    String NUMBER;
    String FLOOR;

    public Room(String number) {
        this.NUMBER = number;
    }

    public void setNumber(String number) {
        number = NUMBER;
    }

    public String getNumber() {
        return NUMBER;
    }

    public void setFloor(String floor) {
        floor = FLOOR;
    }

    public String getFloor() {
        return FLOOR;
    }
}
