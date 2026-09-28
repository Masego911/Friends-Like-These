package com.friendslikethese.backend.registration;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;

@FunctionalInterface
interface GoogleCredentialsProvider {
    GoogleCredentials load() throws IOException;
}
