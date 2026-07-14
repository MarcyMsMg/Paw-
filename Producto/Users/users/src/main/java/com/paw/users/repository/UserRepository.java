package com.paw.users.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.paw.users.enums.UserRole;
import com.paw.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(UserRole role);
}
