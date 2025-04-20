package com.example.login.Refresh.repository;


import com.example.login.Refresh.Entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}