package com.example.data;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "students" })
@XmlRootElement
public class Team {
    @Id
    private int id;
    private String name;
    private String code;
    private String country;
    private int founded;
    private String logo;

    @OneToMany(targetEntity = Player.class)
    private List<Player> players;

    public Team() {
        this.players = new ArrayList<>();
    }

    public Team(int id, String name, String code, String country, int founded, String logo) {
        this.id = id;
        this.players = new ArrayList<>();
        this.name = name;
        this.code = code;
        this.country = country;
        this.founded = founded;
        this.logo = logo;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getFounded() {
        return this.founded;
    }

    public void setFounded(int founded) {
        this.founded = founded;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Object getPlayers() {
        return this.players;
    }

    public void addPlayers(Player players) {
        this.players.add(players);
    };

    public void setPlayers(List<Player> players) {
        this.players = players;
    };

    public String toString() {
        return this.name + " id: " + this.id + " code: " + this.code + " country: " + this.country + " logo: "
                + this.logo;
    }

}
