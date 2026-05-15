package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUserId(Long userId);
}

