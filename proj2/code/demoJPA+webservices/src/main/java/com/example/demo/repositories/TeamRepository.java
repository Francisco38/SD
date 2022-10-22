package com.example.demo.repositories;

import org.springframework.data.repository.CrudRepository;

import com.example.data.Team;

public interface TeamRepository extends CrudRepository<Team, Integer> {
}