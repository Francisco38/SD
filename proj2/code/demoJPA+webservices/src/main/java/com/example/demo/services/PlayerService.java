package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.Player;
import com.example.demo.repositories.PlayerRepository;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> getAllPlayers() {
        List<Player> userRecords = new ArrayList<>();
        playerRepository.findAll().forEach(userRecords::add);
        return userRecords;
    }

    public void addPlayer(Player player) {
        playerRepository.save(player);
    }

    public Optional<Player> getPlayer(int id) {
        return playerRepository.findById(id);
    }

    public String getBestPlayer() {
        return playerRepository.getBestPlayer();
    }

    public List<Player> findByNameEndsWith(String chars) {
        return playerRepository.findByNameEndsWith(chars);
    }

}