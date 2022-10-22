package com.example.demo.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

import com.example.data.Player;

public interface PlayerRepository extends CrudRepository<Player, Integer> {
    @Query("select s from Player s where s.name like %?1")
    public List<Player> findByNameEndsWith(String chars);

    @Query(nativeQuery = true, value = "SELECT player.id as id, count(case when player.id=event.player_id and event.type=2 then 1 ELSE NULL END) as goals FROM player,event,game_events where game_events.events_id=event.id GROUP BY player.id order by goals desc,player.id limit 1")
    public String getBestPlayer();
}