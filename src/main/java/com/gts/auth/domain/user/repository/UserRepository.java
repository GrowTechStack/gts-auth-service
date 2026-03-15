package com.gts.auth.domain.user.repository;

import com.gts.auth.domain.user.entity.AuthProvider;
import com.gts.auth.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
