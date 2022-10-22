package com.example.data;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.OneToMany;

@Entity
@XmlRootElement
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int team1Id;
    private int team2Id;

    @ManyToOne(targetEntity = Team.class)
    private Team team1;
    @ManyToOne(targetEntity = Team.class)
    private Team team2;

    private int score1;
    private int score2;

    private String location;
    private String data;

    @OneToMany(targetEntity = Event.class)
    private List<Event> events;

    @OneToMany(targetEntity = Event.class)
    private List<Event> events_Check;

    public Game() {
        this.events = new ArrayList<>();
        this.events_Check = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(int team1Id) {
        this.team1Id = team1Id;
    }

    public int getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(int team2Id) {
        this.team2Id = team2Id;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvents(Event events) {
        this.events.add(events);
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public int getScore1() {
        return score1;
    }

    public void setScore1(int score1) {
        this.score1 = score1;
    }

    public int getScore2() {
        return score2;
    }

    public void setScore2(int score2) {
        this.score2 = score2;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Event> getEvents_Check() {
        return events_Check;
    }

    public void addEvents_Check(Event events_Check) {
        this.events_Check.add(events_Check);
    }

    public void setEvents_Check(List<Event> events_Check) {
        this.events_Check = events_Check;
    }

    public void deleteEvents_Check(Event events_Check) {
        this.events_Check.remove(events_Check);
    }
}
