package com.example.testapi;
import org.springframework.data.repository.CrudRepository;

import com.example.testapi.User;

public interface UserRepository extends CrudRepository<User, Integer> {

}