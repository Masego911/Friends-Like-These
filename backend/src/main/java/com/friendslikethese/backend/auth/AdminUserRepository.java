package com.friendslikethese.backend.auth;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface AdminUserRepository extends JpaRepository<AdminUser,UUID>{Optional<AdminUser> findByEmailIgnoreCase(String email);Optional<AdminUser> findByUsernameIgnoreCaseAndEnabledTrue(String username);}
