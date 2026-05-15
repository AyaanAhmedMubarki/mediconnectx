package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);// New method for finding by email
}
