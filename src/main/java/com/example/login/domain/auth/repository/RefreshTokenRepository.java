package com.example.login.domain.auth.repository;


import com.example.login.domain.auth.Entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}