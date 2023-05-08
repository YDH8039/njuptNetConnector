package com.yang.njuptnet.dataclass;

public class Person {
    private String id;
    private String password;
    private String type;

    public Person() {
    }

    public void set(String i, String p, String t) {
        id = i;
        password = p;
        type = t;
    }

    public void setType(String t) {
        type = t;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }
}