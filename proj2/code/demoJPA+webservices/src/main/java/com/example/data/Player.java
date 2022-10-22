package com.example.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Player {
    @Id
    private int id;
    private int team_id;
    private String name;
    private int age;
    private int number;
    private String position;
    private String photo;

    public Player() {
    }

    public Player(int team_id) {
        this.team_id = team_id;
    }

    public Player(int id, int team_id, String name, int age, int number, String position, String photo) {
        this.id = id;
        this.team_id = team_id;
        this.name = name;
        this.age = age;
        this.number = number;
        this.position = position;
        this.photo = photo;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeam_id() {
        return this.team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String toString() {
        return this.name + "(id = " + this.id + "). Name: " + this.name + " - Age: " + this.age;
    }
}
