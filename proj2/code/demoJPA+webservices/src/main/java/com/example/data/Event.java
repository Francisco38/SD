package com.example.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Event implements Comparable<Event> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int game_id;
    private int player_id;
    private String message;
    private String data;
    private int type;

    public Event() {
    }

    public Event(int game_id, int type) {
        this.type = type;
        this.game_id = game_id;
    }

    public Event(String message, String data, int type) {
        this.message = message;
        this.data = data;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public int getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Event e) {
        return this.getData().compareTo(e.getData());
    }
}
