package com.example.demo.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.data.Users;

public interface UserRepository extends CrudRepository<Users, Integer> {
    @Query("select u from Users u where u.email = ?1")
    public Users findByName(String chars);
}
