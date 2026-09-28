package com.friendslikethese.backend.auth;
import org.springframework.security.core.userdetails.*;import org.springframework.stereotype.Service;
@Service public class AdminUserDetailsService implements UserDetailsService{
 private final AdminUserRepository users; public AdminUserDetailsService(AdminUserRepository users){this.users=users;}
 public UserDetails loadUserByUsername(String username){AdminUser u=users.findByUsernameIgnoreCaseAndEnabledTrue(username.trim()).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));return User.withUsername(u.getUsername()).password(u.getPasswordHash()).roles(u.getRole().name()).build();}
}
