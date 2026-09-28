package com.friendslikethese.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.friendslikethese.backend.registration.GoogleSheetsProperties;
import com.friendslikethese.backend.registration.RegistrationSyncProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({GoogleSheetsProperties.class, RegistrationSyncProperties.class})
public class FriendsLikeTheseBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FriendsLikeTheseBackendApplication.class, args);
    }
}
