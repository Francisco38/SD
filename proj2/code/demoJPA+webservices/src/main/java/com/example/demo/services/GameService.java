package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.Game;
import com.example.demo.repositories.GameRepository;

@Service
public class GameService {
    @Autowired
    private GameRepository gamesRepository;

    public List<Game> getAllGames() {
        List<Game> userRecords = new ArrayList<>();
        gamesRepository.findAll().forEach(userRecords::add);
        return userRecords;
    }

    public void addGame(Game game) {
        gamesRepository.save(game);
    }

    public List<String> getTeamStats(int d) {
        if (d == 0) {
            return gamesRepository.getStatsTeam();
        } else if (d == 1) {
            return gamesRepository.getStatsTotal();
        } else if (d == 2) {
            return gamesRepository.getStatsWins();
        } else if (d == 3) {
            return gamesRepository.getStatsLosses();
        } else if (d == 4) {
            return gamesRepository.getStatsTie();
        } else if (d == 5) {
            return gamesRepository.getStatsAvg();
        } else if (d == 6) {
            return gamesRepository.getStatsGoals();
        }
        return gamesRepository.getStatsTeam();
    }

    public String getTeamComp(int d1, int d2) {
        return gamesRepository.getTeamComp1(d1, d2);
    }

    public String getCardNumbers(int d1, int d2) {
        return gamesRepository.getTeamComp2(d1, d2);
    }

    public Optional<Game> getGame(int id) {
        return gamesRepository.findById(id);
    }
}