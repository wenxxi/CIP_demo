package com.example.testapi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;

@Entity
@Table(
   name = "test1"
)
public class User {
   @Id
   private String ID;
   private String Name;
   private String Gender;
   private Date Birthdate;

   public String getID() {
      return this.ID;
   }

   public void setID(String ID) {
      this.ID = ID;
   }

   public String getName() {
      return this.Name;
   }

   public void setName(String Name) {
      this.Name = Name;
   }

   public String getGender() {
      return this.Gender;
   }

   public void setGender(String Gender) {
      this.Gender = Gender;
   }

   public Date getBirthdate() {
      return this.Birthdate;
   }

   public void setBirthdate(Date Birthdate) {
      this.Birthdate = Birthdate;
   }
}
