package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.Team;
import com.example.demo.repositories.TeamRepository;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    public List<Team> getAllTeams() {
        List<Team> userRecords = new ArrayList<>();
        teamRepository.findAll().forEach(userRecords::add);
        return userRecords;
    }

    public void addTeam(Team team) {
        System.out.println(team);
        teamRepository.save(team);
    }

    public void updateTeam(Team team) {
        System.out.println("Updated Team:" + team);
        teamRepository.save(team);
    }

    public Optional<Team> getTeam(int id) {
        return teamRepository.findById(id);
    }

    /*
     * @Transactional
     * public void changeProfOffice(int id, String newoffice) {
     * Optional<Team> p = teamRepository.findById(id);
     * if (!p.isEmpty())
     * p.get().setOffice(newoffice);
     * }
     */

}