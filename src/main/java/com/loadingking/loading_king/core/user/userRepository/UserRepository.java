package com.loadingking.loading_king.core.user.userRepository;

import com.loadingking.loading_king.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

   Optional<User> findByEmail(String email);

}
