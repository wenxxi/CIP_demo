package com.example.testapi;

import jakarta.persistence.*;
@Entity
@Table(name="test1")
public class User {
    @Id
    private String ID;

    private String Name;
    private String Gender;
    private java.sql.Date Birthdate;

    public String getID() { return ID; }
    public void setID(String ID) { this.ID = ID; }

    public String getName() { return Name; }
    public void setName(String Name) { this.Name = Name; }

    public String getGender() { return Gender; }
    public void setGender(String Gender) { this.Gender = Gender; }

    public java.sql.Date getBirthdate() { return Birthdate; }
    public void setBirthdate(java.sql.Date Birthdate) { this.Birthdate = Birthdate; }
}
