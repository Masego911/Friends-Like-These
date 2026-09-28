package com.friendslikethese.backend.auth;
import org.slf4j.*;import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.*;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.stereotype.Component;
@Component public class AdminBootstrap implements ApplicationRunner{
 private static final Logger log=LoggerFactory.getLogger(AdminBootstrap.class);private final AdminUserRepository users;private final PasswordEncoder encoder;private final String username,email,password,displayName;
 public AdminBootstrap(AdminUserRepository users,PasswordEncoder encoder,@Value("${app.admin.bootstrap.username:}")String username,@Value("${app.admin.bootstrap.email:}")String email,@Value("${app.admin.bootstrap.password:}")String password,@Value("${app.admin.bootstrap.display-name:Administrator}")String displayName){this.users=users;this.encoder=encoder;this.username=username;this.email=email;this.password=password;this.displayName=displayName;}
 public void run(ApplicationArguments args){if(users.count()==0&&!username.isBlank()&&!email.isBlank()&&!password.isBlank()){users.save(new AdminUser(username,email,encoder.encode(password),displayName));log.info("Initial administrator created from bootstrap configuration.");}else if(users.count()==0)log.warn("No administrator exists and bootstrap credentials are not configured.");}
}
