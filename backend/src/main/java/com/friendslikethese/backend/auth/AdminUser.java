package com.friendslikethese.backend.auth;
import jakarta.persistence.*;import java.time.OffsetDateTime;import java.util.UUID;
@Entity @Table(name="admin_users",uniqueConstraints=@UniqueConstraint(name="uq_admin_user_email",columnNames="email"))
public class AdminUser {
 @Id @GeneratedValue(strategy=GenerationType.UUID) @Column(name="admin_id",nullable=false,updatable=false) private UUID id;
 @Column(name="username",length=100) private String username;
 @Column(nullable=false,length=254) private String email;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Column(name="display_name",nullable=false,length=120) private String displayName;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private AdminRole role=AdminRole.ADMIN;
 @Column(nullable=false) private boolean enabled=true;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @Column(name="last_login_at") private OffsetDateTime lastLoginAt;
 protected AdminUser(){}
 public AdminUser(String username,String email,String passwordHash,String displayName){this.username=normalizeUsername(username);this.email=email.trim().toLowerCase();this.passwordHash=passwordHash;this.displayName=displayName.trim();}
 @PrePersist void create(){createdAt=updatedAt=OffsetDateTime.now();} @PreUpdate void update(){updatedAt=OffsetDateTime.now();}
 public UUID getId(){return id;} public String getUsername(){return username;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getDisplayName(){return displayName;} public AdminRole getRole(){return role;} public boolean isEnabled(){return enabled;} public OffsetDateTime getLastLoginAt(){return lastLoginAt;}
 public void updateUsername(String username){this.username=normalizeUsername(username);}
 public void disable(){this.enabled=false;}
 public void recordLogin(){lastLoginAt=OffsetDateTime.now();}
 private static String normalizeUsername(String username){return username==null||username.isBlank()?null:username.trim().toLowerCase();}
}
